package sjg;

import java.awt.event.*;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Represents the player at the local machine.
 * <p>
 * <p>The local player object is constructed by {@link SJGame SJGame} and
 * is responsible for translating events into mouse and keyboard states.
 *
 * @author Christian Hvid
 */

public class LocalPlayer extends Player implements MouseMotionListener, MouseListener, KeyListener {
    private KeyboardState newKeyboardState = new KeyboardState();
    private MouseState newMouseState = new MouseState();

    /**
     * allows the player state to be replicated across the network
     */

    synchronized public void write(DataOutputStream ud) throws IOException {
        getMouseState().write(ud);
        getKeyboardState().write(ud);
    }

    /**
     * Implementation of the KeyListener interface.
     */

    public void keyPressed(KeyEvent e) {
        newKeyboardState.setDown(e.getKeyCode(), true);
    }

    public void keyReleased(KeyEvent e) {
        newKeyboardState.setDown(e.getKeyCode(), false);
    }

    public void keyTyped(KeyEvent e) {
    }

    /**
     * Implementation of the MouseListener interface
     */

    public void mouseMoved(MouseEvent e) {
        newMouseState.setX(e.getX());
        newMouseState.setY(e.getY());
        newMouseState.setLeft((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK);
        newMouseState.setMiddle((e.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK);
        newMouseState.setRight((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK);
    }

    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    public void mouseClicked(MouseEvent e) {
        mouseMoved(e);
        newMouseState.setLeft(false);
        newMouseState.setMiddle(false);
        newMouseState.setRight(false);
    }

    public void mouseEntered(MouseEvent e) {
        mouseMoved(e);
    }

    public void mouseExited(MouseEvent e) {
        mouseMoved(e);
    }

    public void mousePressed(MouseEvent e) {
        mouseMoved(e);
    }

    public void mouseReleased(MouseEvent e) {
        newMouseState.setLeft(false);
        newMouseState.setMiddle(false);
        newMouseState.setRight(false);
        //	mouseMoved(e);
    }

    protected void move() {
        setMouseState(newMouseState);
        setKeyboardState(newKeyboardState);

        newKeyboardState = (KeyboardState) newKeyboardState.clone();
        newMouseState = (MouseState) newMouseState.clone();
    }
}
