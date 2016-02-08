import sjg.*;
import sjg.synth.*;
import sjg.animation.*;
import sjg.scripting.*;
import sjg.hiscore.*;

import java.awt.*;
import java.util.*;

public class Timbuktu extends SJGame {
    final int WORLD_WIDTH = 350;

    ScriptEngine scripts;
    PlayerSprite playerSprite;
    StarField starField;
    Cropper cropper;
    int addingCircler;
    int level, levelFrameCount;
    int lives, score;
    String savepoint;
    Hiscore hiscore;
    // SynthManager synthManager;

    // View

    class TView implements View {
        private double cameraX;
        private int screenWidth = 560;
        private double dx = 0;

        public int worldToRealX(double x) {
            return (int) x - (int) cameraX + screenWidth / 2;
        }

        public int worldToRealY(double y) {
            return (int) y;
        }

        public double realToWorldX(int x) {
            return x + cameraX - screenWidth / 2;
        }

        public double realToWorldY(int y) {
            return y;
        }

        public void move() {
            if (worldToRealX(playerSprite.getX()) < 80) cameraX = playerSprite.getX() + screenWidth / 2 - 80;
            else if (worldToRealX(playerSprite.getX()) < 200) cameraX -= 8;

            if (worldToRealX(playerSprite.getX()) > screenWidth - 80)
                cameraX = playerSprite.getX() - screenWidth / 2 + 80;
            else if (worldToRealX(playerSprite.getX()) > screenWidth - 200) cameraX += 8;

            if (-WORLD_WIDTH - cameraX + screenWidth / 2 > 32)
                cameraX = -WORLD_WIDTH + screenWidth / 2 - 32;
            if (WORLD_WIDTH - cameraX + screenWidth / 2 < screenWidth - 32)
                cameraX = WORLD_WIDTH - screenWidth / 2 + 32;
        }

        public double getCameraX() {
            return cameraX;
        }

        public void reset() {
            cameraX = 0;
        }
    }

    ;

    TView view = new TView();

    // Sprites

    Sprites sprites = new Sprites(view);

    private static float limit(float x) {
        if (x < 0) return 0.0f;
        if (x > 1) return 1.0f;
        return x;
    }

    class Bonus extends Enemy {
        int count = 0;
        int size = 15;

        public Bonus() {
        }

        public void move() {
            count++;
            if (count > 120) sprites.remove(this);
        }

        public void draw(Graphics g, View view) {
            int w = 20 + (200 / count);
            int h = 20 + (40 / count);
            if (count > 100) {
                w = 120 - count;
                h = 120 - count;
            }
            g.setColor(new Color(220, 210, 255));
            for (int i = 0; i < size; i++) {
                double r = count * 0.1 + i * Math.PI * 2 / size;
                int x = view.worldToRealX(getX()) + (int) Math.round(w * Math.cos(r));
                int y = view.worldToRealY(getY()) + (int) Math.round(h * Math.sin(r));
                g.fillRect(x, y, 2, 2);

            }
            super.draw(g, view);
        }
    }

    class ShieldBonus extends Bonus {
        public ShieldBonus() {
            setAnimation(cropper.getAnimation("shieldbonus"));
        }

        public void die() {
            super.die();
            playerSprite.shield = true;
        }
    }

    class WeaponBonus extends Bonus {
        public WeaponBonus() {
            setAnimation(cropper.getAnimation("weaponbonus"));
        }

        public void die() {
            super.die();
            playerSprite.sideBullet = true;
        }
    }

    class Explosion extends SJGSprite {
        int size;
        int time;
        int count = 0;
        float cr, cg, cb;
        double angle = Math.random() * Math.PI * 2;
        boolean forward;

        public Explosion(int size, int time, float r, float g, float b) {
            super();
            if (time < 0) {
                this.time = -time;
                forward = false;
                count = -time;
            } else {
                this.time = time;
                forward = true;
            }

            this.size = size;
            cr = r;
            cg = g;
            cb = b;
        }

        public void move() {
            if (forward) {
                count++;
                if (count >= time)
                    sprites.remove(this);
            } else {
                count--;
                if (count < 0)
                    sprites.remove(this);
            }
        }

