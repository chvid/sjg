package sjg.gm;

import sjg.GraphicsModel;
import sjg.SJGame;

import java.awt.*;

/**
 * A {@link GraphicsModel graphics model} that accesses the game window through a buffer.
 *
 * @author Christian Hvid
 */

public class Buffered extends GraphicsModel {
    SJGame sjg;
    Image frontImage;

    private void initFrontImage() {
        // this call to System.gc() speeds up IE's JVM

        frontImage = null;
        System.gc();

        frontImage = sjg.createImage(sjg.getWidth(), sjg.getHeight());
        setFront(frontImage.getGraphics());

        getFront().setColor(Color.white);
        getFront().fillRect(0, 0, sjg.getWidth(), sjg.getHeight());
        getFront().setColor(Color.black);
    }

    public Buffered(SJGame sjg) {
        super(sjg);
        this.sjg = sjg;
        initFrontImage();
    }

    protected void screenSizeChanged() {
        initFrontImage();
    }

    protected void startDraw(Graphics g) {
    }

    protected void endDraw(Graphics g) {
        g.drawImage(frontImage, 0, 0, null);
    }
}
