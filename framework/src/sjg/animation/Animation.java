package sjg.animation;

import java.util.*;

/**
 * A list of {@link Frame frames}.
 * <p>
 * The Animation may be looped or not looped. Animations are created
 * by {@link Cropper Cropper} .
 *
 * @author Christian Hvid
 */

public class Animation {
    private String name;
    private Vector frames = new Vector();
    private boolean looped;

    public int size() {
        return frames.size();
    }

    public Frame getFrame(int index) {
        if (looped == true)
            return (Frame) frames.elementAt(index % frames.size());
        else {
            if (index >= frames.size())
                return (Frame) frames.elementAt(frames.size() - 1);
            else
                return (Frame) frames.elementAt(index % frames.size());
        }
    }

    public String getName() {
        return name;
    }

    protected void addFrame(Frame frame) {
        frames.addElement(frame);
    }

    public Animation(String name, boolean looped) {
        this.name = name;
        this.looped = looped;
    }
}
