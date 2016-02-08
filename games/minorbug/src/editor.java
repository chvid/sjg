import java.applet.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;

public final class editor extends Applet implements Runnable {
    final int screenWidth = 560;
    final int screenHeight = 400;
    final int animationSpeed = 140;

    Thread m;

    Image offScreenImage;
    Graphics offScreen;

    ladybug l;

    int animationCount, paintAt;

    String saveToString() {
	String s="";
	for (int i = 0; i < l.mazeHeight; i++) {
	    for (int j = 0; j < l.mazeWidth; j++) {
		s+=""+l.mazeWall[i*l.mazeWidth+j]+l.mazeGate[i*l.mazeWidth+j]+
		    l.mazeBgnd[i*l.mazeWidth+j]+l.mazeDot[i*l.mazeWidth+j];

	    }

	    s+="\n";
	}
	return s;

    }

    void save() {
	System.out.println(saveToString());
    }

    public void init()
    {
	resize(screenWidth, screenHeight);
	offScreenImage = createImage(screenWidth, screenHeight);
	offScreen = offScreenImage.getGraphics();
	
    }
    
    public void paint(Graphics g) {
	
	paintAt = animationCount;
	g.drawImage(offScreenImage,0,0,this);
	
    }

    synchronized public void update(Graphics g) {
	draw();
	paintAt = animationCount;
	g.drawImage(offScreenImage,0,0,this);
	
    }
    
    void draw() {
	if (l == null) {
	    AppletContext ac = getAppletContext();
	    l=(ladybug)ac.getApplet("l");
	}
	if (l == null) return;

	offScreen.setColor(Color.white);
	offScreen.fillRect(0, 0, screenWidth, screenHeight);
	offScreen.setColor(Color.black);

	for (int i = 0; i < l.mazeHeight; i++)
	    for (int j = 0; j < l.mazeWidth; j++) {
		offScreen.drawString(""+l.mazeWall[i*l.mazeWidth+j],10+j*40,16+10+i*40);
		offScreen.drawString(""+l.mazeGate[i*l.mazeWidth+j],26+j*40,16+10+i*40);

		offScreen.drawString(""+l.mazeBgnd[i*l.mazeWidth+j],10+j*40,16+26+i*40);
		offScreen.drawString(""+l.mazeDot[i*l.mazeWidth+j],26+j*40,16+26+i*40);

	    }
	

    }
    
    public void run()
    {
	while (true)
	    {
		int paintAt1;
		try
		    {
			move();
			paintAt1 = animationCount;
			repaint();
			Thread.sleep(animationSpeed);
			while (paintAt1 > paintAt) {
			    showStatus("Frame skipped at "+animationCount);
			    move();
			    Thread.sleep(animationSpeed);
			}
		    }

		catch (InterruptedException e)
		    {
			stop();
		    }
	    }
    }

    public void start()
    {
	if (m == null)
	    {
		m = new Thread(this);
		m.start();
	    }
	
    }

    public void stop()
    {
	if (m != null)
	    {
		m.stop();
		m = null;
	    }
	
    }

    synchronized void move()
    {
	animationCount ++;

    }

    public boolean keyDown(java.awt.Event e,int key)
    {
	if (key == 32)
	    save();

	return false;
    }
    
    public boolean keyUp(java.awt.Event e,int key)
    {
	return false;
    }

    public boolean mouseDown(java.awt.Event e,int x,int y)
    {
	int cx, cy, cxm, cym, param, entry;

	cx = (x-10)/40;
	cy = (y-10)/40;
	cxm = (x-10)%40;
	cym = (y-10)%40;

	if (cxm < 16) param = 0; else
	    if (cxm < 32) param = 1; else
		param = -1;
	if (param != -1) {
	    if (cym < 16) param = param; else
		if (cym < 32) param += 2; else
		    param = -1;

	}

	entry = cx + cy * l.mazeWidth;

	switch (param) {
	case 0:
	    l.mazeWall[entry] = (l.mazeWall[entry]+1)%4;
	    break;
	case 1:
	    l.mazeGate[entry] = (l.mazeGate[entry]+1)%3;
	    break;
	case 2:
	    l.mazeBgnd[entry] = (l.mazeBgnd[entry]+1)%4;
	    break;
	case 3:
	    l.mazeDot[entry] = (l.mazeDot[entry]+1)%4;
	    break;
	    
	}

	if (param != -1) l.drawBack();

	return false;
    }

    public boolean mouseMove(java.awt.Event e,int x,int y)
    {
	return false;
    }

    public boolean mouseUp(java.awt.Event e,int x,int y)
    {
	return false;
    }

}

