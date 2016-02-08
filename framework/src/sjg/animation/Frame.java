package sjg.animation;

import java.awt.*;

/**
 * Represents a single image in an {@link Animation animation}.
 * <p>
 * Frames are created by {@link Cropper Cropper}.
 *
 * @author Christian Hvid
 */

public class Frame {
    private Image image;
    private String name;
    private int width;
    private int height;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Image getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public Frame(String name, Image image, int width, int height) {
        this.name = name;
        this.image = image;
        this.width = width;
        this.height = height;
    }
}