        public void draw(Graphics g, View view) {
            g.setColor(new Color(limit(cr * (time - count) / time), limit(cg * (time - count) / time), limit(cb * (float) Math.pow((double) (time - count) / time, 2.2d))));
            for (int i = 0; i < size; i++) {
                int x = view.worldToRealX(getX()) + (int) Math.round(count * count * 0.2 * Math.cos(angle + 2 * Math.PI * i / size + count * 0.01));
                int y = view.worldToRealY(getY()) + (int) Math.round(count * count * 0.2 * Math.sin(angle + 2 * Math.PI * i / size + count * 0.011));
                g.fillRect(x - 1, y - 1, 4, 4);

            }
        }
    }

    class PlayerSprite extends SJGSpriteFA {
        int canon = 0;
        int bigcanon = 0;
        int dx = 0;
        int delta = 0;
        boolean shield = false;
        boolean sideBullet = false;

        public PlayerSprite() {
            setAnimation(cropper.getAnimation("rumskib"));
        }

        public void move() {
            KeyboardState ks = getLocalPlayer().getKeyboardState();
            if (ks.isDown(37)) dx -= 5;
            else {
                if (ks.isDown(39)) dx += 5;
                else {
                    if (dx < -3) dx += 3;
                    else {
                        if (dx > 3) dx -= 3;
                        else
                            dx = 0;
                    }
                }
            }

            if (ks.isDown(37)) setFrame(3);
            else {
                if (ks.isDown(39)) setFrame(2);
                else
                    setFrame(1);
            }

            if (dx > 16) dx = 16;
            if (dx < -16) dx = -16;

            translate(dx, 0);

            if (getX() < -WORLD_WIDTH) setX(-WORLD_WIDTH);
            if (getX() > WORLD_WIDTH) setX(WORLD_WIDTH);

            if (canon > 0) canon--;
            if (bigcanon > 0) bigcanon--;

            if ((canon == 0) && (ks.isDown(32))) {
                double angle = 0;
                if (ks.isDown(37)) angle = -10.0 / 360 * 2 * Math.PI;
                else {
                    if (ks.isDown(39)) angle = +10.0 / 360 * 2 * Math.PI;
                    else
                        angle = 0;
                }

                sprites.add(new Bullet(angle), getX(), getY());

                if (sideBullet) {
                    sprites.add(new SideBullet(angle, 1), getX(), getY());
                    sprites.add(new SideBullet(angle, -1), getX(), getY());
                }
                canon = 8;
                //		setFrame(4);
            }

            if ((bigcanon == 0) && (ks.isDown(32))) {
                //
                // sprites.add(new LaserBullet(),getX(), getY());
                bigcanon = 16;
                //		setFrame(4);

            }

            if (ks.isDown(32) == false) canon = 0;

            if (getFrame() > 0) prevFrame();
        }

        void die() {

            if ((getScreen() != die) && (getScreen() != gameOver)) {
                if (shield) shield = false;
                else {
                    showScreen(die);
                    sprites.remove(this);
                    sprites.add(new Explosion(50, 15, 0.2f, 1.0f, 1.0f), getX(), getY());
                }
            }
        }

        public void draw(Graphics g, View view) {
            if (shield) {
                g.setColor(new Color((delta % 33) + 170, 20, (delta % 33) * 4 + 120));

                delta++;

                for (int i = 0; i < 10; i++) {
                    double r = Math.PI * i / 5 + (delta * 0.1);

                    int tx = view.worldToRealX(getX() + Math.cos(r) * 22 - 1);
                    int ty = view.worldToRealY(getY() + Math.sin(r) * 22);

                    g.fillRect(tx - 0, ty - 0, 2, 2);
                }
            }
            super.draw(g, view);
        }
    }

    class EnemyBullet extends SJGSprite {
        int count = 0;
        float r, g, b;

        public EnemyBullet() {
            super();
            setWidth(8);
            setHeight(8);
            r = 1.4f;
            g = 0.3f;
            b = 0.2f;

        }

