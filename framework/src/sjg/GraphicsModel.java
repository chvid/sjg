package sjg;

import java.awt.*;

/**
 * Encapsulates access to the game window.
 * <p>
 * <p>This is the superclass for the graphics models. All graphics models provide basic access to a Graphics thru the front field.
 * <p>
 * <p>The meaning of front is the topmost layer of the graphics model. If the model provides say a 3D renderer the front field
 * should either be null or return a reference a graphics object that draws on top of the rendered scene.
 * <p>
 * <p>The SJGame superclass calls {@link #startDraw startDraw} when it has access to the screen via a valid Graphics object and calls
 * {@link #endDraw endDraw} when it leaves the paint method.
 *
 * @author Christian Hvid
 */

public class GraphicsModel {
    private Graphics front = null;

    /**
     * Returns a pointer to the front Graphics object.
     * <p>
     * <p>The front is the topmost layer of the scene.
     */

    public Graphics getFront() {
        return front;
    }

    /**
     * Sets the front Graphics object.
     * <p>
     * <p>The front is the topmost layer of the scene.
     */

    protected void setFront(Graphics front) {
        this.front = front;
    }

    /**
     * Does nothing by the default.
     * <p>
     * <p>You must always create a graphics model with the current (this)
     * game as first parameter.
     */

    public GraphicsModel(SJGame sjg) {
    }

    /**
     * Called by the {@link SJGame SJGame} superclass when it enters a draw pass.
     *
     * @param g will be a valid pointer to the Graphics object of the game window.
     */

    protected void startDraw(Graphics g) {
    }

    /**
     * Called by the {@link SJGame SJGame} superclass when it exits a draw pass.
     *
     * @param g will be a valid pointer to the Graphics object of the game window.
     */

    protected void endDraw(Graphics g) {
    }

    protected void screenSizeChanged() {
    }
}
