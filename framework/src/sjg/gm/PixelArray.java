package sjg.gm;

import sjg.*;

import java.awt.*;
import java.awt.image.*;

class Producer extends MemoryImageSource {
    public ImageConsumer getConsumer() {
        return imageConsumer;
    }

    public Producer(int i, int j, ColorModel colorModel, int ai[], int k, int l) {
        super(i, j, colorModel, ai, k, l);
    }

    public synchronized void addConsumer(ImageConsumer imageConsumer) {
        this.imageConsumer = imageConsumer;
        super.addConsumer(imageConsumer);
    }

    private ImageConsumer imageConsumer;
}

/**
 * A {@link GraphicsModel graphics model} that allows rendering thru a pixel array.
 *
 * @author Christian Hvid
 */

public class PixelArray extends GraphicsModel {
    public int pixels[];
    Image frontImage; // the front layer
    Producer producer;
    SJGame sjg;

    public PixelArray(SJGame sjg) {
        super(sjg);
        this.sjg = sjg;
        pixels = new int[sjg.getWidth() * sjg.getHeight()];
        producer = new Producer(sjg.getWidth(), sjg.getHeight(), new DirectColorModel(32, 0xff0000, 0x00ff00, 0x0000ff), pixels, 0, sjg.getWidth());
        setFront(null);
    }

    protected void startDraw(Graphics g) {
    }

    protected void endDraw(Graphics g) {
        frontImage = sjg.createImage(producer);
        g.drawImage(frontImage, 0, 0, null);
        System.gc();
    }
}
