import sjg.*;
import sjg.animation.*;
import sjg.scripting.*;
import sjg.maze.*;
import sjg.hiscore.*;

import synth.*;

import java.awt.*;
import java.util.*;

/**
 * Mr. Hopwit on the Run
 *
 * @author Christian Hvid 2004
 */

public class JumpnRun extends SJGame {
    final Color DEFAULT_BG_COLOR = new Color(52, 74, 53);
    Color backgroundColor;
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

    public String getConfigurationFileName() {
        return "jumpnrun.txt";
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
	    if (getScreen() == openingAttract)
		cameraY = maze.getHeight() / 2+64;
            move();
        }
    }

    TView view = new TView();

    class RemoteController {
	private boolean up;
	private boolean down;
	private boolean left;
	private boolean right;
	private boolean jump;
	public boolean isUp() { return up; }
	public boolean isDown() { return down; }
	public boolean isLeft() { return left; }
	public boolean isRight() { return right; }
	public boolean isJump() { return jump; }
	public void command(String cmd) {
	    left = false;
	    right = false;
	    up = false;
	    down = false;
	    jump = false;
	    if (cmd.indexOf("left") >= 0) left = true;
	    if (cmd.indexOf("right") >= 0) right = true;
	    if (cmd.indexOf("up") >= 0) up = true;
	    if (cmd.indexOf("down") >= 0) down = true;
	    if (cmd.indexOf("jump") >= 0) jump = true;
	}
    }

    RemoteController remoteController = new RemoteController();

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
	private int jump = 0;
	private double deltay = 0;

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
	    if (((i==2) || (i==3)) && (maze.getElement(getX() / maze.getBlockWidth(), getY() / maze.getBlockHeight()) != stair))
		return false;

            return maze.canMove(this, getX() + sx[i] * speed, getY() + sy[i] * speed);
        }

        private boolean canMoveSec(int primary, int via) {
            if (maze.canMove(this, getX() + sx[via] * speed, getY() + sy[via] * speed)) {
                translate(sx[via] * speed, sy[via] * speed);
                boolean result = canMove(primary);
                translate(sx[via] * speed, sy[via] * speed);
                result |= canMove(primary);
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

	    boolean jump = false;
	    boolean oldk[] = new boolean[4];
	    
	    oldk[0] = keyb[0];
	    oldk[1] = keyb[1];
	    oldk[2] = keyb[2];
	    oldk[3] = keyb[3];
	    
	    if (getScreen() == openingAttract) {
		keyb[0] = remoteController.isLeft();
		keyb[1] = remoteController.isRight();
		keyb[2] = remoteController.isUp();
		keyb[3] = remoteController.isDown();
		jump = remoteController.isJump();
	    } else {
		KeyboardState ks = getLocalPlayer().getKeyboardState();		
		keyb[0] = ks.isDown(37);
		keyb[1] = ks.isDown(39);
		keyb[2] = ks.isDown(38);
		keyb[3] = ks.isDown(40);
		jump = ks.isDown(32);
	    }

	    if (jump)
		if ( (maze.canMove(this, getX(), getY() + 1) == false) ||
		     ((maze.canMove(this, getX()-32, getY() + 1) == false) && (maze.canMove(this, getX()-32, getY()) == true) &&
		      (maze.canMove(this, getX()+32, getY() + 1) == false) && (maze.canMove(this, getX()+32, getY()) == true)) )
		    deltay = -20;

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

	    deltay +=3;
	    if (maze.getHeight() <= getY()) {
		translate(0, -maze.getHeight());
		synthManager.startTrack("lattice");
	    }

	    if ((maze.getElement(getX() / maze.getBlockWidth(), getY() / maze.getBlockHeight()) != stair) || (deltay < 0)) {
		if (maze.canMove(this, getX(), getY() + deltay)) translate(0, deltay);
		else {
		    if (deltay > 0) {
			for (int i = (int)Math.round(deltay); i > 0; i--)
			    if (maze.canMove(this, getX(), getY() + i)) translate(0, i);

		    } else {
			for (int i = (int)Math.round(deltay); i < 0; i++)
			    if (maze.canMove(this, getX(), getY() + i)) translate(0, i);

 			deltay = -deltay;
			translate(0,-1);
			maze.touch(this);
			translate(0,1);
		    }
		}
		if (deltay > 10) deltay=10;
		if (maze.canMove(this, getX(), getY() + 1) == false) deltay = 0;
	    } else deltay = 0;

	    if (maze.getHeight() <= getY()) {
		translate(0, -maze.getHeight());
		synthManager.startTrack("lattice");
	    }
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
            int sw = (s.length()*8 * Math.min(count, 3)) / 3+12;
            int sh = 10;
            int x = view.worldToRealX(getX()) - sw / 2 - 3;
            int y = view.worldToRealY(getY()) - sh / 2 - 1;
            g.setColor(new Color(30, 80, 140));
            g.fillRect(x, y, sw + 6, sh + 3);
            g.setColor(Color.black);
            g.drawRect(x, y, sw + 6, sh + 3);
            if (count > 3) drawText(g, x + 9, y+2, "", s);
        }
    }

    class Bubble extends SJGSprite {
        private int count = 3;
        private Animation animation = cropper.getAnimation("bubble");
        private int dx = 0;
        private int dy = 0;
        private int d = 0;
        public void move() {
	    count ++;
	    if (count >= 4) count = 3;

	    if (playerSprite.status == PlayerSprite.EXITING) return;

	    dy += 3;

	    if (dy > 10) dy = 9;

	    if (maze.canMove(this, getX(), getY()+dy) == false) {
		count = 0;

		if (dy < 0) {
		    translate(0, dy);
		    maze.touch(this);
		    translate(0, -dy);
		}

		dy = -dy;
		if (Math.random() < 0.2) {
		    dx = ((int)(Math.random()*3)-1) * 6;
		    if (dx == 0) dx = ((int)(Math.random()*3)-1) * 6;
		}
	    }

	    if (maze.canMove(this, getX()+dx, getY()) == false) dx = -dx;

	    if (dx > 0) d = 0;
	    if (dx < 0) d = 1;

	    translate(dx, dy);

            if (collidesWith(playerSprite)) playerSprite.die();
	    // maze.touch(this);
	    if (maze.getHeight() < getY()) translate(0, -maze.getHeight());
        }

	public void puf() {
	    dy = -24;
	}

        public void draw(Graphics g, View view) {
            Image i = null;
            switch (count) {
                case 0:
                    i = animation.getFrame(2 + d * 3).getImage();
                    break;
                case 1:
                case 2:
                    i = animation.getFrame(1 + d * 3).getImage();
                    break;
                case 3:
                    i = animation.getFrame(d * 3).getImage();
                    break;
            }

	    g.drawImage(i, view.worldToRealX(getX()) - animation.getFrame(0).getWidth() / 2,
			view.worldToRealY(getY()) - animation.getFrame(0).getHeight() / 2, null);
        }
    }

    class Stardust extends SJGSpriteFA {
	private int count;
	public Stardust(String color) {
	    setAnimation(cropper.getAnimation(color+"-stardust"));
	}
	public void move() {
	    nextFrame();
	    count ++;
	    translate((int)(Math.random()*9-4), 2-(int)(Math.random()*8));
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
	    dx = Math.random()*20-10;
	    dy = Math.random()*20-10;
	    synthManager.startTrack("star");
	}
	public int getWidth() {
	    return animation.getFrame(frame).getWidth();
	}
	public int getHeight() {
	    return animation.getFrame(frame).getHeight();
	}
	public Star(String color) {
	    animation = cropper.getAnimation(color+"-star");
	    dx = Math.random()-0.5;
	    dy = Math.random()-0.5;
	    starCount ++;
	    this.color = color;
	}
	public Star(String color, double dx, double dy) {
	    animation = cropper.getAnimation(color+"-star");
	    dx = Math.random()-0.5;
	    dy = Math.random()-0.5;
	    starCount ++;
	    this.color = color;
	    this.dx = dx;
	    this.dy = dy;
	}
	public void draw(Graphics g, View view) {
	    g.drawImage(animation.getFrame(frame).getImage(),
			view.worldToRealX(getX()) - animation.getFrame(frame).getWidth() / 2,
			view.worldToRealY(getY()) - animation.getFrame(frame).getHeight() / 2,
			null);
	}
	public void move() {
	    count ++;
	    if (count % Math.max(1,(5-((int)Math.pow(dx*dx+dy*dy, 0.5)/2))) == 0)
		frame ++;

	    if (maze.canMove(this, getXdouble()+dx, getYdouble()+dy) == false) {
		if (maze.canMove(this, getXdouble()-dx, getYdouble()+dy) == true)
		    dx=-dx;
		else if (maze.canMove(this, getXdouble()+dx, getYdouble()-dy) == true)
		    dy=-dy;
		else {
		    dx=-dx; dy=-dy;
		}
		dx *= 0.9;
		dy *= 0.9;
	    }
	    translate(dx, dy);
	    for (Enumeration e=sprites.elements(); e.hasMoreElements();) {
		SJGSprite sprite = (SJGSprite)e.nextElement();
		if (collidesWith(sprite)) {
		    if (sprite instanceof YellowBlob) newDirection();
		    if (sprite instanceof Bubble) newDirection();
		    if (sprite instanceof PlayerSprite) {
			sprites.remove(this);

			if (getScreen() != openingAttract) {
			    if (color.equals("red")) score += 250;
			    if (color.equals("blue")) score += 350;
			}

			sprites.add(new Stardust(color), getX(), getY());
			starCount --;
			if ((starCount == 0) && (getScreen() != openingAttract)) {
			    sprites.add(new Sign(findExitText), playerSprite.getX(), playerSprite.getY()-28);
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
            // maze.touch(this);
	    if (getY() < 0) translate(0, maze.getHeight());
	    if (maze.getHeight() <= getY()) translate(0, -maze.getHeight());
	}
    }

    class SoapBubble extends SJGSprite {
	double dx;
	Animation animation;
	int count;
	public SoapBubble() {
	    starCount += 5;
	    animation = cropper.getAnimation("blue-star");
	}
	public void draw(Graphics g, View view) {
	    g.drawImage(cropper.getFrame("soap-bubble-1").getImage(), view.worldToRealX(getX()) - 16, view.worldToRealY(getY()) - 16, null);
	    
	    for (int i = 0; i < 3; i++) {
		int x = getX()+(int)(8*Math.sin((5*i+count)*0.5));
		int y = getY()+(int)(8*Math.sin((5*i+count)*0.6));
		sjg.animation.Frame frame = animation.getFrame((count+i) % animation.size());
		g.drawImage(frame.getImage(),
			    view.worldToRealX(x) - frame.getWidth() / 2,
			    view.worldToRealY(y) - frame.getHeight() / 2,
			    null);
		
	    }
	}
	public void puf() {
	    String color = "blue";
	    sprites.remove(this);
	    starCount -= 5;
	    for (int i = 0; i < 5; i++) {
		Star star = new Star(color, 15*Math.cos(i * Math.PI * 2 / 5), 15*Math.sin(i * Math.PI * 2 / 5));
		
		if (maze.canMove(star, getX()+30*Math.cos(i * Math.PI * 2 / 5), getY()+30*Math.sin(i * Math.PI * 2 / 5) ))
		    sprites.add(star, getX()+30*Math.cos(i * Math.PI * 2 / 5), getY()+30*Math.sin(i * Math.PI * 2 / 5) );
		else
		    sprites.add(star, getX(), getY());
	    }
	    synthManager.startTrack("soap-pop");
	}
	public void newDirection() {
	    dx += Math.random()*2 - 1;
	}
	public void move() {
	    count ++;	    
	    if (Math.random() < 0.1) dx += Math.random() - 0.5;
	    if (dx > 3) dx = 3;
	    if (dx < -3) dx = -3;
	    if (maze.canMove(this, getXdouble()+dx, getYdouble())) translate(dx, 0);
	    else dx = 0;
	    if (maze.canMove(this, getXdouble(), getYdouble()-2)) translate(0, -2);

	    if (getY() < 0) translate(0, maze.getHeight());

	    for (Enumeration e=sprites.elements(); e.hasMoreElements();) {
		SJGSprite sprite = (SJGSprite)e.nextElement();
		if (collidesWith(sprite)) {
		    if (sprite instanceof YellowBlob) {
			if (((YellowBlob)sprite).isJumping()) { puf(); return; }
			else newDirection();
		    }
		    if (sprite instanceof Bubble) newDirection();
		    if (sprite instanceof PlayerSprite) { puf(); return; }
		}
	    }
	}
    }

    class Explosion extends SJGSpriteFA {
	int size = 0;
        Explosion(String explosion) {
            setAnimation(cropper.getAnimation(explosion));
	    size = cropper.getAnimation(explosion).size();
        }

        public void move() {
            nextFrame();
            if (getFrame() >= size) sprites.remove(this);
        }
    }

    class YellowBlobFlying extends SJGSprite {
	int count;
	public void move() {
	    count ++;
	    if (count == 12) synthManager.startTrack("hand");
	    if (count == 15) sprites.add(new YellowBlob("black"), getX(), getY());
	    if (count == 30) sprites.remove(this);	    
	}
        public void draw(Graphics g, View view) {
	    if (count < 15)
		g.drawImage(cropper.getFrame("black-blob-hand-1").getImage(), 
			    view.worldToRealX(getLeft()), view.worldToRealY(getTop() - Math.pow(-count+15, 2.7)) - 19, null);	
	    else
		g.drawImage(cropper.getFrame("hand-1").getImage(), 
			    view.worldToRealX(getLeft())-3, view.worldToRealY(getTop() - Math.pow(count-15, 2.7)) - 19, null);
        }
    }

    class YellowBlob extends SJGSprite {
        private int count = 0;
        private Animation animation;
        private int dx = 0;
        private int dy = 0;
        private int d = 0;
        private int looking = 0;
	private Random random;
	private String kind;
	private int speed = 4;

	YellowBlob(String kind) {
	    this.kind = kind;
	    setWidth(32);
	    setHeight(24);
	    if (getScreen() == openingAttract)
		random = new Random(10);
	    else
		random = new Random();

	    animation = cropper.getAnimation(kind+"-blob");
	    if (kind.equals("red")) speed = 3;
	    if (kind.equals("yellow")) speed = 4;
	    if (kind.equals("black")) speed = 5;	    
	}

	public boolean isJumping() {
	    return ((d==4) || (d==5));
	}

	private boolean canBlobGo(int direction) {
	    switch (direction) {
	    case 0:
		return ( (maze.getElement(getX() / maze.getBlockWidth() + 1, getY() / maze.getBlockHeight()).isSolid() == false) &&
		    ( (maze.getElement(getX() / maze.getBlockWidth() + 1, getY() / maze.getBlockHeight() + 1).isSolid() == true) || 
		      (maze.getElement(getX() / maze.getBlockWidth() + 1, getY() / maze.getBlockHeight() + 1) == stair) ) );
	    case 1:
		return (maze.getElement(getX() / maze.getBlockWidth(), getY() / maze.getBlockHeight() + 1) == stair);
	    case 2:
		return ( (maze.getElement(getX() / maze.getBlockWidth() - 1, getY() / maze.getBlockHeight()).isSolid() == false) &&
		    ( (maze.getElement(getX() / maze.getBlockWidth() - 1, getY() / maze.getBlockHeight() + 1).isSolid() == true) || 
		      (maze.getElement(getX() / maze.getBlockWidth() - 1, getY() / maze.getBlockHeight() + 1) == stair) ) );
	    case 3:
		return (maze.getElement(getX() / maze.getBlockWidth(), getY() / maze.getBlockHeight() - 1) == stair);
	    }
	    return false;
	}

	public void puf() {
	    /* if (maze.getElement(getX() / maze.getBlockWidth(), getY() / maze.getBlockHeight()) != stair) */
	    if (d < 4) {
		d = 4;
		synthManager.startTrack("muffle-up");
	    }
	}

        public void move() {
            count++;
            if (count >= speed) count = 0;

	    if (d == 4) {
		if (maze.canMove(this, getXdouble(), getYdouble() - 32 / 2.0)) {
		    translate(0, -32 / 2.0);
		} else {
		    translate(0, -32 / 2.0);
		    maze.touch(this);
		    translate(0, 32 / 2.0);
		    d = 5;
		}
	    } else if (d == 5) {
		if (maze.canMove(this, getXdouble(), getYdouble() + 32 / 2.0)) {
		    translate(0, 32 / 2.0);
		} else {
		    synthManager.startTrack("muffle-down");
		    sprites.remove(this);
		    sprites.add(new Explosion(kind+"-puf"), getX(), getY());
		    if (kind.equals("black")) sprites.add(new YellowBlob("red"), (getX() / 32)*32 + 16, (getY() / 32)*32 + 20);
		    score += 50;
		}
	    } else if (count == 0) {
                int od = d;
                int choices = 4;
                double ndl = 0.15;

		// if (maze.getElement(getX() / maze.getBlockWidth(), getY() / maze.getBlockHeight()) != stair) {

		if ( (maze.getElement(getX() / maze.getBlockWidth(), getY() / maze.getBlockHeight()) == stair) &&
		     ((d == 0) || (d == 2)) )
		    ndl = 0.6;

                do {
		    if (random.nextDouble() < 0.45) {
		        // chase player

			if ((random.nextDouble() > 0.5) && (playerSprite.getY() != this.getY())) {
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
			switch ((int) (4 * random.nextDouble())) {
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
                } while ((canBlobGo(d) == false) || ((random.nextDouble() > ndl) && (d != od)));

		if (d == 0) looking = 0;
		if (d == 2) looking = 1;
            }

	    if ((playerSprite.status != PlayerSprite.EXITING) && (d != 4) && (d != 5))
		translate(dx * (32.0 / speed), dy * (32.0 / speed));

            if (collidesWith(playerSprite)) playerSprite.die();
	    if (maze.getHeight() < getY()) translate(0, -maze.getHeight());
	    if (0 > getY()) if (getScreen() != openingAttract) translate(0, maze.getHeight());
        }

        public void draw(Graphics g, View view) {
            Image i = null;
            if (d == 4)
                i = animation.getFrame(8).getImage();
            else if (d == 5)
                i = animation.getFrame(9).getImage();
            else
                i = animation.getFrame(4 * looking+count%4).getImage();

	    g.drawImage(i, view.worldToRealX(getLeft()),
			view.worldToRealY(getTop()) - (count*3)%5, null);
        }
    }

    Screen game = new Screen() {
	    public void draw() {
		Graphics g = getGraphicsModel().getFront();
		g.setColor(backgroundColor);
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
		g.setColor(backgroundColor);
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
		    if (lives-1 > 0)
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
            g.setColor(backgroundColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            maze.draw(g, view);
            sprites.draw(g);
            g.drawImage(cropper.getFrame("gameover").getImage(), 280-cropper.getFrame("gameover").getWidth()/2, 
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
		    showScreen(openingHiscore);
        }
    };

    Screen writeHiscore = new Screen() {
	    public void enter() {
		backgroundColor = DEFAULT_BG_COLOR;
		synthManager.setListenToKeyboard(false);
	    }
	    public void exit() {
		synthManager.setListenToKeyboard(true);
	    }
	    public void draw() {
		Graphics g = getGraphicsModel().getFront();
		g.setColor(backgroundColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(cropper.getFrame("logo").getImage(), 
			    280-cropper.getFrame("logo").getWidth()/2, 50, null);

		hiscore.drawHiscoreEditor(g);
		drawText(g, 142, 323, "", "WRITE YOUR NAME AND PRESS RETURN");

		drawScore(g);
	    }
	    public void move() {
		if (hiscore.moveHiscoreEditor() == false) showScreen(openingHiscore);
	    }
	};

    Screen enterGame = new Screen() {
        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            g.setColor(backgroundColor);
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
	    
	    synthManager.startTrack("intro");

            maze = mazes.getMaze(savePoint);
	    starCount = 0;
	    bonus = 0;
            maze.init();

            playerSprite = null;

            scripts.setCallback(new Callback() {
                public void command(String name, String text) {
                    if (name.equals("next-level"))
                        nextLevel = text;
                    if (name.equals("skin")) {
                        skin = text;
			if (skin.equals("blue")) backgroundColor = new Color(34, 42, 48);
			else if (skin.equals("red")) backgroundColor = new Color(48, 43, 34);
			else if (skin.equals("green")) backgroundColor = new Color(60, 49, 61);
			else if (skin.equals("white")) backgroundColor = new Color(91, 106,115);
		    }
                    if (name.equals("bonus")) {
                        try {
			    bonus = Integer.parseInt(text); 
			} catch (Exception e) {
			    throw new Error(""+e);
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
                    else if (name.equals("yellow-blob-hand"))
                        sprites.add(new YellowBlobFlying(), x, y);
                    else if (name.equals("yellow-blob"))
                        sprites.add(new YellowBlob("yellow"), x, y);
                    else if (name.equals("black-blob"))
                        sprites.add(new YellowBlob("black"), x, y);
		    else if (name.equals("red-star"))
			sprites.add(new Star("red"), x, y);
		    else if (name.equals("blue-star"))
			sprites.add(new Star("blue"), x, y);
		    else if (name.startsWith("explosion"))
			sprites.add(new Explosion(name.substring(10)), x, y);
                    else if (name.equals("player") && (playerSprite == null))
                        sprites.add(playerSprite = new PlayerSprite(), x, y);
                }
            });

            scripts.move();
            view.reset();
        }
    };

    class Opening extends Screen {
	public void draw() {
	    Graphics g = getGraphicsModel().getFront();
	    g.setColor(backgroundColor);
	    g.fillRect(0, 0, getWidth(), getHeight());
	    g.drawImage(cropper.getFrame("logo").getImage(), 
			280-cropper.getFrame("logo").getWidth()/2, 50, null);
	    
	    MouseState ms = getLocalPlayer().getMouseState();
	    
	    if (within(ms.getX(), ms.getY(), 135, 320, 289, 20))
		g.drawImage(cropper.getFrame("click-here-mouse-over").getImage(), 135, 320, null);
	    else
		g.drawImage(cropper.getFrame("click-here").getImage(), 135, 320, null);
	    
	    drawScore(g);
	}
	
	public void move() {
	    MouseState ms = getLocalPlayer().getMouseState();
	    if ((within(ms.getX(), ms.getY(), 135, 320, 289, 20)) && (ms.countLeft() > 0))
		newGame();
	}
    }

    Screen openingHiscore = new Opening() {
	    public void enter() {
		getLocalPlayer().getMouseState().countLeft();
		backgroundColor = DEFAULT_BG_COLOR;
	    }
	    public void draw() {
		Graphics g = getGraphicsModel().getFront();
		super.draw();
		hiscore.drawHiscore(g);		
	    }
	    public void move() {
		super.move();
		if (getFramesSinceEnter() == 70) showScreen(openingAttract);
	    }
	};

    Screen openingAttract = new Opening() {
	    public void enter() {
		sprites.removeAll();
		scripts.clear();
		scripts.spawn("attract");
		maze = mazes.getMaze("attract");
		starCount = 0;
		maze.init();

		playerSprite = null;
		
		remoteController.command(" ");

		scripts.setCallback(new Callback() {
			public void command(String name, String text) {
			    if (name.equals("control"))
				remoteController.command(text);
			    else if (name.equals("next-level"))
				nextLevel = text;
			    else if (name.equals("skin")) {
				skin = text;
			    }
			    else if (name.equals("bonus")) {
				try {
				    bonus = Integer.parseInt(text); 
				} catch (Exception e) {
				    throw new Error(""+e);
				}
			    }			
			    else if (name.equals("find-exit-text"))
				findExitText = text;
			    else if (name.equals("sign"))
				sprites.add(new Sign(text), playerSprite.getX(), playerSprite.getY() - 50);
			}
			
			public void command(String name, int x, int y) {
			    if (name.equals("bubble"))
				sprites.add(new Bubble(), x, y);
			    else if (name.equals("yellow-blob-hand"))
				sprites.add(new YellowBlobFlying(), x, y);
			    else if (name.equals("yellow-blob"))
				sprites.add(new YellowBlob("yellow"), x, y);
			    else if (name.equals("black-blob"))
				sprites.add(new YellowBlob("black"), x, y);
			    else if (name.equals("red-star"))
				sprites.add(new Star("red"), x, y);
			    else if (name.equals("blue-star"))
				sprites.add(new Star("blue"), x, y);
			    else if (name.startsWith("explosion"))
				sprites.add(new Explosion(name.substring(10)), x, y);
			    else if (name.equals("player") && (playerSprite == null))
				sprites.add(playerSprite = new PlayerSprite(), x, y);
			}
		    });
		
		scripts.move();
		view.reset();
		
	    }
	    public void draw() {
		super.draw();

		Graphics g = getGraphicsModel().getFront();
		maze.draw(g, view);
		sprites.draw(g);
		drawScore(g);
	    }
	    public void move() {
		super.move();
		scripts.move();
		sprites.move();
		view.move();
		if (getFramesSinceEnter() == 230) showScreen(openingHiscore);
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
            if (i < lives-1)
                g.drawImage(cropper.getFrame("heart").getImage(), 10 + i * 32, 8, null);
    }

    class WallBlock extends Element {
	int y1 = 0;
	public boolean isSolid() {
	    return true;
	}
	public void draw(Graphics g, int x, int y) {
	    if (y1 < 0)
		g.drawImage(cropper.getFrame(skin+"-background-1").getImage(), x, y, null);
	    g.drawImage(cropper.getFrame(skin+"-solid-1").getImage(), x, y+y1, null);
	    if (y1 < 0) y1+=2;
	}
	public void smallJump(int x, int y) {
	    if (y1 == 0) y1 = -6;
	}
	public Element touch(SJGSprite sprite, int x, int y) {
	    y1 = -10;
	    synthManager.startTrack("hit-roof");
	    int xm = x / maze.getBlockWidth();
	    int ym = y / maze.getBlockHeight();
	    
	    if ((xm > 0) && (maze.getElement(xm-1, ym) instanceof WallBlock)) ((WallBlock)maze.getElement(xm-1, ym)).smallJump(32*(xm-1), 32*ym);
	    if ((xm < maze.getWidth()/maze.getBlockWidth()-1) && (maze.getElement(xm+1, ym) instanceof WallBlock)) 
		((WallBlock)maze.getElement(xm+1, ym)).smallJump(32*(xm+1), 32*ym);

	    for (Enumeration e = sprites.elements(); e.hasMoreElements(); ) {
		SJGSprite s = (SJGSprite)e.nextElement();
		if ((s instanceof YellowBlob) && (within(s.getX(), s.getY(), x-50+16, y-32, 100, 32)))
		    ((YellowBlob)s).puf();
		if ((s instanceof Bubble) && (within(s.getX(), s.getY(), x-50+16, y-32, 100, 32)))
		    ((Bubble)s).puf();
	    }

	    return this;
	}
    }

    class MysteryBlock extends WallBlock {
	int form = 0;
	public MysteryBlock() {
	    starCount += 5;
	}
	public void draw(Graphics g, int x, int y) {
	    if (y1 < 0)
		g.drawImage(cropper.getFrame(skin+"-background-1").getImage(), x, y, null);

	    if (form == 0) {
		Animation a = cropper.getAnimation(skin+"-mystery");
		g.drawImage(a.getFrame(getFrameCount() % a.size()).getImage(), x, y+y1, null);
	    }
	    if (form == 1) g.drawImage(cropper.getFrame(skin+"-solved-mystery-1").getImage(), x, y+y1, null);
	    if (y1 < 0) y1+=2;
	}
	public void smallJump(int x, int y) {
	    super.smallJump(x, y);
	    pop(x, y);
	}
	public void pop(int x, int y) {
	    if (form == 0) {
		form = 1;
		starCount -= 5;
		sprites.add(new SoapBubble(), x+16, y-24);		
		synthManager.startTrack("hit-mystery");
	    }
	}
	public Element touch(SJGSprite sprite, int x, int y) {
	    super.touch(sprite, x, y);
	    pop(x, y);
	    return this;
	}
    }

    Element ground = new Element() {
        public boolean isSolid() {
            return false;
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(cropper.getFrame(skin+"-background-1").getImage(), x, y, null);
        }
    };

    Element exit_door = new Element() {
        public boolean isSolid() {
            return false;
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(cropper.getFrame(skin+"-background-1").getImage(), x, y, null);
	    if (starCount == 0)
		g.drawImage(cropper.getAnimation("exit-sign").getFrame(getFrameCount()).getImage(), x, y, null);
        }

        public Element touch(SJGSprite sprite, int x, int y) {
            if ((sprite instanceof PlayerSprite) && (starCount == 0)) {
                playerSprite.exit();
		score += bonus;
		if (bonus > 0) sprites.add(new Sign("BONUS "+bonus), playerSprite.getX(), playerSprite.getY()-28);
		synthManager.startTrack("completion");
	    }
            return this;
        }
    };

    Element stair = new Element() {
        public boolean isSolid() {
            return false;
        }

        public void draw(Graphics g, int x, int y) {
            g.drawImage(cropper.getFrame(skin+"-background-1").getImage(), x, y, null);
            g.drawImage(cropper.getFrame("stair").getImage(), x, y, null);
        }

        public Element touch(SJGSprite sprite, int x, int y) {
            return this;
        }
    };

    Element portal_bubbles = new Element() {
        public boolean isSolid() {
            return false;
        }

        public void draw(Graphics g, int x, int y) {
	    Animation a = cropper.getAnimation("portal-bubbles");
            g.drawImage(cropper.getFrame(skin+"-background-1").getImage(), x, y, null);
	    if (y < 32) g.drawImage(a.getFrame((getFrameCount()) % a.size()).getImage(), x, y-13 - (x+getFrameCount()) % 5, null);
	    else g.drawImage(a.getFrame((getFrameCount()) % a.size()).getImage(), x, y+13 + (x+getFrameCount()) % 5, null);
        }

        public Element touch(SJGSprite sprite, int x, int y) {
            return this;
        }
    };

    public void drawText(Graphics g, int x, int y, String font, String text) {
        int j = x;
        for (int i = 0; i < text.length(); i++) {
            g.drawImage(cropper.getFrame(font + text.charAt(i)).getImage(), j, y, null);
            j += cropper.getFrame(font + text.charAt(i)).getWidth();
        }
    }

    public void init() {
	synthManager = new SynthManager(this);
	synthManager.addTrack("die", "o - c 0 f 440 l 0.6 v 0.04 w square e ping w2 sine f2 54 v2 0.9 e2 ping q 0.2 u 5000 C3 1 u 3000 A 1 u 2000 l 1.6 B 8");

	//u 2000  B 4 l 1.6 u 1500 A 8

	//"o - c 0 f 220 l 0.6 v 0.1 w sine e ping w2 sine f2 55.25 v2 0.9 e2 ping b 0.1 C3 1 b 0.2 A 1 b 0.4 C3 4 b 0.6 B 4 l 1.6 b 0.7 A 4");
	//"o - c 2 f 220 l 0.2 v 0.2 w sine e ping b 0 C3 1 A 1 C3 4 B 4 A 4");
	synthManager.addTrack("blop", "o - c 2 f 220 l 0.2 v 0.03 u 4000 w saw e ping C3 1");
	synthManager.addTrack("intro", "o - c 2 f 220 l 0.1 v 0.1 w square e ping w2 sine f2 110.5 e2 ping q 0.4 v2 0.6 u 500 C3 3 u 900 G3 3 u 600 C3 2 u 1200 G3 2");

	synthManager.addTrack("hand", "o - c 3 f 440 l 0.6 v 0.06 w square e ping w2 sine f2 54 v2 0.9 e2 ping q 0.2 u 5000 C 2 u 4000");

	/*
o - c 2 f 220 l 0.6 v 0.1 w sine e ping w2 sine f2 110.5 e2 ping u 2000 q 1 v2 0.6 A 3 A 1 C3 3 C3 1 v2 0.5 D3 3 D3 1 v2 0.4 E3 6");

o - c 2 f 220 l 0.6 v 0.1 w sine e ping w2 sine f2 110.5 e2 ping 
u 2000 q 1 v2 0.6 A 3 A 1 C3 3 C3 1 v2 0.5 D3 3 D3 1 v2 0.4 E3 6
	*/
	// o - c 0 f 220 l 0.8 v 0.2 w sine w2 sine f2 4 v2 0.7 e ping b 0 A 3 A 1 C3 3 C3 1 D3 3 D3 1 E3 6");
	synthManager.addTrack("point", "o - c 2 f 440 l 0.07 v 0.2 w square e ping q 0.3 u 800 C 1 u 500 G 1");
	synthManager.addTrack("muffle-up", "o - c 2 f 440 l 0.07 v 0.07 w saw e ping u 700 q 0.5  C3 1");
	synthManager.addTrack("muffle-down", "o - c 2 f 440 l 0.07 v 0.07 w saw e ping u 700 q 0.5 C 1");
	synthManager.addTrack("hit-roof", "o - c 3 f 440 l 0.05 v 0.10 w noise e ping u 2000 q 0.5 C 1");
	synthManager.addTrack("lattice", "o - c 3 f 440 l 0.2 v 0.06 w sine e ping w2 sine f2 210 v2 0.9 e2 ping q 0.5 u 5000 C3 1 u 3000 B 1 u 2000 A 1 u 1000 G 1");
	synthManager.addTrack("hit-mystery", "o - c 3 f 440 l 0.1 v 0.12 w sine e ping w2 sine f2 438 v2 0.9 e2 ping q 0.4 u 3000 C 1 u 2500 D 1 u 2000 E 1 u 1500 F 1 u 1000 G 1");
	synthManager.addTrack("soap-pop", "o - c 3 f 440 l 0.1 v 0.15 w sine e ping w2 sine f2 438 v2 0.9 e2 ping q 0.4 u 1000 C 1 u 1500 D 1 u 2000 C 1 u 2500 F 1 u 1000 G 1");
	synthManager.addTrack("find-exit", "o - c 2 f 220 l 0.1 v 0.07 w square e ping q 0.5 u 1500 G 1 u 1200 E 1 u 1000 G 2 u 800 B 2");
	synthManager.addTrack("completion", "o - c 0 f 440 l 0.6 v 0.1 w sine e ping w2 sine f2 54 v2 0.9 e2 ping q 0.2 u 2000 C 1 u 500 G 1 u 2000 D 1 u 500 G 1 u 1000 D 2 ");
	//"o - c 2 f 440 l 0.2 v 0.3 w sine e ping b 0 C 2 D 2 C 2 E 2");
	synthManager.addTrack("star", "o - c 3 f 440 l 0.05 v 0.03 w noise u 0 e ping C 1");
	synthManager.addTrack("bassline-1", "o + c 0 f 55 l 0.17 v 0.17 w sine e ping w2 sine v2 0.0 f 110 e2 ping u 4000 q 1 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 B 2 G3 2 B 2 G3 2 B 2 G3 2 B 2 G3 2 B 2 G3 2 B 2 G3 2 B 2 G3 2 B 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 A 2 E3 2 A 2 E3 2 A 2 E3 2 A 2 E3 2 A 2 E3 2 A 2 E3 2 A 2 E3 2 A 2 E3 2");

	/*

o + c 0 f 55 l 0.2 v 0.2 w sine e ping w2 sine v2 0.0 f 110 e2 ping u 4000 q 1 
C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 
B 2 G3 2 B 2 G3 2 B 2 G3 2 B 2 G3 2 B 2 G3 2 B 2 G3 2 B 2 G3 2 B 2 G3 2 
C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 C3 2 G3 2 
A 2 E3 2 A 2 E3 2 A 2 E3 2 A 2 E3 2 A 2 E3 2 A 2 E3 2 A 2 E3 2 A 2 E3 2

	*/
	// C 2 C3 1 G 1 C 2 C3 1 G 1 C 2 C3 1 G 1 C 1 C 1 G 1 C3 1");
	synthManager.addTrack("bassline-1-exit", "o + c 0 f 110 l 0.1 v 0.15 w saw e ping q 0.2 u 500 C 1 u 400 C 1  u 300 C 2 C3 3 C3 1 u 500 C 4 u 300 C3 4 u 500 C 1 u 400 C 1  u 300 C 2 C3 3 u 400 C3 1 u 500 C 2 u 600 C 2 u 300 C3 4");


			      //"o + c 0 f 220 l 0.05 v 0.025 w noise e ping f2 5 e2 flat v2 0.5 w2 noise b 0.1 C 4 C3 4 C 4 C3 4 b 0.2 C 4 C3 3 C3 1 C 4 C3 4 b 0.4 C 4 C3 4 C 4 C3 4 b 0.8 C 4 C3 3 C3 1 C 4 C3 4 b 0.4 C 4 C3 4 C 4 C3 4 b 0.2 C 4 C3 3 C3 1 C 4 C3 4 b 0.1 C 4 C3 4 C 4 C3 4 b 0.0 C 4 C3 3 C3 1 C 4 C3 4");

	synthManager.addTrack("bassline-2", "o + c 1 f 110 l 0.1 v 0.17 w square e ping w2 sine v2 0.4 f2 109 e2 ping u 1000 q 0.3 u 500 C3 3 G3 3 C3 2 G3 2 C3 1 F3 2 E3 3 u 550 C3 3 G3 3 C3 2 G3 2 C3 1 u 600 F3 2 E3 3 B 3 G3 3 B 2 G3 2 B 1 u 650  F3 2 E3 3 B 3 G3 3 B 2 G3 2 B 1 u 700 F3 2 E3 3 C3 3 G3 3 C3 2 G3 2 C3 1 u 650 F3 2 E3 3 C3 3 G3 3 C3 2 G3 2 C3 1 u 600 F3 2 E3 3 A 3 E3 3 A 2 E3 2 A 1 C3 2 u 550 D3 3 A 3 E3 3 A 2 E3 2 A 1 C3 2 D3 3");

	// synthManager.addTrack("bassline-2", "o + c 1 f 55 l 0.2 v 0.2 w sine e ping w2 sine v2 0.0 f 110 e2 ping u 4000 q 1 C3 3 G3 3 C3 2 G3 2 C3 1 F3 2 E3 3 C3 3 G3 3 C3 2 G3 2 C3 1 F3 2 E3 3 B 3 G3 3 B 2 G3 2 B 1 F3 2 E3 3 B 3 G3 3 B 2 G3 2 B 1 F3 2 E3 3 C3 3 G3 3 C3 2 G3 2 C3 1 F3 2 E3 3 C3 3 G3 3 C3 2 G3 2 C3 1 F3 2 E3 3 A 3 E3 3 A 2 E3 2 A 1 C3 2 D3 3 A 3 E3 3 A 2 E3 2 A 1 C3 2 D3 3");

	/*
o + c 1 f 55 l 0.2 v 0.2 w sine e ping w2 sine v2 0.0 f 110 e2 ping u 4000 q 1 
C3 3 G3 3 C3 2 G3 2 C3 1 F3 2 E3 3 C3 3 G3 3 C3 2 G3 2 C3 1 
F3 2 E3 3 B 3 G3 3 B 2 G3 2 B 1 F3 2 E3 3 B 3 G3 3 B 2 G3 2 B 1 
F3 2 E3 3 C3 3 G3 3 C3 2 G3 2 C3 1 F3 2 E3 3 C3 3 G3 3 C3 2 G3 2 C3 1 
F3 2 E3 3 A 3 E3 3 A 2 E3 2 A 1 C3 2 D3 3 A 3 E3 3 A 2 E3 2 A 1 C3 2 D3 3

	*/
// E 4 E3 4 E 1 E3 3 E3 4 F 4 F3 3 F3 1 F 1 F3 3 F3 4 E 4 E3 4 E 1 E3 3 E3 4 G 4 G3 3 G3 1 G 1 G3 3 G3 4");


/*

o + c 1 f 110 l 0.1 v 0.2 w square e ping w2 sine v2 0.4 f2 109 e2 ping u 1000 q 0.2 
u 500 C3 3 G3 3 C3 2 G3 2 C3 1 F3 2 E3 3 u 550 C3 3 G3 3 C3 2 G3 2 C3 1 
u 600 F3 2 E3 3 B 3 G3 3 B 2 G3 2 B 1 u 650  F3 2 E3 3 B 3 G3 3 B 2 G3 2 B 1 
u 700 F3 2 E3 3 C3 3 G3 3 C3 2 G3 2 C3 1 u 650 F3 2 E3 3 C3 3 G3 3 C3 2 G3 2 C3 1 
u 600 F3 2 E3 3 A 3 E3 3 A 2 E3 2 A 1 C3 2 u 550 D3 3 A 3 E3 3 A 2 E3 2 A 1 C3 2 D3 3
*/


	synthManager.addTrack("bassline-2-exit", "o + c 1 f 220 l 0.08 v 0.02 w saw e ping f2 120 v2 1.0 w2 noise e2 ping q 0.5 u 4000 C3 1 v 0.01 C3 1 v 0.02 u 3500 C 1 v 0.01 C3 1 u 3000 C3 1 v 0.01 C3 1 v 0.02 u 2900 C 1 v 0.01 C3 1 u 2700 C3 1 v 0.01 C3 1 v 0.02 u 2450 C 1 v 0.01 C3 1 u 2250 C3 1 v 0.01 C3 1 v 0.02 u 2150 C 1 v 0.01 C3 1 u 2100 C3 1 v 0.01 C3 1 v 0.02 u 2150 C 1 v 0.01 C3 1 u 2250 C3 1 v 0.01 C3 1 v 0.02 u 2450 C 1 v 0.01 C3 1 u 2700 C3 1 v 0.01 C3 1 v 0.02 u 2900 C 1 v 0.01 C3 1 u 3000 C3 1 v 0.01 C3 1 v 0.02 u 3500 C 1 v 0.01 C3 1");

	//o + c 1 f 220 l 0.06 v 0.02 w noise e ping f2 120 v2 1.0 w2 noise e2 ping b 0.1 C3 1 v 0.01 C3 1 v 0.02 b 0.14 C 1 v 0.01 C3 1 b 0.2 C3 1 v 0.01 C3 1 v 0.02 b 0.28 C 1 v 0.01 C3 1 b 0.4 C3 1 v 0.01 C3 1 v 0.02 b 0.55 C 1 v 0.01 C3 1 b 0.8 C3 1 v 0.01 C3 1 v 0.02 b 0.84 C 1 v 0.01 C3 1 b 0.9 C3 1 v 0.01 C3 1 v 0.02 b 0.84 C 1 v 0.01 C3 1 b 0.8 C3 1 v 0.01 C3 1 v 0.02 b 0.65 C 1 v 0.01 C3 1 b 0.4 C3 1 v 0.01 C3 1 v 0.02 b 0.32 C 1 v 0.01 C3 1 b 0.2 C3 1 v 0.01 C3 1 v 0.02 b 0.16 C 1 v 0.01 C3 1");
	// o + c 1 f 110 l 0.06 v 0.02 w noise e flat b 0.3 C3 1 v 0.01 C3 1 v 0.02 C 1 v 0.01 C3 1");

        setGraphicsModel(new sjg.gm.Buffered(this));
        showScreen(openingHiscore);
        cropper = new Cropper(this);
        scripts = new ScriptEngine(this);
        mazes = new Mazes(this, new ElementFactory() {
            public Element getElement(char c) {
                switch (c) {
		case 'w':
		    return new WallBlock();
		case 'm':
		    return new MysteryBlock();
		case '.':
		    return ground;
		case 's':
		    return stair;
		case 'E':
		    return exit_door;
		case 'p':
		    return portal_bubbles;		    
                }
                return NullElement.getInstance();
            }
        });
	hiscore = new Hiscore(this, new HttpGetHiscoreServer(this, "/hiscore/list-hiscores.php", "/hiscore/add-hiscore.php")) {
		public void drawHiscoreText(int position, String positionText, String entryText, int score, String round) {
		    Graphics g = getGraphicsModel().getFront();
		    int y = 145+position*16;
		    if (position==9)
			drawText(g, 117, y, "", positionText);
		    else
			drawText(g, 125, y, "", positionText);
		    drawText(g, 165, y, "", entryText);
		    drawText(g, 360, y, "", round);
		    drawText(g, 395, y, "", ""+score);
		}
		public void drawWriteHiscoreText(int position, String positionText, String entryText, int score, String round) {
		    drawHiscoreText(position, positionText, entryText, score, round);
		}
	    };
    }
}
