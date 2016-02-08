// Erik vs. Erik
// Based on Triple Punch
// Christian Hvid 2002-2004

import sjg.*;
import sjg.animation.*;
import sjg.scripting.*;
import sjg.hiscore.*;
import sjg.maze.*;

import java.awt.*;
import java.util.*;

public class TriplePunch extends SJGame {
    Color fillColor[] = new Color[4];
    Color rimColor[] = new Color[4];
    Color bgColor = new Color(149, 28, 192);
    ScriptEngine scripts;
    PlayerSprite playerSprite;

    Cropper cropper;
    Mazes mazes;
    Maze maze;

    Hiscore hiscore;

    String savePoint;
    String nextLevel;

    int level, levelFrameCount;
    int lives, score;
    int blocksTaken, blocksCount;
    int gameStopped;

    public String getConfigurationFileName() {
        return "triple-punch.txt";
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

	    if (maze.getWidth() > getWidth()) {
		int dx = (int) (playerSprite.getX() - cameraX);
		int cx = getWidth() / 5;
		if (dx > cx)
		    cameraX += dx - cx;
		if (dx < -cx)
		    cameraX += dx + cx;
		if (cameraX < getWidth()/2) cameraX = getWidth()/2;
		if (cameraX > maze.getWidth() - getWidth()/2) cameraX = maze.getWidth() - getWidth()/2;
	    }
	    if (maze.getHeight() > getHeight()) {
		int dy = (int) (playerSprite.getY() - cameraY);
		int cy = getHeight() / 5;
		if (dy > cy)
		    cameraY += dy - cy;
		if (dy < -cy)
		    cameraY += dy + cy;

		if (cameraY < getHeight()/2) cameraY = getHeight()/2;
		if (cameraY > maze.getHeight() - getHeight()/2) cameraY = maze.getHeight() - getHeight()/2;
	    }
        }