        public void move() {
            count++;
            translate(0, 6 + count * 0.25);

            if (collidesWith(playerSprite)) {
                playerSprite.die();
                sprites.add(new Explosion(4, 12, r, g, b), getX(), getY());
                sprites.remove(this);

            }

            if (getY() > 400) {
                sprites.remove(this);

            }
        }

        public void draw(Graphics g, View view) {
            g.setColor(new Color(limit(r * (0.02f * (30 + count))), limit(this.g * 0.02f * (30 + count)), limit(b * 0.02f * (30 + count))));
            int dx = (int) (4 * Math.cos(count * 1.1));
            int dy = (int) (4 * Math.sin(count * 1.1));

            int tx = view.worldToRealX(getX());
            int ty = view.worldToRealY(getY());

            g.fillRect(tx - 1 + dx, ty - 1 + dy, 3, 3);
            g.fillRect(tx - 1 - dx, ty - 1 - dy, 3, 3);

        }

    }

    class TSEnemyBullet extends EnemyBullet {
        double dx = 0;

        TSEnemyBullet() {
            super();
            r = 0.7f;
            g = 1.4f;
            b = 0.2f;
        }

        public void move() {
            super.move();
            double dx1 = (playerSprite.getX() - getX()) * 0.015;
            if (Math.abs(dx1) < 2)
                dx = dx * 0.85 + dx1 * 0.85;
            else
                dx = dx1;
            translate(dx, 0);
        }
    }

    class Enemy extends SJGSpriteFA {
        public void die() {
            sprites.remove(this);
            sprites.add(new Explosion(8, 16, 0.3f, 1.0f, 0.9f), getX(), getY());
        }

        public int getScore() {
            return 0;
        }
    }

    class Saucer extends Enemy {
        double dx = 0;

        public Saucer() {
            setAnimation(cropper.getAnimation("saucer"));
        }

        public void move() {
            dx -= getX() / 25d;
            translate(dx, 4);
            nextFrame();

            if (collidesWith(playerSprite)) {
                playerSprite.die();
                die();
            }

            if (getY() > 400)
                sprites.remove(this);

        }

        public int getScore() {
            return 10;

        }
    }

    class Silver extends Enemy {
        int count = 0;

        public Silver() {
            setAnimation(cropper.getAnimation("silver"));

        }

        public void move() {

            count++;
            nextFrame();

            switch ((count % 160) / 20) {
                case 0:
                    translate(0, 8);
                    break;
                case 2:
                    translate(8, 0);
                    break;
                case 4:
                    translate(0, -8);
                    break;
                case 6:
                    translate(-8, 0);
                    break;
                case 1:
                case 3:
                case 5:
                case 7:
                    if (count % 2 == 0) prevFrame();
                    if (count % 10 == 0)
                        sprites.add(new EnemyBullet(), getX(), getY());

                    break;

            }

            if (collidesWith(playerSprite)) {
                playerSprite.die();
                die();
            }

            if (count > 400)
                sprites.remove(this);

        }

        public int getScore() {
            return 100;

        }
    }


    class Gold extends Enemy {
        int count = 0;

        public Gold() {
            setAnimation(cropper.getAnimation("gold"));

        }

        public void die() {
            sprites.remove(this);
            sprites.add(new Explosion(6, 20, 0.8f, 1.0f, 0.4f), getX(), getY());

        }

        public void move() {
            count++;
            nextFrame();

            switch ((count % 160) / 20) {
                case 0:
                    translate(-8, 8);
                    break;
                case 1:
                    if (count % 2 == 0) prevFrame();
                    if (count % 20 == 0)
                        sprites.add(new TSEnemyBullet(), getX(), getY());

                    break;
                case 2:
                    translate(8, 0);
                    break;
                case 3:
                    if (count % 2 == 0) prevFrame();
                    if (count % 20 == 0)
                        sprites.add(new TSEnemyBullet(), getX(), getY());
                    break;
                case 4:
                    translate(8, 0);
                    break;
                case 5:
                    if (count % 2 == 0) prevFrame();
                    if (count % 20 == 0)
                        sprites.add(new TSEnemyBullet(), getX(), getY());

                    break;
                case 6:
                    translate(-8, -8);
                    break;
                case 7:
                    if (count % 2 == 0) prevFrame();
                    break;

            }

            if (collidesWith(playerSprite)) {
                playerSprite.die();
                die();
            }

            if (count > 400)
                sprites.remove(this);

        }

