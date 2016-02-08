package sjg;

/**
 * Encapsulates the input from the player.
 * <p>
 * <p>It is an abstract class. Concrete instances are either the {@link LocalPlayer local player} or an
 * {@link sjg.network.RemotePlayer remote proxy} of a player.
 * <p>The class is composed of three objects:
 * <ul>
 * <li>{@link #getKeyboardState one} containing the current state of the keyboard
 * <li>{@link #getMouseState one} containing the current state of the mouse
 * <li>and {@link #getOldMouseState one} containing the previous (one tick ago) state of the mouse
 * </ul>
 * <p>The trick is you catch mouse clicks by comparing the current state with the state one move pass ago.
 *
 * @author Christian Hvid
 */

public class Player {
    private MouseState mouseState = new MouseState();
    private KeyboardState keyboardState = new KeyboardState();
    private MouseState oldMouseState = new MouseState();

    /**
     * Returns the current state of the mouse.
     */

    public MouseState getMouseState() {
        return mouseState;
    }

    protected void setMouseState(MouseState mouseState) {
        oldMouseState = this.mouseState;
        this.mouseState = mouseState;
    }

    /**
     * Returns the state of the mouse one move pass ago.
     */

    public MouseState getOldMouseState() {
        return oldMouseState;
    }

    /**
     * Returns the current state of the keyboard.
     */

    public KeyboardState getKeyboardState() {
        return keyboardState;
    }

    protected void setKeyboardState(KeyboardState keyboardState) {
        this.keyboardState = keyboardState;
    }
}
