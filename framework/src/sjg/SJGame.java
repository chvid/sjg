package sjg;

import sjg.network.NullNetworkModel;
import sjg.xml.Document;
import sjg.xml.Parser;

import java.applet.Applet;
import java.awt.*;

/**
 * The core of the framework.
 * <p>
 * <p>SJGame contains the main loop which sends
 * move and draw calls to the current screen. The user should override the
 * {@link #init init()} method to set the initial screen and graphics model.
 *
 * @author Christian Hvid
 */

public abstract class SJGame extends Applet implements Runnable {
    private int animationSpeed = 80;

    private int frameCount = 0;
    private int enterAt = 0;

    private LocalPlayer localPlayer = new LocalPlayer();

    private Thread m;
    private Screen screen, newScreen;
    private GraphicsModel graphicsModel;
    private NetworkModel networkModel = new NullNetworkModel(this);

    private int width = -1;
    private int height;

    private void setWidthAndHeight() {
        Dimension size = getSize();
        width = size.width;
        height = size.height;
        if (height < 2) height = 2;
        if (width < 2) width = 2;
    }

    /**
     * Returns the width of this applet.
     * <p>
     * <p>Eventhough this method is implemented in the Applet class since Java 1.2 I have implemented this
     * so this method can be used on earlier platforms. The method calls getSize().
     */

    public int getWidth() {
        if (width == -1)
            setWidthAndHeight();

        return width;

    }

    /**
     * Returns the height of this applet.
     * <p>
     * <p>Eventhough this method is implemented in the Applet class since Java 1.2 I have implemented this
     * so this method can be used on earlier platforms. The method calls getSize().
     */

    public int getHeight() {
        if (width == -1)
            setWidthAndHeight();

        return height;

    }

    /**
     * Returns the number of move calls since program initialization.
     * <p>
     * <p>The user cannot set this variable.
     */

    public int getFrameCount() {
        return frameCount;
    }

    /**
     * Returns the number of move calls since last screen shift.
     * <p>
     * <p>The user cannot set this variable.
     */

    public int getFramesSinceEnter() {
        return getFrameCount() - enterAt;

    }

    /**
     * Returns the selected GraphicsModel.
     * <p>
     * <p>The GraphicsModel encapsulates user access to the screen.
     * The user must specify graphics model in the init method.
     *
     * @see #setGraphicsModel
     * @see #init
     */

    public GraphicsModel getGraphicsModel() {
        return graphicsModel;
    }

    /**
     * Returns the selected NetworkModel.
     * <p>
     * <p>The network model encapsulates usage og network and maintains Player proxies.
     */

    public NetworkModel getNetworkModel() {
        return networkModel;

    }

    /**
     * Returns the local player.
     * <p>
     * <p>A game has one or more players - one them sits at this (local) machine. He is the local player.
     * The class encapsulates keyboard and mouse input.
     */

    public LocalPlayer getLocalPlayer() {
        return localPlayer;
    }

    /**
     * Set the animation speed.
     * <p>
     * <p>The unit is number of milliseconds between each move call.
     * Default is 70 millisecs or approx. 14 frames pr. second.
     */

    public void setAnimationSpeed(int animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    /**
     * Sets the Graphics Mode.
     * <p>
     * <p>The graphics model encapsulates access to the game window.
     * <p>
     * <p>The user must set the graphics model in the {@link #init init()} method. It should be se anywhere else.
     */

    public void setGraphicsModel(GraphicsModel graphicsModel) {
        this.graphicsModel = graphicsModel;

    }

    /**
     * Sets the Network Model.
     * <p>
     * The network model encapsulates network communications and may maintain player proxies.
     * <p>
     * By default the network model is set to {@link NullNetworkModel NullNetworkModel}.
     */

    public void setNetworkModel(NetworkModel networkModel) {
        this.networkModel = networkModel;
    }

    /**
     * Changes current screen.
     * <p>
     * <p>Screens are implemented as the State design pattern with inner classes.
     * Screens will receive move and draw calls from this class and enter and exit will be called
     * when a screen is entered or exited.<p>
     * <p>
     * The first screen should be set in the {@link #init init()} method.
     */

    public void showScreen(Screen screen1) {
        newScreen = screen1;
    }

    private void setScreen(Screen screen1) {
        if (screen != null)
            screen.exit();

        screen = screen1;
        enterAt = getFrameCount();

        screen.enter();
    }

    /**
     * Returns the current screen.
     */

    public Screen getScreen() {
        return screen;
    }

    public SJGame() {
        super();
        addKeyListener(getLocalPlayer());
        addMouseMotionListener(getLocalPlayer());
        addMouseListener(getLocalPlayer());
    }

    public void init() {

    }

    public void paint(Graphics g) {
        graphicsModel.startDraw(g);
        draw();
        graphicsModel.endDraw(g);
    }

    public void update(Graphics g) {
        graphicsModel.startDraw(g);
        draw();
        graphicsModel.endDraw(g);
    }

    /**
     * The main game loop.
     */

    public void run() {
        while (true) {
            try {
                long t1 = System.currentTimeMillis();
                networkModel.receiveCommands();
                move();
                networkModel.sendCommands();
                repaint();
                long t2 = System.currentTimeMillis();
                Thread.sleep(
                        // animationSpeed
                        Math.max(5, animationSpeed - t2 + t1)
                );
            } catch (InterruptedException e) {
                stop();
            }
        }
    }

    public void start() {
        if (m == null) {
            m = new Thread(this);
            m.start();
        }
    }

    public void stop() {
        if (m != null) {
            // this line causes a deprecated warning - but prevents null pointer errors in IE
            m.stop();
            m = null;
        }
    }

    synchronized private void move() {
        frameCount++;

        Dimension size = getSize();

        if ((size.width != width) || (size.height != height)) {
            width = size.width;
            height = size.height;
            graphicsModel.screenSizeChanged();
        }

        getLocalPlayer().move();
        if (screen != newScreen)
            setScreen(newScreen);

        if (screen != null)
            screen.move();
    }

    private void draw() {
        if (screen != null)
            screen.draw();
    }

    /**
     * Checks if a given point is contained in a given rectangle.
     */

    public static boolean within(int x1, int y1, int x, int y, int w, int h) {
        return ((x <= x1) && (y <= y1) && (x + w > x1) && (y + h > y1));
    }

    private Document configuration;

    public synchronized Document getConfiguration() {
        if (configuration == null) {
            String fn = getConfigurationFileName();
            if (fn == null) return null;

            configuration = Parser.parse(this, getConfigurationFileName());
        }

        return configuration;
    }

    public abstract String getConfigurationFileName();

}