        public int getScore() {
            return 100;

        }
    }

    class ZigZagger extends Enemy {
        double dx = 0;

        public ZigZagger() {
            setAnimation(cropper.getAnimation("zigzagger"));

        }

        public void move() {
            dx += ((Timbuktu.this.getWidth() / 2) - getX()) / 20d;
            translate(dx, 4 + getYdouble() / 25d);
            nextFrame();

            if (collidesWith(playerSprite)) {
                playerSprite.die();
                die();
            }

            if (getY() > 400)
                sprites.remove(this);

        }

        public int getScore() {
            return 10;

        }
    }

    class Jumper extends Enemy {
        double dx = 0;
        double dy = 0;

        public Jumper() {
            setAnimation(cropper.getAnimation("zigzagger"));

        }

        public void move() {
            translate(dx, dy);
            nextFrame();
            if ((getXdouble() < -WORLD_WIDTH) || (getXdouble() > WORLD_WIDTH))
                dx = -dx;

            if (getY() < 5) {
                dy = 3 + Math.random() * 20;
                dx = Math.random() * 20 - 10;
                sprites.add(new TSEnemyBullet(), getX(), getY());

            }

            dy--;

            if (collidesWith(playerSprite)) {
                playerSprite.die();
                die();
            }
        }

        public int getScore() {
            return 25;

        }
    }

    class Flat extends Enemy {
        double dx = 0;
        double dy = 0;
        int time = 300;
        int dtime = -1;
        double direction;

        public void die() {
            sprites.remove(this);
            sprites.add(new Explosion(20, 10, 0.2f, 0.5f, 1.0f), getX(), getY());

        }

        public Flat(double direction) {
            this.direction = direction;
            setAnimation(cropper.getAnimation("flat"));

        }

        public void move() {
            translate(dx, dy);
            translate(direction * Math.cos(time * 0.12) * (300 - time) * 0.1,
                    Math.sin(time * 0.1) * (300 - time) * 0.1);

            nextFrame();
            time += dtime;

            if (time % 20 == 0)
                sprites.add(new EnemyBullet(), getX(), getY());


            if (time == 150) {
                dtime = 1;

            }

            if (time == 300) {
                die();
                sprites.add(new TSEnemyBullet(), getX(), getY());

            }

            if (collidesWith(playerSprite)) {
                playerSprite.die();
                die();
            }
        }

        public int getScore() {
            return 25;

        }
    }

    class Circler extends Enemy {
        int time = 300;
        double direction = 0;
        double speed = 0.15;
        double skew = 1;

        public Circler(double direction) {
            super();
            this.direction = direction;
            setAnimation(cropper.getAnimation("circler"));
        }

        public void move() {
            translate(direction * Math.cos(time * 0.1) * (300 - time) * speed,
                    Math.sin(skew * time * direction * 0.1) * (300 - time) * speed);

            time--;
            nextFrame();

            if (collidesWith(playerSprite)) {
                playerSprite.die();
                die();
            }
            if (time < 0)
                sprites.remove(this);

        }

        public int getScore() {
            return 2;

        }
    }

    class LaserBullet extends SJGSprite {
        double speed = 5;

        public void move() {
            translate(0, -(int) Math.round(speed));
            speed *= 1.06;

            for (Enumeration e = sprites.elements(); e.hasMoreElements();) {
                SJGSprite s = (SJGSprite) e.nextElement();

                if ((s instanceof Enemy) && (collidesWith(s))) {
                    ((Enemy) s).die();
                    score += ((Enemy) s).getScore();
                    sprites.remove(this);
                    break;

                }
            }

            if (getY() < 0)
                sprites.remove(this);

        }

        public void draw(Graphics g, View view) {
            double w = Math.sin((speed - 5) / 8);
            g.setColor(new Color(0.2f * (float) Math.pow((double) (w), 2.2d), (float) (0.7 * (w)), (float) (0.4 * ((1 - w)) + 0.15)));
            setWidth((int) Math.round(w * 170));

            int tx = view.worldToRealX(getX());
            int ty = view.worldToRealY(getY());

            g.fillRect(tx - getWidth() / 2, ty - 1, getWidth(), 1 + (int) Math.round(w * 4));

        }
    }

