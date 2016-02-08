package sjg.animation;

import java.util.*;
import java.awt.*;

/**
 * A list of {@link SJGSprite sprites}.
 * <p>
 * <p>A call to move or draw will propagate the call out to the sprites.
 *
 * @author Christian Hvid
 */

public class Sprites {
    private Vector sprites = new Vector();
    private Vector spritesInQueue = new Vector();
    private View view;

    public Sprites(View view) {
        this.view = view;
    }

    public Sprites() {
        view = NullView.getInstance();
    }

    public View getView() {
        return view;
    }

    public Enumeration elements() {
        return sprites.elements();
    }

    public void add(SJGSprite sprite, double x, double y) {
        sprite.setX(x);
        sprite.setY(y);
        add(sprite);
    }

    public void add(SJGSprite sprite) {
        sprites.addElement(sprite);
    }

    public void remove(SJGSprite sprite) {
        sprites.removeElement(sprite);
    }

    public void removeAll() {
        sprites.removeAllElements();
    }

    public void draw(Graphics g) {
        for (Enumeration e = elements(); e.hasMoreElements(); ) {
            SJGSprite s = (SJGSprite) e.nextElement();
            s.draw(g, view);
        }
    }

    public void move() {
        for (Enumeration e = elements(); e.hasMoreElements(); ) {
            SJGSprite s = (SJGSprite) e.nextElement();
            s.move();
        }
    }
}
