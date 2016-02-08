import sjg.KeyboardState;
import sjg.MouseState;
import sjg.SJGame;
import sjg.Screen;
import sjg.animation.*;
import sjg.hiscore.Hiscore;
import sjg.hiscore.HttpGetHiscoreServer;
import sjg.maze.*;
import sjg.scripting.Callback;
import sjg.scripting.ScriptEngine;
import sjg.synth.SynthManager;

import java.awt.*;
import java.util.Enumeration;

/**
 * Mr. Hopwit and the mysterious maze
 *
 * @author Christian Hvid 2004
 */

public class Hopwit extends SJGame {
    public String getConfigurationFileName() {
        return "hopwit.txt";
    }

    final Color BG_COLOR = new Color(75, 86, 105);

    ScriptEngine scripts;
    PlayerSprite playerSprite;
    Cropper cropper;
    Mazes mazes;
    Maze maze;
    String savePoint;
    String nextLevel;
    Hiscore hiscore;

    int starCount;

    SynthManager synthManager;

    int level, levelFrameCount;
    int lives, score;
    int bonus;

    String findExitText;
    String skin = "blue";

    class TView implements View {
        private double cameraX;
        private double cameraY;

        public int worldToRealX(double x) {
            return (int) x - (int) cameraX + getWidth() / 2;
        }

        public int worldToRealY(double y) {
            return (int) y - (int) cameraY + getHeight() / 2;
        }

        public double realToWorldX(int x) {
            return x + cameraX - getWidth() / 2;
        }

        public double realToWorldY(int y) {
            return y + cameraY - getHeight() / 2;
        }

        public void move() {
            if (playerSprite == null) return;

            if (maze.getWidth() + 16 > getWidth()) {
                int dx = (int) (playerSprite.getX() - cameraX);
                int cx = getWidth() / 5;
                if (dx > cx)
                    cameraX += dx - cx;
                if (dx < -cx)
                    cameraX += dx + cx;
                if (cameraX < getWidth() / 2 - 8) cameraX = getWidth() / 2 - 8;
                if (cameraX > maze.getWidth() - getWidth() / 2 + 8) cameraX = maze.getWidth() - getWidth() / 2 + 8;
            }
            if (maze.getHeight() + 16 > getHeight()) {
                int dy = (int) (playerSprite.getY() - cameraY);
                int cy = getHeight() / 5;
                if (dy > cy)
                    cameraY += dy - cy;
                if (dy < -cy)
                    cameraY += dy + cy;

                if (cameraY < getHeight() / 2 - 8) cameraY = getHeight() / 2 - 8;
                if (cameraY > maze.getHeight() - getHeight() / 2 + 8) cameraY = maze.getHeight() - getHeight() / 2 + 8;
            }
        }

        public void reset() {
            cameraX = maze.getWidth() / 2;
            cameraY = maze.getHeight() / 2;
            move();
        }
    }

    TView view = new TView();

    Sprites sprites = new Sprites(view);

    class PlayerSprite extends SJGSprite {
        final static int ALIVE = 0;
        final static int DYING = 1;
        final static int EXITING = 2;
        private int count = 0;
        private int status = ALIVE;
        private Animation animation = cropper.getAnimation("player");
        private int d = -1;
        private int primaryDirection = -1;
        private int secondaryDirection = -1;
        private double speed = 32.0 / 3.0;

        private boolean keyb[] = {false, false, false, false};

        public int getWidth() {
            return 32;
        }

        public int getHeight() {
            return 32;
        }

