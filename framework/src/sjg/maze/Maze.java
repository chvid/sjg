package sjg.maze;

import sjg.animation.*;

import java.awt.*;
import java.util.*;

/**
 * Maze
 *
 * @author Christian Hvid
 */

public class Maze {
    private int blockWidth = 32;
    private int blockHeight = 32;

    private Element maze[][];
    private int width, height;

    private String name;

    private int screenHeight = 400;
    private int screenWidth = 560;

    public void setScreenSize(int screenWidth, int screenHeight) {
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
    }

    public void setBlockSize(int blockWidth, int blockHeight) {
        this.blockWidth = blockWidth;
        this.blockHeight = blockHeight;
    }

    public int getWidth() {
        return width * blockWidth;
    }

    public int getHeight() {
        return height * blockHeight;
    }

    public int getWidthInBlocks() {
        return width;
    }

    public int getHeightInBlocks() {
        return height;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public int getBlockWidth() {
        return blockWidth;
    }

    public Element getElement(int x, int y) {
        return maze[x][y];
    }

    public void setElement(int x, int y, Element element) {
        maze[x][y] = element;
    }

    public synchronized void draw(Graphics g, View view) {
        for (int x = 0; x < width; x++) {
            int realX = view.worldToRealX(x * blockWidth);
            if ((realX > -blockWidth) &&
                    (realX < screenWidth))
                for (int y = 0; y < height; y++) {
                    int realY = view.worldToRealY(y * blockHeight);
                    if ((realY > -blockHeight) &&
                            (realY < screenHeight))
                        maze[x][y].draw(g, realX, realY);
                }
        }
    }

    public boolean collidesWith(SJGSprite sprite) {
        return (canMove(sprite, sprite.getX(), sprite.getY()) == false);
    }

    public int count(Element element) {
        int result = 0;
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (element == maze[x][y]) result++;
        return result;
    }

    public boolean canMove(SJGSprite sprite, double x1, double y1) {
        double w = (double) sprite.getWidth();
        double h = (double) sprite.getHeight();
        double sx = x1 - w / 2;
        double sy = y1 - h / 2;

        for (int x = Math.max((int) Math.floor(sx / blockWidth), 0); x <= Math.min((int) Math.floor((sx + w - 1) / blockWidth), width - 1); x++)
            for (int y = Math.max((int) Math.floor(sy / blockHeight), 0); y <= Math.min((int) Math.floor((sy + h - 1) / blockHeight), height - 1); y++)
                if (maze[x][y].isSolid())
                    return false;

        return true;
    }

    public void touch(SJGSprite sprite) {
        double w = (double) sprite.getWidth();
        double h = (double) sprite.getHeight();
        double sx = sprite.getX() - w / 2;
        double sy = sprite.getY() - h / 2;

        for (int x = Math.max((int) Math.floor(sx / blockWidth), 0); x <= Math.min((int) Math.floor((sx + w - 1) / blockWidth), width - 1); x++)
            for (int y = Math.max((int) Math.floor(sy / blockHeight), 0); y <= Math.min((int) Math.floor((sy + h - 1) / blockHeight), height - 1); y++)
                maze[x][y] = maze[x][y].touch(sprite, x * blockWidth, y * blockHeight);
    }

    public void init() {
        width = 0;
        for (int i = 0; i < d.length; i++) width = Math.max(width, d[i].length());
        height = d.length;
        maze = new Element[width][height];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (x < d[y].length())
                    maze[x][y] = elementFactory.getElement(d[y].charAt(x));
                else
                    maze[x][y] = NullElement.getInstance();

    }

    public String getName() {
        return name;
    }

    private String d[];
    private ElementFactory elementFactory;

    public Maze(sjg.xml.Element element, ElementFactory ef) {
        name = element.getAttribute("name").getValue();

        int size = 0;
        for (Enumeration e = element.elements("b"); e.hasMoreElements(); ) {
            e.nextElement();
            size++;
        }
        d = new String[size];
        int i = 0;
        for (Enumeration e = element.elements("b"); e.hasMoreElements(); ) {
            d[i] = ((sjg.xml.Element) e.nextElement()).getAttribute("d").getValue();
            i++;
        }
        elementFactory = ef;
        init();
    }
}
