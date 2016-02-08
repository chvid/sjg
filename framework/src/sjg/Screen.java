package sjg;

/**
 * Represents a screen.
 * <p>
 * <p>Based on the GoF state pattern.
 */

public class Screen {
    /**
     * Called by {@link SJGame SJGame} when this screen becomes the current screen.
     */

    public void enter() {
    }

    /**
     * Called by {@link SJGame SJGame} when this screen no longer is the current screen.
     */

    public void exit() {
    }

    /**
     * Called by {@link SJGame SJGame} at a steady frequency.
     */

    public void move() {
    }

    /**
     * Usualy called by {@link SJGame SJGame} after each move pass.
     */

    public void draw() {
    }
}