        public void draw(Graphics g, View view) {
            if (status == ALIVE) {
                switch (d) {
                    case -2:
                        g.drawImage(animation.getFrame(4).getImage(),
                                view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2,
                                view.worldToRealY(getY()) - animation.getFrame(0).getHeight() + 16, null);
                        break;
                    case -1:
                        g.drawImage(animation.getFrame(0).getImage(),
                                view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2,
                                view.worldToRealY(getY()) - animation.getFrame(0).getHeight() + 16, null);
                        break;
                    case 1:
                        g.drawImage(animation.getFrame(count % 4 + 0).getImage(),
                                view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2,
                                view.worldToRealY(getY()) - animation.getFrame(0).getHeight() + 16 - count % 3, null);
                        break;
                    case 2:
                        g.drawImage(animation.getFrame(count % 4 + 4).getImage(),
                                view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2,
                                view.worldToRealY(getY()) - animation.getFrame(0).getHeight() + 16 - count % 3, null);
                        break;
                }
            }
            if (status == DYING) {
                g.drawImage(animation.getFrame(Math.min(count, 22) + 10).getImage(),
                        view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2,
                        view.worldToRealY(getY()) - animation.getFrame(0).getHeight() + 16, null);
            }
            if (status == EXITING) {
                if (count < 16)
                    g.drawImage(animation.getFrame((count / 4) % 2 + 8).getImage(),
                            view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2,
                            view.worldToRealY(getY()) - animation.getFrame(0).getHeight() + 16, null);
                else
                    g.drawImage(animation.getFrame(8).getImage(),
                            view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2,
                            view.worldToRealY(getY()) - animation.getFrame(0).getHeight() + 16, null);
            }
        }

        final int sx[] = {-1, 1, 0, 0};
        final int sy[] = {0, 0, -1, 1};

        private boolean canMove(int i) {
            return maze.canMove(this, getX() + sx[i] * speed, getY() + sy[i] * speed);
        }

        private boolean canMoveSec(int primary, int via) {
            if (maze.canMove(this, getX() + sx[via] * speed, getY() + sy[via] * speed)) {
                translate(sx[via] * speed, sy[via] * speed);
                boolean result = maze.canMove(this, getX() + sx[primary] * speed, getY() + sy[primary] * speed);
                translate(sx[via] * speed, sy[via] * speed);
                result |= maze.canMove(this, getX() + sx[primary] * speed, getY() + sy[primary] * speed);
                translate(-2 * sx[via] * speed, -2 * sy[via] * speed);
                return result;
            } else {
                return false;
            }
        }

        private void move(int i) {
            if (i == 0)
                d = 2;
            else if (i == 1)
                d = 1;
            else
                d = Math.abs(d);
            count++;
            translate(speed * sx[i], speed * sy[i]);
        }

        public void move() {
            // showStatus(getX() + ", " + getY());
            if (status != ALIVE) {
                count++;
                if ((status == EXITING) && (count > 32)) {
                    savePoint = nextLevel;
                    showScreen(enterGame);
                }
                return;
            }
            d = -Math.abs(d);

            d = -Math.abs(d);

            KeyboardState ks = getLocalPlayer().getKeyboardState();
            boolean oldk[] = new boolean[4];
            oldk[0] = keyb[0];
            oldk[1] = keyb[1];
            oldk[2] = keyb[2];
            oldk[3] = keyb[3];
            keyb[0] = ks.isDown(37);
            keyb[1] = ks.isDown(39);
            keyb[2] = ks.isDown(38);
            keyb[3] = ks.isDown(40);
            for (int i = 0; i < 4; i++) if ((keyb[i] == true) && (oldk[i] == false)) primaryDirection = i;
            for (int i = 0; i < 4; i++) if ((keyb[i] == true) && (primaryDirection != i)) secondaryDirection = i;

            boolean move = false;
            for (int i = 0; i < 4; i++) move |= keyb[i];

            if (move == true) {
                if ((primaryDirection != -1) && canMove(primaryDirection))
                    move(primaryDirection);
                else if ((secondaryDirection != -1) && canMove(secondaryDirection))
                    move(secondaryDirection);
                else {
                    switch (primaryDirection) {
                        case 0:
                            if (canMoveSec(0, 2))
                                move(2);
                            else if (canMoveSec(0, 3)) move(3);
                            break;
                        case 1:
                            if (canMoveSec(1, 2))
                                move(2);
                            else if (canMoveSec(1, 3)) move(3);
                            break;
                        case 2:
                            if (canMoveSec(2, 0))
                                move(0);
                            else if (canMoveSec(2, 1)) move(1);
                            break;
                        case 3:
                            if (canMoveSec(3, 0))
                                move(0);
                            else if (canMoveSec(3, 1)) move(1);
                            break;
                    }

                }
            } else {
                primaryDirection = -1;
                secondaryDirection = -1;
                if (status == ALIVE) count = 0;
            }

            if ((secondaryDirection > -1) && (keyb[secondaryDirection] == false))
                secondaryDirection = -1;

            if ((primaryDirection > -1) && (keyb[primaryDirection] == false))
                if (secondaryDirection != -1) primaryDirection = secondaryDirection;

            maze.touch(this);
        }

