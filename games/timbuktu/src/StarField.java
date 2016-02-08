import java.awt.*;
import java.util.*;

public class StarField {
    private int frame = 0;
    private int width;
    private int height;
    private int noStars;
    private int time = 0;

    private double speed = 0.2;
    private double dspeed = 0;

    private int centerx;
    private int centery;

    private double x[];
    private double y[];
    private double d[];
    private Color c[];

    private float color_r = 0.30f;
    private float color_g = 0.45f;
    private float color_b = 0.65f;

    private float dcolor_r;
    private float dcolor_g;
    private float dcolor_b;

    private int ctime = 0;

    private int screenWidth = 560;
    private double cameraX = 0;

    private float cut(double x) {
        if (x < 0) return 0.0f;
        if (x > 1) return 1.0f;
        return (float) x;
    }

    private void newStar(int i) {
        d[i] = 0.5 / (Math.random() + 0.01);
        x[i] = d[i] * (2 * Math.random() - 1) * width;
        if (speed > 0) y[i] = d[i] * ((Math.random() - .5) * height * 1.5) - centery;
        else y[i] = -(d[i] * ((Math.random() - .5) * height * 1.5) - centery);
        float e = 1 / ((float) d[i]);
        c[i] = new Color(cut(e * color_r), cut(e * color_g), cut(0.2 + e * color_b));
    }

    public StarField(int width, int height, int noStars) {
        this.width = width;
        this.height = height;
        this.noStars = noStars;

        x = new double[noStars];
        y = new double[noStars];
        d = new double[noStars];
        c = new Color[noStars];

        centery = height / 2;

        for (int i = 0; i < noStars; i++)
            newStar(i);

    }

    public void setSpeed(double speed, int time) {
        if (time == 0) {
            this.speed = speed;
            this.time = 0;
            dspeed = 0;

        } else {
            this.time = time;
            dspeed = (speed - this.speed) / time;

        }
    }

    public void setColor(float r, float g, float b, int time) {
        if (time == 0) {
            color_r = r;
            color_g = g;
            color_b = b;
            ctime = time;
            dcolor_r = 0;
            dcolor_g = 0;
            dcolor_b = 0;

        } else {
            ctime = time;
            dcolor_r = (r - color_r) / time;
            dcolor_g = (g - color_g) / time;
            dcolor_b = (b - color_b) / time;

        }
    }

    public void move(double cameraX) {
        this.cameraX = cameraX;
        frame++;
        if (time > 0) {
            time--;
            speed += dspeed;

        }
        if (ctime > 0) {
            ctime--;
            color_r += dcolor_r;
            color_g += dcolor_g;
            color_b += dcolor_b;

        }

        for (int i = 0; i < noStars; i++) {
            if ((Math.random() < 0.01 * speed + 0.002) || (y[i] > height) || (y[i] < -height))
                newStar(i);

            y[i] += speed * (5/*d[i]*/);
            //	    x[i]+=speed*((5/*d[i]*/*(x[i]-width/2))/1000);

        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);
        for (int i = 0; i < noStars; i++) {
            g.setColor(c[i]);

            // g.fillRect((int) Math.round((x[i] - cameraX) / d[i]) + screenWidth / 2, (int) Math.round((y[i]-speed*10) / d[i] + centery), 2, 2);

            // g.fillRect((int) Math.round((x[i] - cameraX) / d[i]) + screenWidth / 2, (int) Math.round((y[i]-speed*3) / d[i] + centery), 2, 2);

            g.fillRect((int) Math.round((x[i] - cameraX) / d[i]) + screenWidth / 2, (int) Math.round(y[i] / d[i] + centery), 2, 2);

        }
    }
}

