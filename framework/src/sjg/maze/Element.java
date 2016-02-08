package sjg.maze;

import sjg.animation.*;

import java.awt.*;

/**
 * An element (block) in a maze.
 *
 * @author Christian Hvid
 */

public abstract class Element {
    public abstract void draw(Graphics g, int x, int y);

    public abstract boolean isSolid();

    public Element touch(SJGSprite sprite, int x, int y) {
        return this;
    }
}