        void die() {
            if (status == ALIVE) {
                showScreen(die);
                status = DYING;
                count = 0;
                synthManager.startTrack("die");
            }
        }

        void exit() {
            if (status == ALIVE) {
                synthManager.stopTrack("bassline-1");
                synthManager.stopTrack("bassline-2");
                synthManager.stopTrack("bassline-1-exit");
                synthManager.stopTrack("bassline-2-exit");
                status = EXITING;
                count = 0;
            }
        }
    }

    class Sign extends SJGSprite {
        private int count = 0;
        private String s;

        Sign(String s) {
            this.s = s;
        }

        public void move() {
            count++;
            if (count > 30) sprites.remove(this);
        }

        public void draw(Graphics g, View view) {
            int sw = (s.length() * 8 * Math.min(count, 3)) / 3 + 12;
            int sh = 10;
            int x = view.worldToRealX(getX()) - sw / 2 - 3;
            int y = view.worldToRealY(getY()) - sh / 2 - 1;
            g.setColor(new Color(30, 80, 140));
            g.fillRect(x, y, sw + 6, sh + 3);
            g.setColor(Color.black);
            g.drawRect(x, y, sw + 6, sh + 3);
            if (count > 3) drawText(g, x + 9, y + 2, "", s);
        }
    }

    class Bubble extends SJGSprite {
        private int count = 0;
        private Animation animation = cropper.getAnimation("bubble");
        private int dx = 0;
        private int dy = 0;
        private int d = 0;
        private int pause;

        public Bubble() {
        }

        public Bubble(int pause) {
            this.pause = pause;
        }

        public void move() {
            if (count < pause) {
            } else if (count % 8 == 0) {
                int od = d;
                do {
                    if (Math.random() < 0.85) {
                        // chase player

                        if ((Math.random() > 0.5) && (playerSprite.getX() != this.getX())) {
                            // horizontal
                            if (playerSprite.getX() > this.getX()) {
                                dx = 1;
                                dy = 0;
                                d = 0;
                            } else {
                                dx = -1;
                                dy = 0;
                                d = 2;
                            }
                        } else {
                            // vertical
                            if (playerSprite.getY() > this.getY()) {
                                dx = 0;
                                dy = 1;
                                d = 1;
                            } else {
                                dx = 0;
                                dy = -1;
                                d = 3;
                            }
                        }
                    } else {
                        switch ((int) (4 * Math.random())) {
                            case 0:
                                dx = 1;
                                dy = 0;
                                d = 0;
                                break;
                            case 1:
                                dx = 0;
                                dy = 1;
                                d = 1;
                                break;
                            case 2:
                                dx = -1;
                                dy = 0;
                                d = 2;
                                break;
                            case 3:
                                dx = 0;
                                dy = -1;
                                d = 3;
                                break;
                        }
                    }
                }
                while ((maze.canMove(this, getX() + dx, getY() + dy) == false) || ((Math.random() > .1) && (d != od)));
            }

            if (playerSprite.status != PlayerSprite.EXITING)
                switch (count % 8) {
                    case 0:
                    case 1:
                        translate(dx * 0, dy * 0);
                        break;
                    case 2:
                    case 3:
                    case 4:
                        translate(dx * 1, dy * 1);
                        break;
                    case 5:
                        translate(dx * 5, dy * 5);
                        break;
                    case 6:
                        translate(dx * 9, dy * 9);
                        break;
                    case 7:
                        translate(dx * 15, dy * 15);
                        break;
                }

            count++;
            if (collidesWith(playerSprite)) playerSprite.die();
            maze.touch(this);
        }

