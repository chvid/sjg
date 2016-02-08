package sjg.maze;

import java.awt.*;

/**
 * NullElement.
 *
 * @author Christian Hvid
 */

public class NullElement extends Element {
    private static NullElement instance = new NullElement();

    public static NullElement getInstance() {
        return instance;
    }

    private NullElement() {
    }

    public void draw(Graphics g, int x, int y) {
    }

    public boolean isSolid() {
        return false;
    }
}
