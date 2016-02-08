import sjg.*;
import sjg.animation.*;
import sjg.scripting.*;
import sjg.maze.*;
import sjg.hiscore.*;

import java.awt.*;
import java.util.*;

/**
 * Taleban vs. Robot
 *
 * @author Christian Hvid 2002-2004
 */

public class Taleban extends SJGame {
    final Color BG_COLOR = new Color(151, 160, 150);
    ScriptEngine scripts;
    PlayerSprite playerSprite;
    Cropper cropper;
    Mazes mazes;
    Maze maze;
    String savePoint;
    String nextLevel;
    Hiscore hiscore;

    int level, levelFrameCount;
    int lives, score;
    int goldEaten, goldCount;

    public String getConfigurationFileName() {
        return "taleban.txt";
    }

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

	    if (maze.getWidth()+16 > getWidth()) {
		int dx = (int) (playerSprite.getX() - cameraX);
		int cx = getWidth() / 5;
		if (dx > cx)
		    cameraX += dx - cx;
		if (dx < -cx)
		    cameraX += dx + cx;
		if (cameraX < getWidth()/2-8) cameraX = getWidth()/2-8;
		if (cameraX > maze.getWidth() - getWidth()/2+8) cameraX = maze.getWidth() - getWidth()/2+8;
	    }
	    if (maze.getHeight()+16 > getHeight()) {
		int dy = (int) (playerSprite.getY() - cameraY);
		int cy = getHeight() / 5;
		if (dy > cy)
		    cameraY += dy - cy;
		if (dy < -cy)
		    cameraY += dy + cy;

		if (cameraY < getHeight()/2-8) cameraY = getHeight()/2-8;
		if (cameraY > maze.getHeight() - getHeight()/2 + 8) cameraY = maze.getHeight() - getHeight()/2 + 8;
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

    private void setBomb(int x, int y, int size) {
        sprites.add(new Bomb(size), x, y);
    }

    class Bomb extends SJGSpriteFA {
        int size;

        public Bomb(int size) {
            this.size = size;
            setAnimation(cropper.getAnimation("bomb"));
        }

        int count = 20;

        public void move() {
            count--;
            nextFrame();
            if (count < 0) {
                sprites.remove(this);
                sprites.add(new BombSplit(8, 0), getX(), getY());
                sprites.add(new BombSplit(-8, 0), getX(), getY());
                sprites.add(new BombSplit(0, 8), getX(), getY());
                sprites.add(new BombSplit(0, -8), getX(), getY());
            }
        }
    }

    class BombSplit extends SJGSpriteFA {
        int dx, dy;

        public BombSplit(int dx, int dy) {
            setAnimation(cropper.getAnimation("bombsplit"));
            this.dx = dx;
            this.dy = dy;
        }

        int count = 8;

        public void move() {
            nextFrame();
            translate(dx, dy);
            count--;
            if (count < 0)
                sprites.remove(this);
            else if (collidesWith(playerSprite))
                playerSprite.die();
            else {
                for (Enumeration e = sprites.elements(); e.hasMoreElements();) {
                    SJGSprite sprite = (SJGSprite) e.nextElement();
                    if ((collidesWith(sprite)) && ((sprite instanceof Robot) || (sprite instanceof Fish))) {
                        sprites.remove(sprite);
                        sprites.remove(this);
                        sprites.add(new Explosion("explosion_fish"), sprite.getX(), sprite.getY());
                        score += 10;
                        // sprites.add(new Explosion(getX(), getY());
                        return;
                    }
                }
                maze.touch(this);
            }
        }
    }

    class Explosion extends SJGSpriteFA {
        Explosion(String explosion) {
            setAnimation(cropper.getAnimation(explosion));
        }

        public void move() {
            nextFrame();
            if (getFrame() >= 4) sprites.remove(this);
        }
    }

    class PlayerSprite extends SJGSprite {
        final static int ALIVE = 0;
        final static int DYING = 1;
        final static int EXITING = 2;
        int bomb = 0;
        private int bombSize = 3;
        private int count = 0;
        private int status = ALIVE;
        private Animation animation = cropper.getAnimation("bush");
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
                        g.drawImage(animation.getFrame(5).getImage(),
                                view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2,
                                view.worldToRealY(getY()) - animation.getFrame(0).getHeight() + 16, null);
                        break;
                    case -1:
                        g.drawImage(animation.getFrame(0).getImage(),
                                view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2,
                                view.worldToRealY(getY()) - animation.getFrame(0).getHeight() + 16, null);
                        break;
                    case 1:
                        g.drawImage(animation.getFrame(count % 4 + 1).getImage(),
                                view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2,
                                view.worldToRealY(getY()) - animation.getFrame(0).getHeight() + 16 - count % 3, null);
                        break;
                    case 2:
                        g.drawImage(animation.getFrame(count % 4 + 6).getImage(),
                                view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2,
                                view.worldToRealY(getY()) - animation.getFrame(0).getHeight() + 16 - count % 3, null);
                        break;
                }
            }
            if (status == DYING) {
                g.drawImage(animation.getFrame(Math.min(count, 7) + 10).getImage(),
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
                if ((status == EXITING) && (count > 8)) {
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

            if (bomb > 0) bomb--;

            if (ks.isDown(32) && (bomb == 0)) {
                setBomb(getX(), getY(), bombSize);
                bomb = 16;
            }
            maze.touch(this);
        }

        void die() {
            if (status == ALIVE) {
                showScreen(die);
                status = DYING;
                count = 0;
            }
        }

        void exit() {
            if (status == ALIVE) {
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
            if (count > 20) sprites.remove(this);
        }

        public void draw(Graphics g, View view) {
            int sw = (g.getFontMetrics().stringWidth(s) * Math.min(count, 3)) / 3;
            int sh = g.getFontMetrics().getHeight();
            int x = view.worldToRealX(getX()) - sw / 2 - 3;
            int y = view.worldToRealY(getY()) - sh / 2 - 1;
            g.setColor(new Color(217, 214, 135));
            g.fillRect(x, y, sw + 6, sh + 2);
            g.setColor(Color.black);
            g.drawRect(x, y, sw + 6, sh + 2);
            if (count > 3) g.drawString(s, x + 3, y + sh - 2);
        }
    }

    class Fish extends SJGSprite {
        private int count = 0;
        private Animation animation = cropper.getAnimation("fish");
        private int dx = 0;
        private int dy = 0;
        private int d = 0;

        public void move() {
            if (count == 0) {
                int od = d;
                do {
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
                } while ((maze.canMove(this, getX() + dx, getY() + dy) == false) || ((Math.random() > .1) && (d != od)));
            }
            switch (count) {
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
            if (count >= 8) count = 0;
            if (collidesWith(playerSprite)) playerSprite.die();
        }

        public void draw(Graphics g, View view) {
            Image i = null;
            switch (count) {
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

    class Robot extends SJGSprite {
        private int count = 0;
        private Animation animation = cropper.getAnimation("robot");
        private int dx = 0;
        private int dy = 0;
        private int d = 0;
        private int looking = 0;
        private int bombed = 0;

        public void move() {
            if (count == 0) {
                int od = d;
                int choices = 4;
                double ndl = 0.05;
                if (bombed + 25 < getFrameCount()) {
                    choices = 6;
                    ndl = 0.2;
                }
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
                } while ((maze.canMove(this, getX() + dx, getY() + dy) == false) || ((Math.random() > ndl) && (d != od)));
                if ((od == 4) && (d != 4) && (Math.random() < 0.8)) {
                    setBomb(getX(), getY(), 3);
                    bombed = getFrameCount();
                }
                if (d == 0) looking = 0;
                if (d == 2) looking = 1;
            }
            translate(dx * 4, dy * 4);
            count++;
            if (count >= 8) count = 0;
            if (collidesWith(playerSprite)) playerSprite.die();
        }

        public void draw(Graphics g, View view) {
            Image i = null;
            if (d == 4)
                i = animation.getFrame(0).getImage();
            else
                i = animation.getFrame(1 + 4 * looking + count / 2).getImage();
            g.drawImage(i, view.worldToRealX(getX()) - 16,
                    view.worldToRealY(getY()) - 34, null);
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
        }
    };

    Screen die = new Screen() {
        public void enter() {
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
                if (lives > 0)
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
            g.drawImage(cropper.getFrame("gameover").getImage(), 40, 140, null);
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
	    public void draw() {
		Graphics g = getGraphicsModel().getFront();
		g.setColor(BG_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(cropper.getFrame("logo").getImage(), 10, 20, null);

		g.setColor(new Color(255, 251, 222));
		g.fillRect(200, 18, 320, 230);
		g.setColor(Color.black);
		g.drawRect(200, 18, 320, 230);
		g.drawRect(201, 19, 318, 228);
		g.drawImage(cropper.getFrame("hiscore").getImage(), 274, 24, null);

		String s = "NEW HISCORE - WRITE YOUR NAME AND PRESS RETURN";

		int sw = g.getFontMetrics().stringWidth(s);
		int sh = g.getFontMetrics().getHeight();

		g.setColor(new Color(246, 228, 128));
		g.fillRect(26, 306, sw+16, sh+8);
		g.setColor(Color.black);
		g.drawRect(26, 306, sw+16, sh+8);
		g.drawRect(27, 307, sw+14, sh+6);

		g.drawString(s, 34, 307+sh);

		hiscore.drawHiscoreEditor(g);

		drawScore(g);
	    }
	    public void move() {
		if (hiscore.moveHiscoreEditor() == false) showScreen(opening);
	    }
	};

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
            if (getFramesSinceEnter() > 20)
                showScreen(game);
        }

        public void enter() {
            sprites.removeAll();
            scripts.clear();
            scripts.spawn(savePoint);
            maze = mazes.getMaze(savePoint);
            maze.init();
            goldCount = maze.count(gold);
            goldEaten = 0;

            playerSprite = null;

            scripts.setCallback(new Callback() {
                public void command(String name, String text) {
                    if (name.equals("nextlevel"))
                        nextLevel = text;
                    if (name.equals("sign"))
                        sprites.add(new Sign(text), playerSprite.getX(), playerSprite.getY() - 50);
                }

                public void command(String name, int x, int y) {
                    if (name.equals("fish"))
                        sprites.add(new Fish(), x, y);
                    else if (name.equals("robot"))
                        sprites.add(new Robot(), x, y);
                    else if (name.equals("player") && (playerSprite == null))
                        sprites.add(playerSprite = new PlayerSprite(), x, y);
                }
            });

            scripts.move();
            view.reset();
        }
    };

    Screen opening = new Screen() {
	    int enterAt;
	    
	    public void enter() {
		enterAt = getFrameCount();
		getLocalPlayer().getMouseState().countLeft();
	    }
	    
	    private void drawHiscore(int count) {
		Graphics g = getGraphicsModel().getFront();

		if (count < 5) {
		    g.setColor(new Color(255, 251, 222));
		    g.fillRect(200, 18, 64*count+2, 44*count+2);
		    g.setColor(Color.black);
		    g.drawRect(200, 18, 64*count+2, 44*count+2);
		    g.drawRect(201, 19, 64*count, 44*count);
		} else {
		    g.setColor(new Color(255, 251, 222));
		    g.fillRect(200, 18, 320, 230);
		    g.setColor(Color.black);
		    g.drawRect(200, 18, 320, 230);
		    g.drawRect(201, 19, 318, 228);
		    g.drawImage(cropper.getFrame("hiscore").getImage(), 274, 24, null);
		    hiscore.drawHiscore(getGraphicsModel().getFront());
		}
	    }
	    
	    public void draw() {
		Graphics g = getGraphicsModel().getFront();
		g.setColor(BG_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(cropper.getFrame("logo").getImage(), 10, 20, null);
		
		if (enterAt + 20 < getFrameCount())
		    g.drawImage(cropper.getFrame("talebaner").getImage(), Math.min(192, 40 * (getFrameCount() - 20 - enterAt)) - 192, 140, null);
		
		if (enterAt + 50 < getFrameCount())
		    drawHiscore(getFrameCount()-enterAt-50);

		if (enterAt + 30 < getFrameCount())
		    g.drawImage(cropper.getFrame("bigrobot").getImage(), 235, 143 + 400 - Math.min(400, 40 * (getFrameCount() - 30 - enterAt)), null);

		if (enterAt + 40 < getFrameCount())
		    g.drawImage(cropper.getFrame("clickhere").getImage(), 300 + 400 - Math.min(400, 40 * (getFrameCount() - 40 - enterAt)), 320, null);

		drawScore(g);
	    }
	    
	    public void move() {
		MouseState ms = getLocalPlayer().getMouseState();
		if ((within(ms.getX(), ms.getY(), 300, 320, 230, 62)) && (ms.countLeft() > 0))
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
            g.drawImage(cropper.getFrame("" + k).getImage(), 534 - j * 16 + 8 - cropper.getFrame("" + k).getWidth() / 2, 10, null);

        }

        for (int i = 0; i < 5; i++)
            if (i < lives)
                g.drawImage(cropper.getFrame("heart").getImage(), 10 + i * 28, 8, null);
    }

    Element wall_brick = new Element() {
        public boolean isSolid() {
            return true;
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(cropper.getFrame("brick").getImage(), x, y, null);
        }
    };

    Element wall_block = new Element() {
        public boolean isSolid() {
            return true;
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(cropper.getFrame("block").getImage(), x, y, null);
        }
    };

    Element ground = new Element() {
        public boolean isSolid() {
            return false;
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(cropper.getFrame("ground").getImage(), x, y, null);
        }
    };

    Element exit_door = new Element() {
        public boolean isSolid() {
            return false;
        }

        public void draw(Graphics g, int x, int y) {
            if (goldEaten == goldCount)
                g.drawImage(cropper.getFrame("exit").getImage(), x, y, null);
            else
                g.drawImage(cropper.getFrame("ground").getImage(), x, y, null);
        }

        public Element touch(SJGSprite sprite, int x, int y) {
            if ((sprite instanceof PlayerSprite) && (sprite.getX() % 32 == 16) &&
                    ((sprite.getY()) % 32 == 16) && (goldEaten == goldCount))
                playerSprite.exit();
            return this;
        }
    };

    Element gold = new Element() {
        public boolean isSolid() {
            return false;
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(cropper.getFrame("gold").getImage(), x, y, null);
        }

        public Element touch(SJGSprite sprite, int x, int y) {
            if (sprite instanceof PlayerSprite) {
                score += 100;
                sprites.add(new Explosion("explosion_gold"), x + 16, y + 16);
                goldEaten++;
                if (goldEaten == goldCount)
                    sprites.add(new Sign(" GO FIND EXIT "), playerSprite.getX(), playerSprite.getY() - 50);
                return ground;
            } else
                return this;
        }
    };

    Element ice[] = {new Ice(1), new Ice(2), new Ice(3), ground};

    class Ice extends Element {
        int s;

        Ice(int s) {
            this.s = s;
        }

        public boolean isSolid() {
            return true;
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(cropper.getFrame("ice" + s).getImage(), x, y, null);
        }

        public Element touch(SJGSprite sprite, int x, int y) {
            if (sprite instanceof BombSplit) {
                sprites.remove(sprite);
                if (s == 3)
                    sprites.add(new Explosion("explosion_blue"), x + 16, y + 16);
                return ice[s];
            }
            return this;
        }
    }

    class Barrel extends Element {
	Element turnsTo;
	Barrel (Element turnsTo) {
	    this.turnsTo = turnsTo;
	}
        public boolean isSolid() {
            return true;
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(cropper.getFrame("barrel").getImage(), x, y, null);
        }

        public Element touch(SJGSprite sprite, int x, int y) {
            if (sprite instanceof BombSplit) {
                sprites.add(new BombSplit(8, 0), x + 16, y + 16);
                sprites.add(new BombSplit(-8, 0), x + 16, y + 16);
                sprites.add(new BombSplit(0, 8), x + 16, y + 16);
                sprites.add(new BombSplit(0, -8), x + 16, y + 16);
                sprites.remove(sprite);
                return turnsTo;
            }
            return this;
        }
    }

    Element barrel = new Barrel(ground);
    Element barrelNull = new Barrel(NullElement.getInstance());

    public void init() {
        try {
            Thread.sleep(250);
        } catch (Exception e) {
            throw new Error(e + "");
        }
        setGraphicsModel(new sjg.gm.Buffered(this));
        showScreen(opening);
        cropper = new Cropper(this);
        scripts = new ScriptEngine(this);
        mazes = new Mazes(this, new ElementFactory() {
            public Element getElement(char c) {
                switch (c) {
                    case 'W':
                        return wall_brick;
                    case 'w':
                        return wall_block;
                    case 'b':
                        return barrel;
                    case 'B':
                        return barrelNull;
                    case '.':
                        return ground;
                    case '*':
                        return gold;
                    case 'E':
                        return exit_door;
                    case 'i':
                        return ice[0];
                }
                return NullElement.getInstance();
            }
        });
	hiscore = new Hiscore(this, new HttpGetHiscoreServer(this, "/hiscore/list-hiscores.php", "/hiscore/add-hiscore.php")) {
		public void drawHiscoreText(int position, String positionText, String entryText, int score, String round) {
		    Graphics g = getGraphicsModel().getFront();
		    if (getScreen() == opening)
			g.setColor(Color.black);
		    else
			g.setColor(new Color(90, 90, 90));
		    int y = position*20+54;
		    g.drawString(positionText, 205, y);
		    g.drawString(entryText, 243, y);
		    g.drawString(""+score, 474, y);
		    g.drawString(round, 456, y);
		}
		public void drawWriteHiscoreText(int position, String positionText, String entryText, int score, String round) {
		    Graphics g = getGraphicsModel().getFront();
		    g.setColor(Color.black);
		    int y = position*20+54;
		    g.drawString(positionText, 205, y);
		    g.drawString(entryText, 243, y);
		    g.drawString(""+score, 476, y);
		    g.drawString(round, 456, y);
		}
	    };
    }
}