    class LevelSprite extends SJGSprite {
        int count;
        String text;

        public LevelSprite(String text) {
            this.text = text;
        }

        public void move() {
            count++;
            if (count > 15) sprites.remove(this);
        }

        public void draw(Graphics g, View view) {
            int tx1 = view.worldToRealX(getX() - count * count - 8);
            int tx2 = view.worldToRealX(getX() + count * count + 8);
            int ty = view.worldToRealY(getY());

            if ((count > 8) && (count % 2 == 0)) return;

            g.drawImage(cropper.getFrame("L" + text.substring(0, 1)).getImage(), tx1, ty, null);
            g.drawImage(cropper.getFrame("L" + text.substring(1, 2)).getImage(), tx2, ty, null);
        }
    }

    class Bullet extends SJGSprite {
        double speed = 20;
        double angle;
        double f = 0.3+0.3*Math.random();

        public Bullet(double angle) {
            this.angle = -Math.PI / 2 + angle;
            setHeight(18);
            setWidth(32);
            // synthManager.startTrack("blop");
        }

        public void move() {
            translate(Math.cos(angle) * speed, Math.sin(angle) * speed);
            if (speed > 1) speed = speed * 0.96;
            for (Enumeration e = sprites.elements(); e.hasMoreElements();) {
                SJGSprite s = (SJGSprite) e.nextElement();
                if ((s instanceof Enemy) && (collidesWith(s))) {
                    ((Enemy) s).die();
                    score += ((Enemy) s).getScore();
                    sprites.remove(this);
                    break;
                }
            }
            if (getY() < 0) sprites.remove(this);
        }

        public void draw(Graphics g, View view) {
            g.setColor(new Color(limit((float) Math.pow(1 * (speed / 20), 0.8)),
                    limit((float) Math.pow((speed / 20), 2.9d)),
                    limit((float) (0.15 * ((20 - speed) / 20)))));

            int d = (int) Math.round(Math.sin(speed*f) * speed);

            int tx = view.worldToRealX(getX());
            int ty = view.worldToRealY(getY());

            int dx = (int) (d * Math.sin(angle));
            int dy = (int) (d * Math.cos(angle));


            int s = Math.max(2, 5-(int)(20 / speed));

            g.fillRect(tx - s/2 - dx, ty + dy, s, s);
            g.fillRect(tx - s/2 + dx, ty - dy, s, s);

            g.setColor(new Color(limit((float) Math.pow(1 * (speed / 20), 0.3)),
                    limit((float) Math.pow((speed / 20), 1.2d)),
                    limit((float) (0.2 * ((20 - speed) / 20)))));

            g.fillRect(tx - s/2, ty, s, s);
        }
    }

    class SideBullet extends SJGSprite {
        double speed = 20;
        double angle;
        int count = 0;
        double side;

        public SideBullet(double angle, double side) {
            this.angle = -Math.PI / 2 + angle;
            this.side = side;
            setHeight(8);
            setWidth(8);
        }

        public void move() {
            count++;
            if (count > 4)
                translate(Math.cos(angle) * speed, Math.sin(angle) * speed);
            else
                translate(Math.cos(angle + 1.3 * side) * speed, Math.sin(angle + 1.3 * side) * speed);

            if (speed > 1) speed = speed * 0.96;
            for (Enumeration e = sprites.elements(); e.hasMoreElements();) {
                SJGSprite s = (SJGSprite) e.nextElement();
                if ((s instanceof Enemy) && (collidesWith(s))) {
                    ((Enemy) s).die();
                    score += ((Enemy) s).getScore();
                    sprites.remove(this);
                    break;
                }
            }
            if (getY() < 0) sprites.remove(this);
        }

