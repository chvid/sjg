package sjg;

import java.io.*;

/**
 * Represents the state of the mouse.
 *
 * @author Christian Hvid
 */

public class MouseState implements Cloneable {
    private int x;
    private int y;
    private int countLeft = 0;
    private int countRight = 0;
    private int countMiddle = 0;
    private boolean left;
    private boolean middle;
    private boolean right;

    public Object clone() {
        MouseState ms = new MouseState();
        ms.x = x;
        ms.y = y;
        ms.left = left;
        ms.middle = middle;
        ms.right = right;
        return ms;
    }

    /**
     * Returns the horisontal position of the mouse.
     */

    public int getX() {
        return x;
    }

    protected void setX(int x1) {
        x = x1;
    }

    /**
     * Returns the vertical position of the mouse.
     */

    public int getY() {
        return y;
    }

    protected void setY(int y1) {
        y = y1;
    }

    /**
     * Returns true if the left button is pressed otherwise it returns false.
     */

    public boolean isLeft() {
        return left;
    }

    protected void setLeft(boolean left1) {
        if ((left == true) && (left1 == false)) countLeft++;
        left = left1;
    }

    public int countLeft() {
        int r = countLeft;
        countLeft = 0;
        return r;
    }

    /**
     * Returns true if the middle button is pressed otherwise it returns false.
     */

    public boolean isMiddle() {
        return middle;
    }

    protected void setMiddle(boolean middle1) {
        if ((middle == true) && (middle1 == false)) countMiddle++;
        middle = middle1;
    }

    public int countMiddle() {
        int r = countMiddle;
        countMiddle = 0;
        return r;
    }

    /**
     * Returns true if the right button is pressed otherwise it returns false.
     */

    public boolean isRight() {
        return right;
    }

    protected void setRight(boolean right1) {
        if ((right == true) && (right1 == false)) countRight++;
        right = right1;
    }

    public int countRight() {
        int r = countRight;
        countRight = 0;
        return r;
    }

    public void read(DataInputStream ind) throws IOException {
        int i;

        setX(ind.readInt());
        setY(ind.readInt());

        i = ind.readInt();

        setLeft((i & 1) == 1);
        setMiddle((i & 2) == 2);
        setRight((i & 4) == 4);
    }

    public void write(DataOutputStream ud) throws IOException {
        int i;

        ud.writeInt(getX());
        ud.writeInt(getY());

        i = 0;

        if (isLeft() == true) i += 1;
        if (isMiddle() == true) i += 2;
        if (isRight() == true) i += 4;

        ud.writeInt(i);
    }
}