        public void draw(Graphics g, View view) {
            Image i = null;
            switch (count % 8) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    i = animation.getFrame(0 + d * 3).getImage();
                    break;
                case 5:
                case 6:
                    i = animation.getFrame(1 + d * 3).getImage();
                    break;
                case 7:
                    i = animation.getFrame(2 + d * 3).getImage();
                    break;
            }
            g.drawImage(i, view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2,
                    view.worldToRealY(getY()) - animation.getFrame(0).getHeight() / 2, null);
        }
    }

    class Stardust extends SJGSpriteFA {
        private int count;

        public Stardust(String color) {
            setAnimation(cropper.getAnimation(color + "-stardust"));
        }

        public void move() {
            nextFrame();
            count ++;
            translate((int) (Math.random() * 9 - 4), 2 - (int) (Math.random() * 8));
            if (count > 8) sprites.remove(this);
        }
    }

    class Star extends SJGSprite {
        private Animation animation;
        private int frame;
        private double dx;
        private double dy;
        private int count;
        private String color;

        private void newDirection() {
            dx = Math.random() * 20 - 10;
            dy = Math.random() * 20 - 10;
            synthManager.startTrack("star");
        }

        public int getWidth() {
            return animation.getFrame(frame).getWidth();
        }

        public int getHeight() {
            return animation.getFrame(frame).getHeight();
        }

        public Star(String color) {
            animation = cropper.getAnimation(color + "-star");
            dx = Math.random() - 0.5;
            dy = Math.random() - 0.5;
            starCount ++;
            this.color = color;
        }

        public void draw(Graphics g, View view) {
            double x;
            double y;
            if (getScreen() == enterGame) {
                double d = Math.pow(enterGameCompletion, 0.6);
                double dx = getX() - playerSprite.getX();
                double dy = getY() - playerSprite.getY();
                double dx1 = dy * (-Math.sin(1 - d)) + dx * Math.cos(1 - d);
                double dy1 = dy * Math.cos(1 - d) + dx * Math.sin(1 - d);

                x = playerSprite.getX() + dx1 * d;
                y = playerSprite.getY() + dy1 * d;
                count ++;
                if (count % 4 == 0) frame ++;

            } else {
                x = getX();
                y = getY();
            }
            g.drawImage(animation.getFrame(frame).getImage(),
                    view.worldToRealX(x) - animation.getFrame(frame).getWidth() / 2,
                    view.worldToRealY(y) - animation.getFrame(frame).getHeight() / 2,
                    null);
        }

        public void move() {
            count ++;
            if (count % Math.max(1, (5 - ((int) Math.pow(dx * dx + dy * dy, 0.5) / 2))) == 0)
                frame ++;

            if (maze.canMove(this, getX() + dx, getY() + dy) == false) {
                if (maze.canMove(this, getX() - dx, getY() + dy) == true)
                    dx = -dx;
                else if (maze.canMove(this, getX() + dx, getY() - dy) == true)
                    dy = -dy;
                else {
                    dx = -dx;
                    dy = -dy;
                }
                dx *= 0.9;
                dy *= 0.9;
            }
            translate(dx, dy);
            for (Enumeration e = sprites.elements(); e.hasMoreElements();) {
                SJGSprite sprite = (SJGSprite) e.nextElement();
                if (collidesWith(sprite)) {
                    if (sprite instanceof ClayMan) newDirection();
                    if (sprite instanceof Ghost) newDirection();
                    if (sprite instanceof Bubble) newDirection();
                    if (sprite instanceof PlayerSprite) {
                        sprites.remove(this);

                        if (color.equals("red")) score += 250;
                        if (color.equals("blue")) score += 350;

                        sprites.add(new Stardust(color), getX(), getY());
                        starCount --;
                        if (starCount == 0) {
                            sprites.add(new Sign(findExitText), playerSprite.getX(), playerSprite.getY() - 28);
                            synthManager.startTrack("find-exit");
                            synthManager.stopTrack("bassline-1");
                            synthManager.stopTrack("bassline-2");
                            synthManager.startTrack("bassline-1-exit");
                            synthManager.startTrack("bassline-2-exit");

                        } else {
                            synthManager.startTrack("point");
                        }
                    }
                }
            }
            maze.touch(this);
        }
    }

    class ClayMan extends SJGSprite {
        private int count = 0;
        private Animation animation = cropper.getAnimation("clay-man");
        private int dx = 0;
        private int dy = 0;
        private int d = 0;
        private int looking = 0;
        private int pause;

        ClayMan() {
        }

        ClayMan(int pause) {
            this.pause = pause;
        }

        public void move() {
            if (count < pause) {
            } else if (count % 4 == 0) {
                int od = d;
                int choices = 4;
                double ndl = 0.2;

                if ((playerSprite.getX() == getX()) || (playerSprite.getY() == getY())) ndl = 0.4;

                do {
                    if (Math.random() < 0.65) {
                        // chase player

                        if ((Math.random() > 0.5) && (playerSprite.getY() != this.getY())) {
                            // vertical
                            if (playerSprite.getY() > this.getY()) {
                                dx = 0;
                                dy = 1;
                                d = 1;
                            } else {
                                dx = 0;
                                dy = -1;
                                d = 3;
                            }
                        } else {
                            // horizontal
                            if (playerSprite.getX() > this.getX()) {
                                dx = 1;
                                dy = 0;
                                d = 0;
                            } else {
                                dx = -1;
                                dy = 0;
                                d = 2;
                            }
                        }
                    } else {
                        switch ((int) (4 * Math.random())) {
                            case 0:
                                dx = 1;
                                dy = 0;
                                d = 0;
                                break;
                            case 1:
                                dx = 0;
                                dy = 1;
                                d = 1;
                                break;
                            case 2:
                                dx = -1;
                                dy = 0;
                                d = 2;
                                break;
                            case 3:
                                dx = 0;
                                dy = -1;
                                d = 3;
                                break;
                        }
                    }
                }
                while ((maze.canMove(this, getX() + dx, getY() + dy) == false) || ((Math.random() > ndl) && (d != od)));
                if (d == 0) looking = 0;
                if (d == 2) looking = 1;
            }

            if (playerSprite.status != PlayerSprite.EXITING)
                translate(dx * 8, dy * 8);

            count++;
            if (collidesWith(playerSprite)) playerSprite.die();
            maze.touch(this);
        }

        public void draw(Graphics g, View view) {
            Image i = null;
            if (d == 4)
                i = animation.getFrame(0).getImage();
            else
                i = animation.getFrame(4 * looking + count % 4).getImage();
            g.drawImage(i, view.worldToRealX(getX()) - 16,
                    view.worldToRealY(getY()) - 16, null);
        }
    }

    class Ghost extends SJGSprite {
        private int count = 0;
        private Animation animation = cropper.getAnimation("ghost");
        private int dx = 0;
        private int dy = 0;
        private int d = 0;
        private int pause;

        Ghost() {
        }

        Ghost(int pause) {
            this.pause = pause;
        }

        public void move() {
            if (count < pause) {
                // nada
            } else if (count % 4 == 0) {
                int od = d;
                int choices = 4;
                double ndl = 0.05;
                do {
                    switch ((int) (choices * Math.random())) {
                        case 0:
                            dx = 1;
                            dy = 0;
                            d = 0;
                            break;
                        case 1:
                            dx = 0;
                            dy = 1;
                            d = 1;
                            break;
                        case 2:
                            dx = -1;
                            dy = 0;
                            d = 2;
                            break;
                        case 3:
                            dx = 0;
                            dy = -1;
                            d = 3;
                            break;
                        case 4:
                        case 5:
                            dx = 0;
                            dy = 0;
                            d = 4;
                            break;
                    }
                }
                while ((maze.canMove(this, getX() + dx, getY() + dy) == false) || ((Math.random() > ndl) && (d != od)));
            }

            if (playerSprite.status != PlayerSprite.EXITING)
                translate(dx * 4, dy * 4);

            count++;
            if (collidesWith(playerSprite)) playerSprite.die();
            maze.touch(this);
        }

        public void draw(Graphics g, View view) {
            g.drawImage(animation.getFrame(count % 6).getImage(),
                    view.worldToRealX(getX()) - 16 - count % 3, view.worldToRealY(getY()) - 16 - count % 3, null);
        }
    }

    Screen game = new Screen() {
        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            g.setColor(BG_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
            maze.draw(g, view);
            sprites.draw(g);
            drawScore(g);
        }

        public void move() {
            scripts.move();
            sprites.move();
            view.move();

            if (bonus > 0) bonus -= 5;
            else bonus = 0;
        }

        public void enter() {
            synthManager.stopTrack("bassline-1-exit");
            synthManager.stopTrack("bassline-2-exit");
            synthManager.startTrack("bassline-1");
            synthManager.startTrack("bassline-2");
        }

        public void exit() {
            synthManager.stopTrack("bassline-1");
            synthManager.stopTrack("bassline-2");
            synthManager.stopTrack("bassline-1-exit");
            synthManager.stopTrack("bassline-2-exit");
        }
    };

    Screen die = new Screen() {
        public void enter() {
        }

        public void exit() {
            lives--;
        }

        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            g.setColor(BG_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
            maze.draw(g, view);
            sprites.draw(g);
            drawScore(g);
        }

        private double sigmoid(double s) {
            return 1 / (1 + Math.exp(-s));
        }

        public void move() {
            sprites.move();
            view.move();
            if (getFramesSinceEnter() > 40) {
                if (lives - 1 > 0)
                    showScreen(enterGame);
                else
                    showScreen(gameOver);
            }
        }
    };

    Screen gameOver = new Screen() {
        public void enter() {
        }

        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            g.setColor(BG_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
            maze.draw(g, view);
            sprites.draw(g);
            g.drawImage(cropper.getFrame("gameover").getImage(), 280 - cropper.getFrame("gameover").getWidth() / 2,
                    140, null);
            drawScore(g);
        }

        public void move() {
            view.move();
            sprites.move();
            if (getFramesSinceEnter() > 60)
                if (hiscore.registerScore(score, savePoint.substring(5)))
                    showScreen(writeHiscore);
                else
                    showScreen(opening);
        }
    };

    Screen writeHiscore = new Screen() {
        public void enter() {
            synthManager.setListenToKeyboard(false);
        }

        public void exit() {
            synthManager.setListenToKeyboard(true);
        }

        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            g.setColor(BG_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(cropper.getFrame("logo").getImage(),
                    280 - cropper.getFrame("logo").getWidth() / 2, 50, null);

            hiscore.drawHiscoreEditor(g);
            drawText(g, 142, 323, "", "WRITE YOUR NAME AND PRESS RETURN");

            drawScore(g);
        }

        public void move() {
            if (hiscore.moveHiscoreEditor() == false) showScreen(opening);
        }
    };

    double enterGameCompletion;

    Screen enterGame = new Screen() {
        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            g.setColor(BG_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
            maze.draw(g, view);
            sprites.draw(g);
            drawScore(g);
        }

        public void move() {
            enterGameCompletion = getFramesSinceEnter() / 21.0;
            if (getFramesSinceEnter() > 20)
                showScreen(game);
        }

        public void enter() {
            sprites.removeAll();
            scripts.clear();
            scripts.spawn(savePoint);

            synthManager.startTrack("intro");

            maze = mazes.getMaze(savePoint);
            maze.init();
            starCount = 0;
            bonus = 0;

            playerSprite = null;

            scripts.setCallback(new Callback() {
                public void command(String name, String text) {
                    if (name.equals("next-level"))
                        nextLevel = text;
                    if (name.equals("skin"))
                        skin = text;
                    if (name.equals("bonus")) {
                        try {
                            bonus = Integer.parseInt(text);
                        } catch (Exception e) {
                            throw new Error("" + e);
                        }
                    }
                    if (name.equals("find-exit-text"))
                        findExitText = text;
                    if (name.equals("sign"))
                        sprites.add(new Sign(text), playerSprite.getX(), playerSprite.getY() - 50);
                }

                public void command(String name, int x, int y) {
                    if (name.equals("bubble"))
                        sprites.add(new Bubble(), x, y);
                    else if (name.startsWith("bubble "))
                        sprites.add(new Bubble(Integer.parseInt(name.substring(7))), x, y);
                    else if (name.equals("clay-man"))
                        sprites.add(new ClayMan(), x, y);
                    else if (name.startsWith("clay-man "))
                        sprites.add(new ClayMan(Integer.parseInt(name.substring(9))), x, y);
                    else if (name.equals("ghost"))
                        sprites.add(new Ghost(), x, y);
                    else if (name.startsWith("ghost "))
                        sprites.add(new Ghost(Integer.parseInt(name.substring(6))), x, y);
                    else if (name.equals("red-star"))
                        sprites.add(new Star("red"), x, y);
                    else if (name.equals("blue-star"))
                        sprites.add(new Star("blue"), x, y);
                    else if (name.equals("player") && (playerSprite == null))
                        sprites.add(playerSprite = new PlayerSprite(), x, y);
                }
            });

            scripts.move();
            view.reset();
            closedGate = "red";
        }
    };

    Screen opening = new Screen() {
        public void enter() {
            getLocalPlayer().getMouseState().countLeft();
        }

        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            g.setColor(BG_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(cropper.getFrame("logo").getImage(),
                    280 - cropper.getFrame("logo").getWidth() / 2, 50, null);

            MouseState ms = getLocalPlayer().getMouseState();

            if (within(ms.getX(), ms.getY(), 135, 320, 289, 20))
                g.drawImage(cropper.getFrame("click-here-mouse-over").getImage(), 135, 320, null);
            else
                g.drawImage(cropper.getFrame("click-here").getImage(), 135, 320, null);

            drawScore(g);
            hiscore.drawHiscore(g);
        }

        public void move() {
            MouseState ms = getLocalPlayer().getMouseState();
            if ((within(ms.getX(), ms.getY(), 135, 320, 289, 20)) && (ms.countLeft() > 0))
                newGame();
        }
    };

    public void newGame() {
        level = 0;
        levelFrameCount = 0;
        score = 0;
        lives = 3;
        savePoint = "level1";
        showScreen(enterGame);
    }

    public void drawScore(Graphics g) {
        int p = 1;
        for (int j = 0; j < 5; j++) {
            int k = ((score / p) % 10);
            if (score < p)
                g.setColor(new Color(0, 51, 102));
            else
                g.setColor(new Color(0, 102, 153));

            p *= 10;
            g.drawImage(cropper.getFrame("L" + k).getImage(), 534 - j * 14 + 8 - cropper.getFrame("" + k).getWidth() / 2, 10, null);

        }

        for (int i = 0; i < 5; i++)
            if (i < lives - 1)
                g.drawImage(cropper.getFrame("heart").getImage(), 10 + i * 32, 8, null);
    }

    Element wall_block = new Element() {
        public boolean isSolid() {
            return true;
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(cropper.getFrame(skin + "-solid-1").getImage(), x, y, null);
        }
    };

    Element ground = new Element() {
        public boolean isSolid() {
            return false;
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(cropper.getFrame(skin + "-background-1").getImage(), x, y, null);
        }
    };

    Element exit_door = new Element() {
        public boolean isSolid() {
            return false;
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(cropper.getFrame(skin + "-background-1").getImage(), x, y, null);
            if (starCount == 0)
                g.drawImage(cropper.getAnimation("exit-sign").getFrame(getFrameCount()).getImage(), x, y, null);
        }

        public Element touch(SJGSprite sprite, int x, int y) {
            if ((sprite instanceof PlayerSprite) && (starCount == 0)) {
                playerSprite.exit();
                score += bonus;
                if (bonus > 0) sprites.add(new Sign("BONUS " + bonus), playerSprite.getX(), playerSprite.getY() - 28);
                synthManager.startTrack("completion");
            }
            return this;
        }
    };

    class ColorGate extends Element {
        int changeCount;
        String color;

        public ColorGate(String color) {
            this.color = color;
        }

        public boolean isSolid() {
            if (getFrameCount() - changeCount < 3) return false;
            return color.equals(closedGate);
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(cropper.getFrame(skin + "-background-1").getImage(), x, y, null);

            if (isSolid()) //(color.equals(closedGate))
                g.drawImage(cropper.getFrame(color + "-closed").getImage(), x, y, null);
            else
                g.drawImage(cropper.getAnimation(color).getFrame(getFrameCount() % 4).getImage(), x, y, null);

        }

        public Element touch(SJGSprite sprite, int x, int y) {
            if (sprite instanceof PlayerSprite) {
                String closedGateOld = closedGate;
                if (color.equals("red"))
                    closedGate = "green";
                if (color.equals("green"))
                    closedGate = "blue";
                if (color.equals("blue"))
                    closedGate = "red";

                if (closedGate.equals(closedGateOld) == false)
                    synthManager.startTrack("blop");

            } else {
                changeCount = getFrameCount();
            }
            return this;
        }
    }

    String closedGate;

    public void drawText(Graphics g, int x, int y, String font, String text) {
        int j = x;
        for (int i = 0; i < text.length(); i++) {
            g.drawImage(cropper.getFrame(font + text.charAt(i)).getImage(), j, y, null);
            j += cropper.getFrame(font + text.charAt(i)).getWidth();
        }
    }

    public void init() {
        synthManager = new SynthManager(this);
        setGraphicsModel(new sjg.gm.Buffered(this));
        showScreen(opening);
        cropper = new Cropper(this);
        scripts = new ScriptEngine(this);
        mazes = new Mazes(this, new ElementFactory() {
            public Element getElement(char c) {
                switch (c) {
                    case 'w':
                        return wall_block;
                    case '.':
                        return ground;
                    case 'r':
                        return new ColorGate("red");
                    case 'g':
                        return new ColorGate("green");
                    case 'b':
                        return new ColorGate("blue");
                    case 'E':
                        return exit_door;
                }
                return NullElement.getInstance();
            }
        });
        hiscore = new Hiscore(this, new HttpGetHiscoreServer(this, "/hiscore/list-hiscores.php", "/hiscore/add-hiscore.php")) {
            public void drawHiscoreText(int position, String positionText, String entryText, int score, String round) {
                Graphics g = getGraphicsModel().getFront();
                int y = 145 + position * 16;
                if (position == 9)
                    drawText(g, 117, y, "", positionText);
                else
                    drawText(g, 125, y, "", positionText);
                drawText(g, 165, y, "", entryText);
                drawText(g, 360, y, "", round);
                drawText(g, 395, y, "", "" + score);
            }

            public void drawWriteHiscoreText(int position, String positionText, String entryText, int score, String round) {
                drawHiscoreText(position, positionText, entryText, score, round);
            }
        };
    }
}