        public void draw(Graphics g, View view) {
            g.setColor(new Color((float) (0.2 * ((20 - speed) / 20)), (float) Math.pow((double) (speed / 20), 2.2d), (float) (1 * (speed / 20))));
            int d = (int) Math.round(Math.cos(speed) * speed * 0.75);

            int tx = view.worldToRealX(getX());
            int ty = view.worldToRealY(getY());

            int dx = (int) (d * Math.sin(angle));
            int dy = (int) (d * Math.cos(angle));

            g.fillRect(tx - 1, ty, 3, 3);
        }
    }

    class Meteor extends Enemy {
        double speed = 0;
        double dx = 0;

        public int getScore() {
            return 0;
        }

        public void die() {
            sprites.add(new Explosion(4, 8, 1.2f, 0.0f, 0.1f), getX(), getY());
        }

        public Meteor(double dx) {
            this.dx = dx;
            setAnimation(cropper.getAnimation("red"));
            speed = 12;
        }

        public void move() {
            dx += dx / 70;
            translate(dx, speed);
            nextFrame();
            if (getY() > 400)
                sprites.remove(this);

            if (collidesWith(playerSprite)) {
                playerSprite.die();
                die();
            }
        }
    }

    // Screens

    Screen game = new Screen() {
        public void enter() {
            view.reset();
            sprites.removeAll();
            sprites.add(playerSprite = new PlayerSprite(), 0, 380);

            addingCircler = 0;
            scripts.clear();
            scripts.spawn(savepoint);

            scripts.setCallback(new Callback() {
                public void jumpto(String script) {
                    if (script.startsWith("level")) savepoint = script;
                }

                public void command(String name, String text) {
                    if (name.equals("level")) {
                        sprites.add(new LevelSprite(text), 0, 150);
                    } else if (name.equals("meteor")) {
                        try {
                            double x = Integer.parseInt(text);
                            sprites.add(new Meteor(x / WORLD_WIDTH), x, -10);
                        } catch (Exception e) {
                            throw new Error("" + e);
                        }
                    }
                }

                public void command(String name, int x, int y) {
                    if (name.equals("saucer"))
                        sprites.add(new Saucer(), x, y);

                    if (name.equals("silver"))
                        sprites.add(new Silver(), x, y);

                    if (name.equals("gold"))
                        sprites.add(new Gold(), x, y);

                    if (name.equals("circler_left"))
                        sprites.add(new Circler(-1), x, y);

                    if (name.equals("circler_right"))
                        sprites.add(new Circler(1), x, y);

                    if (name.equals("jumper"))
                        sprites.add(new Jumper(), x, y);

                    if (name.equals("flat_1"))
                        sprites.add(new Flat(1), x, y);

                    if (name.equals("flat_-1"))
                        sprites.add(new Flat(-1), x, y);

                    if (name.equals("flat_1.5"))
                        sprites.add(new Flat(1.5), x, y);

                    if (name.equals("flat_-1.5"))
                        sprites.add(new Flat(-1.5), x, y);

                    if (name.equals("shield"))
                        if (playerSprite.shield == false) sprites.add(new ShieldBonus(), x, y);

                    if (name.equals("weapon"))
                        if (playerSprite.sideBullet == false) sprites.add(new WeaponBonus(), x, y);

                }
            });
        }

        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            starField.draw(g);
            sprites.draw(g);
            drawScore(g);
        }

