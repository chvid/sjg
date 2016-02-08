package sjg.animation;

import java.awt.*;

/**
 * SJGSprite with frame animation.
 *
 * @author Christian Hvid
 */

public class SJGSpriteFA extends SJGSprite {
    private Animation animation;
    private int frame;

    protected void setFrame(int frame) {
        this.frame = frame;
    }

    protected int getFrame() {
        return frame;
    }

    protected void nextFrame() {
        frame++;
    }

    protected void prevFrame() {
        frame--;
    }

    protected void reset() {
        frame = 0;
    }

    public int getWidth() {
        return animation.getFrame(frame).getWidth();
    }

    public int getHeight() {
        return animation.getFrame(frame).getHeight();
    }

    protected void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public void draw(Graphics g, View view) {
        g.drawImage(animation.getFrame(frame).getImage(),
                view.worldToRealX(getX()) - animation.getFrame(frame).getWidth() / 2,
                view.worldToRealY(getY()) - animation.getFrame(frame).getHeight() / 2,
                null);
    }
}
