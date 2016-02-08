package sjg.animation;

import java.awt.*;

/**
 * Sprite class with state.
 *
 * @author Christian Hvid
 */

public class SJGSpriteFSM extends SJGSprite {
    private SJGSpriteState state;

    protected void setState(SJGSpriteState state) {
        this.state = state;
    }

    public SJGSpriteState getState() {
        return state;
    }

    public void move() {
        state.move();
    }

    public void draw(Graphics g) {
        state.draw(g);
    }
}

