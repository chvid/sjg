package sjg.animation;

import java.awt.*;

/**
 * Sprite super class.
 *
 * @author Christian Hvid
 */

public class SJGSprite {
    private double x, y;
    private int width = 32;
    private int height = 32;

    protected void translate(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    protected void setHeight(int height) {
        this.height = height;
    }

    public int getLeft() {
        return getX() - getWidth() / 2;
    }

    public int getTop() {
        return getY() - getHeight() / 2;
    }

    public int getX() {
        return (int) Math.round(x);
    }

    public int getY() {
        return (int) Math.round(y);
    }

    public double getXdouble() {
        return x;
    }

    public double getYdouble() {
        return y;
    }

    protected void setX(double x) {
        this.x = x;
    }

    protected void setY(double y) {
        this.y = y;
    }

    public boolean collidesWith(SJGSprite s) {
        return ((getLeft() < (s.getLeft() + s.getWidth())) && ((getLeft() + getWidth()) > s.getLeft()) &&
                (getTop() < (s.getTop() + s.getHeight())) && ((getTop() + getHeight()) > s.getTop()));
    }

    public void move() {
    }

    public void draw(Graphics g, View view) {
    }
}
