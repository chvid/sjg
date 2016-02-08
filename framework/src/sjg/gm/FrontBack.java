package sjg.gm;

import sjg.GraphicsModel;
import sjg.SJGame;

import java.awt.*;

/**
 * A two layered (buffered) {@link GraphicsModel graphics model}.
 * <p>
 * <p>When a draw pass is started the contents of the back layer is copied
 * to the front layer which when the draw pass ends is copied to
 * the game window.
 *
 * @author Christian Hvid
 */

public class FrontBack extends GraphicsModel {
    private Image frontImage;
    private Image backImage;
    private Graphics back;

    /**
     * Returns the back Graphics object
     */

    public Graphics getBack() {
        return back;
    }

    /**
     * Sets the back Graphics object
     */

    protected void setBack(Graphics back1) {
        back = back1;
    }

    public FrontBack(SJGame sjg) {
        super(sjg);
        frontImage = sjg.createImage(sjg.getWidth(), sjg.getHeight());
        backImage = sjg.createImage(sjg.getWidth(), sjg.getHeight());
        setFront(frontImage.getGraphics());
        setBack(backImage.getGraphics());

        getFront().setColor(Color.white);
        getFront().fillRect(0, 0, sjg.getWidth(), sjg.getHeight());
        getFront().setColor(Color.black);

        getBack().setColor(Color.white);
        getBack().fillRect(0, 0, sjg.getWidth(), sjg.getHeight());
        getBack().setColor(Color.black);
    }

    protected void startDraw(Graphics g) {
        getFront().drawImage(backImage, 0, 0, null);
    }

    protected void endDraw(Graphics g) {
        g.drawImage(frontImage, 0, 0, null);
    }
}