        public void move() {
            scripts.move();
            sprites.move();
            view.move();
            starField.move(view.getCameraX());
        }
    };

    Screen die = new Screen() {
        public void enter() {
            lives--;
            starField.setColor(0.35f, 0.40f, 0.85f, 20);
            starField.setSpeed(0.5, 30);
        }

        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            starField.draw(g);
            sprites.draw(g);
            drawScore(g);

        }

        private double sigmoid(double s) {
            return 1 / (1 + Math.exp(-s));
        }

        public void move() {
            starField.move(view.getCameraX() * sigmoid((20.1d - getFramesSinceEnter()) / 5d));
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
            starField.setSpeed(0.2, 80);

        }

        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            starField.draw(g);
            sprites.draw(g);
            g.drawImage(cropper.getFrame("gameover").getImage(), 190, 150, null);
            drawScore(g);

        }

        public void move() {
            view.move();
            starField.move(0);
            sprites.move();
            if (getFramesSinceEnter() > 60)
                if (hiscore.registerScore(score, savepoint.substring(5)))
                    showScreen(writeHiscore);
                else
                    showScreen(opening);
        }
    };

    Screen writeHiscore = new Screen() {
        public void enter() {
            // synthManager.setListenToKeyboard(false);
        }

        public void exit() {
            // synthManager.setListenToKeyboard(true);
        }

        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            starField.draw(g);
            hiscore.drawHiscoreEditor(g);
            g.drawImage(cropper.getFrame("newhiscore").getImage(), 82, 75, null);
            drawText(g, 142, 323, "", "WRITE YOUR NAME AND PRESS RETURN");

            /*
           g.setColor(new Color(90, 90, 90));
           g.drawString("Write your name and press return.", 100, 360);
           */
        }

        public void move() {
            starField.move(0);
            if (hiscore.moveHiscoreEditor() == false) showScreen(opening);
        }
    };

    Screen enterGame = new Screen() {

        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            starField.draw(g);
            sprites.draw(g);
            drawScore(g);

        }

        public void move() {
            starField.move(0);
            sprites.move();
            if (getFramesSinceEnter() == 12)
                sprites.add(new Explosion(100, -18, 0.2f, 1.0f, 1.0f), 0, 380);

            if (getFramesSinceEnter() > 20)
                showScreen(game);

        }

        public void enter() {
            sprites.removeAll();
            starField.setSpeed(0.8, 120);
            view.reset();

            // starField.setColor(1.0f, 0.2f, 0.1f, 100);

        }
    };

    Screen opening = new Screen() {
        int count;

        public void enter() {
            starField.setSpeed(-0.2, 200);
            getLocalPlayer().getMouseState().countLeft();
            // synthManager.startTrack("bassline-2");
        }

        public void draw() {
            Graphics g = getGraphicsModel().getFront();
            starField.draw(g);
            g.drawImage(cropper.getFrame("logo").getImage(), 120, 50, null);

            MouseState ms = getLocalPlayer().getMouseState();
            if (within(ms.getX(), ms.getY(), 144, 318, 271, 18))
                g.drawImage(cropper.getFrame("clickheretostart-down").getImage(), 146, 320, null);
            else
                g.drawImage(cropper.getFrame("clickheretostart-up").getImage(), 146, 320, null);

            drawScore(g);
            hiscore.drawHiscore(g);
        }

        public void move() {
            MouseState ms = getLocalPlayer().getMouseState();

            if ((within(ms.getX(), ms.getY(), 144, 318, 271, 18)) && (ms.countLeft() > 0))
                newGame();

            count++;
            starField.move(0);
        }
    };

    public void newGame() {
        view.reset();
        level = 0;
        levelFrameCount = 0;
        score = 0;
        lives = 3;
        savepoint = "level1";
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
        for (int j = 0; j < 5; j++) {
            int k = ((score / p) % 10);
            if (score < p)
                g.setColor(new Color(0, 51, 102));
            else
                g.setColor(new Color(0, 102, 153));

            p *= 10;
            g.drawImage(cropper.getFrame("L" + k).getImage(), 534 - j * 16 + 8 - cropper.getFrame("" + k).getWidth() / 2, 10, null);

        }

        for (int i = 0; i < 5; i++)
            if (i < lives)
                g.drawImage(cropper.getFrame("heart").getImage(), 10 + i * 16, 10, null);

    }

    public void init() {
        setGraphicsModel(new sjg.gm.Buffered(this));
        showScreen(opening);
        setAnimationSpeed(50);
        cropper = new Cropper(this);
        scripts = new ScriptEngine(this);
        starField = new StarField(800, 400, 100);
        // synthManager = new SynthManager(this);
        hiscore = new Hiscore(
                this, new HttpGetHiscoreServer(this, "/hiscore/list-hiscores.php", "/hiscore/add-hiscore.php")
        ) {
            public void drawHiscoreText(int position, String positionText, String entryText, int score, String round) {
                Graphics g = getGraphicsModel().getFront();
                int y = 142 + position * 16;
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

    public String getConfigurationFileName() {
        return "timbuktu.txt";
    }
}