        public void reset() {
            cameraX = maze.getWidth() / 2;
            cameraY = maze.getHeight() / 2;
            move();
        }
    };

    TView view = new TView();

    Sprites sprites = new Sprites(view);

    class StaticSprite extends SJGSpriteFA {
        StaticSprite(String animation) {
            setAnimation(cropper.getAnimation(animation));
        }

        public void move() {
            nextFrame();
        }
    }

    class PlayerSprite extends SJGSprite {
        final static int ALIVE = 0;
        final static int DYING = 1;
        final static int EXITING = 2;
        private int count = 0;
        private int status = ALIVE;
        private Animation animation = cropper.getAnimation("player");
        private Animation die_animation = cropper.getAnimation("player_die");
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

        private void drawFrame(Graphics g, View view, int i) {
            g.drawImage(animation.getFrame(i).getImage(),
                    view.worldToRealX(getX()) - animation.getFrame(i).getWidth() / 2,
                    view.worldToRealY(getY()) - animation.getFrame(i).getHeight() + 16, null);
        }

        public void draw(Graphics g, View view) {
            if (status == ALIVE) {
                if (primaryDirection == -1)
                    drawFrame(g, view, 0);
                else
                    drawFrame(g, view, primaryDirection * 8 + count % 8 + 1);
            }
            if ((status == DYING) && (count < die_animation.size())) {
                g.drawImage(die_animation.getFrame(count).getImage(),
                        view.worldToRealX(getX()) - animation.getFrame(count).getWidth() / 2,
                        view.worldToRealY(getY()) - animation.getFrame(count).getHeight() + 16, null);
            }
            if (status == EXITING) {
                if ((count / 4) % 2 == 0)
                    drawFrame(g, view, 0);
                else
                    drawFrame(g, view, 33);
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
	    //showStatus(getX()+", "+getY());
            if (status != ALIVE) {
                count++;
                if ((status == EXITING) && (count > 24)) {
                    savePoint = nextLevel;
                    showScreen(enterGame);
                }
                return;
            }
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
		    // cut the corners
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
            }
        }

        void exit() {
            if (status == ALIVE) {
                status = EXITING;
                count = 0;
            }
        }
    }

    class Ring extends SJGSprite {
        int count = 10;

        public void move() {
            count--;
            if (count < 0) sprites.remove(this);
        }

        public void draw(Graphics g, View view) {
            int x1 = view.worldToRealX(getX());
            int y1 = view.worldToRealY(getY());
            int xo = -1;
            int yo = 0;

            for (int i = 0; i < 20; i++) {
                double r = i * 0.31 + (i + count * 3) * 0.85;
                g.setColor(new Color(255 - i * 8, 255 - i * 5, 255));
                int x = x1 + (int) ((count * 4) * Math.sin(r));
                int y = y1 + (int) ((count * 4) * Math.cos(r));
                if (xo != -1) g.drawLine(x, y, xo, yo);
                xo = x;
                yo = y;
            }
        }
    }

    class PointsInBlock extends SJGSprite {
        int count = 15;
        int score;

        public PointsInBlock(int score) {
            this.score = score;
        }

        public void move() {
            count--;
            if (count < 0) sprites.remove(this);
        }

        public void draw(Graphics g, View view) {
            int x1 = view.worldToRealX(getX());
            int y1 = view.worldToRealY(getY() + count * 2 - 10);
            if ((count > 8) || (count % 2 == 0))
                drawText(g, x1, y1, "", "" + score);
        }
    }

    abstract class Enemy extends SJGSprite {
        public abstract void die();

        public abstract int getScore();
    }

    class Fish extends Enemy {
        private int count = 0;
        private Animation animation = cropper.getAnimation("ape");
        private int dx = 0;
        private int dy = 0;
        private int d = 0;
        private boolean morgen = true;

	Fish (boolean morgen) {
	    this.morgen = morgen;
	    if (morgen)
		animation = cropper.getAnimation("morgen-ape");
	}

        public int getScore() {
            if (morgen) return 1000;
	    else return 500;
        }

        public void die() {
            sprites.remove(this);
        }

        public void move() {
            if (gameStopped > 0) return;
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
                } while ((maze.canMove(this, getX() + dx * 1, getY() + dy * 1) == false) || ((Math.random() > .1) && (d != od)));
            }
            if (morgen) {
                switch (count) {
                    case 0:
                        translate(dx * 3, dy * 3);
                        break;
                    case 1:
                        translate(dx * 5, dy * 5);
                        break;
                    case 2:
                        translate(dx * 9, dy * 9);
                        break;
                    case 3:
                        translate(dx * 15, dy * 15);
                        break;
                }
            } else {
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
            }
            count++;
            if (morgen)
		if (count >= 4) count = 0;
	    if (count >= 8) count = 0;
            if (collidesWith(playerSprite)) playerSprite.die();
            maze.touch(this);
        }

        public void draw(Graphics g, View view) {
            Image i = null;
            int d = this.d / 2;

	    if (gameStopped > 0) {
		if ((gameStopped < 16) && (gameStopped % 2 == 0))
		    i = animation.getFrame(2 + d * 4).getImage();
		else
		    i = animation.getFrame(3 + d * 4).getImage();
	    } else
		i = animation.getFrame((count / 2) % 3 + d * 4).getImage();

            g.drawImage(i, view.worldToRealX(getX()) - 16 - count % 3 + 1, view.worldToRealY(getY()) - 24 - count % 4, null);
        }
    }

    class Fire extends Enemy {
        private int count = 0;
        private Animation animation = cropper.getAnimation("fire");
        private int dx = 0;
        private int dy = 0;
        private int d = 0;
        private int hd = 0;
        private int dont_move;

        public Fire(int dont_move) {
            this.dont_move = dont_move;
        }

        public int getScore() {
            return 50;
        }

        public void die() {
            sprites.remove(this);
        }

        public void move() {
            if (gameStopped > 0) return;
            if (count == 0) {
                // should fire die here?

                boolean die = false;

                switch (dont_move) {
                    case 0:
                        die = ((maze.canMove(this, getX() - 8, getY()) == false) && (maze.canMove(this, getX(), getY() - 8) == false));
                        break;
                    case 1:
                        die = ((maze.canMove(this, getX(), getY() - 8) == false) && (maze.canMove(this, getX() - 8, getY()) == false));
                        break;
                    case 2:
                        die = ((maze.canMove(this, getX() + 8, getY()) == false) && (maze.canMove(this, getX(), getY() + 8) == false));
                        break;
                    case 3:
                        die = ((maze.canMove(this, getX(), getY() + 8) == false) && (maze.canMove(this, getX() + 8, getY()) == false));
                        break;
                }

                if (die) {
                    sprites.add(new Explosion("explosion_enemy"), getX(), getY());
                    sprites.remove(this);
                    return;
                }

                int od = d;
                do {
                    switch ((int) (4 * Math.random())) {
                        case 0:
                            dx = 1;
                            dy = 0;
                            d = 0;
                            hd = 0;
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
                            hd = 1;
                            break;
                        case 3:
                            dx = 0;
                            dy = -1;
                            d = 3;
                            break;
                    }
                } while ((maze.canMove(this, getX() + dx, getY() + dy) == false) ||
                        ((Math.random() > .05) && (d != od)) || (d == dont_move));
            }
            count++;
            if (count >= 4) count = 0;
            translate(dx * 8, dy * 8);
            if (collidesWith(playerSprite)) playerSprite.die();
            maze.touch(this);
        }

        public void draw(Graphics g, View view) {
	    Image i;
	    if (gameStopped > 0) {
		if ((gameStopped%2 == 0) && (gameStopped < 16))
		    i = animation.getFrame(2+hd*5).getImage();
		else
		    i = animation.getFrame(4+hd*5).getImage();
	    } else
		i = animation.getFrame(count + hd * 5).getImage();
            g.drawImage(i, view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2 - (2 * count % 2) + 2,
                    view.worldToRealY(getY()) - animation.getFrame(0).getHeight() / 2 - count % 3, null);
        }
    }

    class Robot extends Enemy {
        private int count = 0;
        private Animation animation;
        private int dx = 0;
        private int dy = 0;
        private int d = 0;
        private int looking = 0;
        private boolean morgen = false;

        Robot(boolean morgen) {
            if (morgen)
                animation = cropper.getAnimation("morgen-erik");
            else
                animation = cropper.getAnimation("erik");
            this.morgen = morgen;
        }

        public int getScore() {
            if (morgen) return 500;
	    else return 250;
        }

        public void die() {
            sprites.remove(this);
        }

        public void move() {
            if (gameStopped > 0) return;
            if (count == 0) {
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
                } while ((maze.canMove(this, getX() + dx, getY() + dy) == false) || ((Math.random() > ndl) && (d != od)));
                if (d == 0) looking = 0;
                if (d == 2) looking = 1;
            }
            if (morgen)
                translate(dx * 8, dy * 8);
            else
                translate(dx * 4, dy * 4);
            count++;
            if (count >= 4) count = 0;
            if (collidesWith(playerSprite)) playerSprite.die();
        }

        public void draw(Graphics g, View view) {
            Image i = null;
	    
	    if (gameStopped > 0) {
		if ((gameStopped < 16) && (gameStopped % 2 == 0)) {
		    i = animation.getFrame(4 + 5 * looking).getImage();
		} else {
		    i = animation.getFrame(5 + 5 * looking).getImage();
		}

	    } else {
		if (d == 4)
		    i = animation.getFrame(0).getImage();
		else
		    i = animation.getFrame(1 + 5 * looking + count / 2).getImage();
	    }
            g.drawImage(i, view.worldToRealX(getX()) - 16 - count % 3,
                    view.worldToRealY(getY()) - 16 - count % 3, null);
        }
    }

    Screen game = new Screen() {
        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            g.setColor(bgColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            maze.draw(g, view);
            sprites.draw(g);
            drawScore(g);
        }

        public void move() {
            scripts.move();
            sprites.move();
            view.move();
            if (blocksTaken == blocksCount) playerSprite.exit();
            if (gameStopped > 0) gameStopped--;
        }
    };

    public void nextLevel() {
        savePoint = nextLevel;
        showScreen(enterGame);
    }

    Screen die = new Screen() {
        public void exit() {
            lives--;
        }

        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            g.setColor(bgColor);
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
                if ((lives-1) > 0)
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
            g.setColor(bgColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            maze.draw(g, view);
            sprites.draw(g);
            g.drawImage(cropper.getFrame("gameover").getImage(), 280 - cropper.getFrame("gameover").getWidth() / 2, 150, null);
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

    Screen enterGame = new Screen() {
        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            g.setColor(bgColor);
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
            resetColors();
            maze = mazes.getMaze(savePoint);
            gameStopped = 0;

            initMaze();

            playerSprite = null;

            scripts.setCallback(new Callback() {
                public void command(String name, String text) {
                    if (name.equals("nextlevel"))
                        nextLevel = text;
                    if (name.startsWith("sc_")) {
                        Color c = new Color(Integer.parseInt(text, 16));
                        if (name.equals("sc_bg")) bgColor = c;
                        if (name.equals("sc_1f")) fillColor[0] = c;
                        if (name.equals("sc_1r")) rimColor[0] = c;
                        if (name.equals("sc_2f")) fillColor[1] = c;
                        if (name.equals("sc_2r")) rimColor[1] = c;
                        if (name.equals("sc_3f")) fillColor[2] = c;
                        if (name.equals("sc_3r")) rimColor[2] = c;
                    }
                }

                public void command(String name, int x, int y) {
                    if (name.equals("fish"))
                        sprites.add(new Fish(false), x, y);
                    else if (name.equals("morgen-ape"))
                        sprites.add(new Fish(true), x, y);
                    else if (name.equals("robot"))
                        sprites.add(new Robot(false), x, y);
                    else if (name.equals("morgen-robot"))
                        sprites.add(new Robot(true), x, y);
                    else if (name.equals("firew"))
                        sprites.add(new Fire(0), x, y);
                    else if (name.equals("fires"))
                        sprites.add(new Fire(1), x, y);
                    else if (name.equals("firee"))
                        sprites.add(new Fire(2), x, y);
                    else if (name.equals("firen"))
                        sprites.add(new Fire(3), x, y);
                    else if (name.equals("ring"))
                        sprites.add(new Ring(), x, y);
                    else if (name.equals("stop_watch"))
                        sprites.add(new StopWatch(), x, y);
                    else if (name.equals("player") && (playerSprite == null))
                        sprites.add(playerSprite = new PlayerSprite(), x, y);
                    else if (name.startsWith("animation "))
                        sprites.add(new StaticSprite(name.substring(10)), x, y);
                }
            });

            scripts.move();
            view.reset();
        }
    };

    Screen writeHiscore = new Screen() {
	    public void enter() {
		resetColors();
	    }

	    public void draw() {
		Graphics g = getGraphicsModel().getFront();
		
		g.setColor(bgColor);
		g.fillRect(0, 0, getWidth(), getHeight());

		g.drawImage(cropper.getFrame("sun").getImage(), 455, 2, null);
		g.drawImage(cropper.getFrame("stars").getImage(), 120, 20, null);
		g.drawImage(cropper.getFrame("stars").getImage(), 20, 40, null);
		g.drawImage(cropper.getFrame("newhiscore").getImage(), 99, 75, null);
		g.drawImage(cropper.getFrame("andpressreturn").getImage(), 144, 360, null);

		hiscore.drawHiscoreEditor(g);
		
		
		/*
		g.setColor(new Color(90, 90, 90));
		g.drawString("Write your name and press return.", 100, 360);
		*/
	    }
	    public void move() {
		if (hiscore.moveHiscoreEditor() == false) showScreen(opening);
	    }
	};

    Screen showHiscore = new Screen() {
	    private int count;
	    public void enter() {
		count = 0;
	    }
	    public void draw() {
		Graphics g = getGraphicsModel().getFront();
		
		g.setColor(bgColor);
		g.fillRect(0, 0, getWidth(), getHeight());

	    }
	    public void move() {
		count ++;
		if (count > 30) showScreen(opening);
	    }
	};

    Screen opening = new Screen() {
	    int enterAt;

	    public void enter() {
		enterAt = getFrameCount();
		getLocalPlayer().getMouseState().countLeft();
		resetColors();
	    }
	    
	    private void drawCharacters1(int count) {
		Graphics g = getGraphicsModel().getFront();
		g.setColor(Color.black);
		
		if (count > 0) g.drawImage(cropper.getFrame("ape" + ((count / 2) % 3) + "e").getImage(), 160, 150 - ((count / 2) % 4), null);
		if (count > 15) g.drawImage(cropper.getFrame("erik" + (1 + (count / 2) % 3) + "e").getImage(), 160, 220 - ((count / 2) % 3), null);
		
		if (count > 5) g.drawString("Slacker Erik", 220, 150);
		
		if (count > 7) g.drawString("Slacker Erik is a real sucker", 220, 172);
		if (count > 9) g.drawString("that stupid bastard is eating your dots", 220, 188);
		
		if (count > 20) g.drawString("Angry Erik", 220, 220);
		
		if (count > 22) g.drawString("Angry Erik is bitter madman", 220, 242);
		if (count > 24) g.drawString("better stay clear of him", 220, 258);
	    }
	    
	    private void drawCharacters2(int count) {
		Graphics g = getGraphicsModel().getFront();
		g.setColor(Color.black);
		
		if (count > 0) g.drawImage(cropper.getFrame("morgen-ape" + ((count / 2) % 3) + "e").getImage(), 160, 150 - ((count / 2) % 4), null);
		if (count > 15) g.drawImage(cropper.getFrame("morgen-erik" + (1 + (count / 2) % 3) + "e").getImage(), 160, 220 - ((count / 2) % 3), null);
		
		if (count > 5) g.drawString("Hophop Erik", 220, 150);
		
		if (count > 7) g.drawString("Hophop Erik dances the evil hopla", 220, 172);
		if (count > 9) g.drawString("and then he is taking your dots", 220, 188);
		
		if (count > 20) g.drawString("Mornin' Erik", 220, 220);
		
		if (count > 22) g.drawString("Mornin' Erik is really mad", 220, 242);
		if (count > 24) g.drawString("Omfufu has stolen his morning coffee", 220, 258);
	    }
	    
	    private void drawCharacters3(int count) {
		Graphics g = getGraphicsModel().getFront();
		g.setColor(Color.black);
		
		if (count > 0) g.drawImage(cropper.getFrame("fire" + (1 + (count / 2) % 3)).getImage(), 162, 150 - ((count) % 3), null);
		if (count > 15) g.drawImage(cropper.getFrame("babystill").getImage(), 160, 220, null);
		
		if (count > 5) g.drawString("Speedy Erik", 220, 150);
		
		if (count > 7) g.drawString("Speedy Erik is in a real hurry", 220, 172);
		if (count > 9) g.drawString("duck or he will run you down", 220, 188);
		
		if (count > 20) g.drawString("Commerade Erik", 220, 220);
		
		if (count > 22) g.drawString("Commarede Erik is our hero", 220, 242);
		if (count > 24) g.drawString("build some rectangles with him", 220, 258);
	    }
	    
	    public void drawCharacters(int count) {
		switch (count / 100) {
		case 0:
		    hiscore.drawHiscore(getGraphicsModel().getFront());
		    break;
                case 1:
                    drawCharacters1(count % 100);
                    break;
                case 2:
                    drawCharacters2(count % 100);
                    break;
                case 3:
                    drawCharacters3(count % 100);
                    break;
		}
	    }
	    
	    public void drawTitle1(int count) {
		Graphics g = getGraphicsModel().getFront();
		if (count < 60) {
		    g.drawImage(cropper.getFrame("logo-top").getImage(), 560 - Math.min(560, count * 28), 0, null);
		    g.drawImage(cropper.getFrame("logo-bottom").getImage(), Math.min(0, count * 28 - 560), 108, null);
		} else {
		    g.drawImage(cropper.getFrame("logo-top").getImage(), 560 - Math.min(560 * 2, (count - 40) * 28), 0, null);
		    g.drawImage(cropper.getFrame("logo-bottom").getImage(), Math.min(560, (count - 40) * 28 - 560), 108, null);
		}
	    }
	    
	    public void drawTitle(int count) {
		Graphics g = getGraphicsModel().getFront();
		g.drawImage(cropper.getFrame("erik-forward").getImage(), 96 - Math.max(0, 560 - count * 28), 35, null);
		g.drawImage(cropper.getFrame("erik-backward").getImage(), 326 + Math.max(0, 560 - count * 28), 35, null);
		g.drawImage(cropper.getFrame("vs").getImage(), 261, 63, null);
	    }
	    
	    public void draw() {
		Graphics g = getGraphicsModel().getFront();
		
		g.setColor(bgColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		int count = getFrameCount() - enterAt;
		
		drawTitle(count);
		
		drawCharacters((count + 15) % 400);
		
		// drawScore(g);
		
		MouseState ms = getLocalPlayer().getMouseState();
		
		if (within(ms.getX(), ms.getY(), 100, 347, 320, 27))
		    g.drawImage(cropper.getFrame("clickhereover").getImage(), 120, 347, null);
		else
		    g.drawImage(cropper.getFrame("clickhere").getImage(), 120, 347, null);
	    }
	    public void move () {
		MouseState ms = getLocalPlayer().getMouseState();
		if ((within(ms.getX(), ms.getY(), 120, 347, 360, 27)) && (ms.countLeft() > 0))
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

    public void drawText(Graphics g, int x, int y, String font, String text) {
        int j = x;
        for (int i = 0; i < text.length(); i++) {
            g.drawImage(cropper.getFrame(font + text.charAt(i)).getImage(), j, y, null);
            j += cropper.getFrame(font + text.charAt(i)).getWidth();
        }
    }

    public void drawScore(Graphics g) {
        int p = 1;
        String text = "";
	if (score < 100000) {
	    for (int j = 0; j < 5; j++) {
		int k = ((score / p) % 10);
		p *= 10;
		text = k + text;
	    }
	    drawText(g, 474, 10, "", text);
	} else {
	    for (int j = 0; j < 6; j++) {
		int k = ((score / p) % 10);
		p *= 10;
		text = k + text;
	    }
	    drawText(g, 460, 10, "", text);
	}

        for (int i = 0; i < 5; i++)
            if (i < lives - 1)
                g.drawImage(cropper.getFrame("heart").getImage(), 10 + i * 26, 8, null);
    }

    // TPElement is the element class with an init procedure
    // which is called after all elements are created.

    abstract class TPElement extends Element {
        public void init(Maze maze, int x, int y) {
        }
    }

    Color colors[] = {Color.blue, Color.red, Color.green, Color.yellow};

    class Block {
        private int count = 0;
        private Color color = new Color((int) (Math.random() * 128) + 64, (int) (Math.random() * 128) + 64, (int) (Math.random() * 128) + 64);
        private Vector surroundingGrounds = new Vector();
        private int sumX, sumY, sumCount;

        public void addSurroundingGround(Ground ground) {
            surroundingGrounds.addElement(ground);
            sumX += ground.getX();
            sumY += ground.getY();
            sumCount++;
        }

        public int getCount() {
            return count;
        }

        public void oneDown() {
            count--;
            if (count == 0) {
                // send message to all surrounding grounds to go into fixed mode
                for (Enumeration e = surroundingGrounds.elements(); e.hasMoreElements();) {
                    Ground ground = (Ground) e.nextElement();
                    ground.fix();
                }
                blocksTaken++;
                score += 10 * surroundingGrounds.size();
                sprites.add(new PointsInBlock(10 * surroundingGrounds.size()), sumX / sumCount, sumY / sumCount);
            }
        }

        public void oneUp() {
            count++;
        }

        public Block() {
            blocksCount++;
        }
    }

    class NullBlock extends Block {
        public Color getColor() {
            return bgColor;
        }

        public void oneDown() {
        }

        public void oneUp() {
        }

        public NullBlock() {
            blocksCount--; /* super counts this block - uncount it */
        }
    }

    class StopWatch extends SJGSpriteFA {
        StopWatch() {
            setAnimation(cropper.getAnimation("stop_watch"));
        }

        public void move() {
            nextFrame();
            if (playerSprite.collidesWith(this)) {
                sprites.remove(this);
                gameStopped = 50;
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

    class FixExplosion extends SJGSpriteFA {
        FixExplosion() {
            setAnimation(cropper.getAnimation("explosion_untick"));
        }

        public void move() {
            for (Enumeration e = sprites.elements(); e.hasMoreElements();) {
                SJGSprite s = (SJGSprite) e.nextElement();
                if ((s instanceof Enemy) && (collidesWith(s))) {
                    ((Enemy) s).die();
                    score += ((Enemy) s).getScore();
                    sprites.add(new Explosion("explosion_enemy"), s.getX(), s.getY());
                    sprites.remove(this);
                    return;
                }
            }
            nextFrame();
            if (getFrame() >= 4) sprites.remove(this);
        }
    }

    class Wall extends TPElement {
        Block block;
        boolean wn, we, ww, ws;
        int colorNo;

        public Wall(int colorNo) {
            this.colorNo = colorNo;
        }

        public Block getBlock() {
            return block;
        }

        public void init(Maze maze, int x, int y) {
            try {
                wn = (maze.getElement(x, y - 1) instanceof Ground);
            } catch (Exception e) { /* ignore bound error */
            }
            try {
                we = (maze.getElement(x + 1, y) instanceof Ground);
            } catch (Exception e) { /* ignore bound error */
            }
            try {
                ws = (maze.getElement(x, y + 1) instanceof Ground);
            } catch (Exception e) { /* ignore bound error */
            }
            try
            {
                ww = (maze.getElement(x - 1, y) instanceof Ground);
            } catch (Exception e) { /* ignore bound error */
            }

            if ((block == null) && ((x * 32 != 0) && (y * 32 != 0) && (x * 32 != maze.getWidth() - 32) && (y * 32 != maze.getHeight() - 32)))
                paint(maze, x, y, new Block());
            else
                paint(maze, x, y, new NullBlock());
        }

        private void paint(Maze maze, int x, int y, Block block) {
            if (this.block == null) {
                this.block = block;
                try {
                    ((Wall) (maze.getElement(x, y - 1))).paint(maze, x, y - 1, block);
                } catch (Exception e) { /* ignore error */
                }
                try {
                    ((Wall) (maze.getElement(x + 1, y))).paint(maze, x + 1, y, block);
                } catch (Exception e) { /* ignore error */
                }
                try {
                    ((Wall) (maze.getElement(x, y + 1))).paint(maze, x, y + 1, block);
                } catch (Exception e) { /* ignore error */
                }
                try {
                    ((Wall) (maze.getElement(x - 1, y))).paint(maze, x - 1, y, block);
                } catch (Exception e) { /* ignore error */
                }
            }
        }

        public boolean isSolid() {
            return true;
        }

        public void draw(Graphics g, int x, int y) {
            if ((block instanceof NullBlock) == false) {
                if (block.getCount() == 0) {
                    g.setColor(fillColor[colorNo]);
                    g.fillRect(x, y, 32, 32);
                    g.setColor(rimColor[colorNo]);
                    if (wn) g.fillRect(x, y, 32, 2);
                    if (ws) g.fillRect(x, y + 30, 32, 2);
                    if (ww) g.fillRect(x, y, 2, 32);
                    if (we) g.fillRect(x + 30, y, 2, 32);
                } else {
                }
            } else {
                // g.drawImage(cropper.getFrame("brick1").getImage(),x,y,null);
                int colorNo = 3;
                // g.setColor(fillColor[colorNo]);
                // g.fillRect(x,y, 32,32);
                /*
                g.setColor(rimColor[colorNo]);
                if (wn) g.fillRect(x,y+15,32,2);
                if (ws) g.fillRect(x,y+15,32,2);
                if (ww) g.fillRect(x+15,y,2,32);
                if (we) g.fillRect(x+15,y,2,32);
                */
            }
        }
    };

    int circle[][] = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};

    class Ground extends TPElement {
        private Image image;
        private boolean state;
        private boolean fixed;
        private Wall wall[] = new Wall[8];
        int x,y;

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void addAsSurroundingToBlocks() {
            for (int i = 0; i < 8; i++)
                if (wall[i] != null)
                    wall[i].getBlock().addSurroundingGround(this);
        }

        public void init(Maze maze, int x, int y) {
            // figure out the right image to use - the images are named mazeXXXX
            // when X is 1 there is a connection to hhv. (eng?) north, east, south, west

            String s = "";
            try {
                if (maze.getElement(x, y - 1) instanceof Ground) s = "1" + s; else s = "0" + s;
            } catch (Exception e) { /* ignore bound error */
            }
            try {
                if (maze.getElement(x + 1, y) instanceof Ground) s = "1" + s; else s = "0" + s;
            } catch (Exception e) { /* ignore bound error */
            }
            try {
                if (maze.getElement(x, y + 1) instanceof Ground) s = "1" + s; else s = "0" + s;
            } catch (Exception e) { /* ignore bound error */
            }
            try {
                if (maze.getElement(x - 1, y) instanceof Ground) s = "1" + s; else s = "0" + s;
            } catch (Exception e) { /* ignore bound error */
            }
            image = cropper.getFrame("maze" + s).getImage();

            // find neighbouring walls and add this ground as neighbour

            for (int i = 0; i < 8; i++)
                try {
                    wall[i] = (Wall) maze.getElement(x + circle[i][0], y + circle[i][1]);
                } catch (Exception e) { /* ignore bound or cast error */
                }

            this.x = x * 32;
            this.y = y * 32;
        }

        public boolean isSolid() {
            return false;
        }

        public void oneDown() {
            for (int i = 0; i < 8; i++)
                if (wall[i] != null) wall[i].getBlock().oneDown();
        }

        public void oneUp() {
            for (int i = 0; i < 8; i++)
                if (wall[i] != null) wall[i].getBlock().oneUp();
        }

        public Element touch(SJGSprite sprite, int x, int y) {
            if ((state == false) && (sprite instanceof PlayerSprite)) {
                state = true;
                oneDown();
                score++;
                sprites.add(new Explosion("explosion_tick"), x + 16, y + 16);
            }
            if ((state == true) && (fixed == false) && (sprite instanceof Fish)) {
                state = false;
                oneUp();
                score--;
                sprites.add(new Explosion("explosion_untick"), x + 16, y + 16);
            }
            return this;
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(image, x, y, null);
            if (state) {
                if (fixed)
                    g.drawImage(cropper.getFrame("fixedtick").getImage(), x, y, null);
                else
                    g.drawImage(cropper.getFrame("tick").getImage(), x, y, null);
            }
        }

        public void move() {
        }

        public void fix() {
            fixed = true;
            sprites.add(new FixExplosion(), x + 16, y + 16);
        }
    }

    private void resetColors() {
        fillColor[0] = new Color(222, 106, 66); // red
        fillColor[1] = new Color(204, 200, 92); // yellow
        fillColor[2] = new Color(92, 182, 96); // green
        fillColor[3] = new Color(115, 134, 151);
        rimColor[0] = new Color(249, 157, 125);
        rimColor[1] = new Color(229, 223, 119);
        rimColor[2] = new Color(125, 215, 127);
        rimColor[3] = new Color(105, 124, 141);
        bgColor = new Color(185, 192, 199);
    }

    private void initMaze() {
        blocksTaken = 0;
        blocksCount = 0;

        maze.init();

        // init all blocks in all mazes

        for (int x = 0; x < maze.getWidthInBlocks(); x++)
            for (int y = 0; y < maze.getHeightInBlocks(); y++)
                ((TPElement) maze.getElement(x, y)).init(maze, x, y);

        // set the counter in the blocks to the number of surrounding walls

        for (int x = 0; x < maze.getWidthInBlocks(); x++)
            for (int y = 0; y < maze.getHeightInBlocks(); y++)
                if (maze.getElement(x, y) instanceof Ground) {
                    Ground ground = (Ground) maze.getElement(x, y);
                    ground.oneUp();
                    ground.addAsSurroundingToBlocks();
                }
    }
    
    void drawString(Graphics g, String text, int x, int y) {
        int x1 = x;
        for (int i = 0; i < text.length(); i++) {
            g.drawImage(cropper.getFrame("" + text.charAt(i)).getImage(), x1, y, null);
            x1 += cropper.getFrame("" + text.charAt(i)).getWidth();
        }
    }

    public void init() {
        setGraphicsModel(new sjg.gm.Buffered(this));
        showScreen(opening);
        cropper = new Cropper(this);
        scripts = new ScriptEngine(this);
        mazes = new Mazes(this, new ElementFactory() {
		public Element getElement(char c) {
		    switch (c) {
                    case 'q':
                        return new Wall(0);
                    case 'w':
                        return new Wall(1);
                    case 'e':
                        return new Wall(2);
                    case '.':
                        return new Ground();
                    case ' ':
                        return new Wall(0);
		    }
		    return new Wall(0);
		}
	    } );
	hiscore = new Hiscore(this, new HttpGetHiscoreServer(this, "/hiscore/list-hiscores.php", "/hiscore/add-hiscore.php")) {
		public void drawHiscoreText(int position, String positionText, String entryText, int score, String round) {
		    Graphics g = getGraphicsModel().getFront();
		    if (getScreen() == opening)
			g.setColor(Color.black);
		    else
			g.setColor(new Color(90, 90, 90));
		    int y = position*20+140;
		    g.drawString(positionText, 125, y);
		    g.drawString(entryText, 175, y);
		    g.drawString(""+score, 390, y);
		    g.drawString(round, 360, y);
		}
		public void drawWriteHiscoreText(int position, String positionText, String entryText, int score, String round) {
		    Graphics g = getGraphicsModel().getFront();
		    g.setColor(Color.black);
		    int y = position*20+140;
		    g.drawString(positionText, 125, y);
		    g.drawString(entryText, 175, y);
		    g.drawString(""+score, 390, y);
		    g.drawString(round, 360, y);
		}
	    };
    }
}
