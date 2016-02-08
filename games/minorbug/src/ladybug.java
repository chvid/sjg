import java.applet.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;

public final class ladybug extends Applet implements Runnable {

    // TODO: change values
    final int animationSpeed = 70; // in msec pr. frame
    final int screenWidth = 560;
    final int screenHeight = 400;
    final int maxSprites = 10;
    final int initialNoLives = 3;

    // game state constants

    final int gsOpening = 0;
    final int gsEnter = 1;
    final int gsPlay = 2;
    final int gsNextLevel = 3;
    final int gsDie = 4;
    final int gsGameOver = 5;

    final int ctEnter = 20; // wait in the enter game state
    final int ctDie = 30; // a little longer death sequence
    final int ctNextLevel = 20;
    final int ctGameOver = 20;

    final String graphicsFN = "ladybug.gif";

    // hotspots - may not be overlapping - format: left, top, width, height

    // TODO: insert hotspots
    final int hotspot[] = { 185, 230, 190, 16 };

    // sprites

    int x[] = new int[maxSprites];
    int y[] = new int[maxSprites];
    int dx[] = new int[maxSprites];
    int dy[] = new int[maxSprites];
    int status[] = new int[maxSprites];
    int kind[] = new int[maxSprites];

    // w, h removed!

    // keyboard control variables

    boolean kUp=false, kDown=false, kLeft=false, kRight=false, kSpace=false, gamePaused = false;

    // game control variables

    int frameCount; // current frame number
    int paintAt; // last painted frame
    int gsca; // game status changed at
    int level; // current round or level
    int gameState; // state of the game
    int lives; // number of lives left
    int score; // current score
    int hiscore = 0; // current high score
    int rollOverHotspot=-1; // cursor over a hotspot
    int mouseDownHotspot=-1; // mouse down on a hotspot

    Thread m;

    Image frontImage; // the front layer
    Image backImage; // the back layer
    Image image[]; // cropped images

    Graphics front; // graphics of frontImage
    Graphics back; // graphics of backImage

    // constants specific to ladybug

    final int mazeWidth = 14;
    final int mazeHeight = 10;
    final int mazeBS = 40;
    final int skewX = 20;
    final int skewY = 20;
    final int microStep = 10;
    final Color bgColor = new Color(48,24,24);

    String maze[] = {
	/*
 wgbdwgbdwgbdwgbdwgbdwgbdwgbdwgbdwgbdwgbdwgbdwgbdwgbdwgbd
	 */
"00000000000000000000303310233011101110111011101110112000"+
"00000000000000000000201110111011201110112011101120112000"+
"00000000000000000000201130110011001101110011011100112000"+
"00000000000000000000201120111011101110110011201102112000"+
"00000000000000000000201100110111301100110111201110112000"+
"00000000000000000000201120110011001130110011101120112000"+
"00000000000000000000201110110011101100110011101100112000"+
"00000000000000000000201130111011101100110111101100112000"+
"00000000000000000000204000110011021120321022102200322000"+
"00000000000000000000100010001000100010001000100010000000",

"00000000000000000000301110111011101110111011101110112000"+
"00000000000000000000201101111011201110112011101120112000"+
"30331023103310231033201130110011001101110011011100112000"+
"20110011001100110011001120111011101110110011201102112000"+
"20113011001110110011011100110111301100110111201110112000"+
"20110011011110112011101100110011001130110011101120112000"+
"20111011001120111011101110110011101100110011101100112000"+
"20113011101100110211201130111011101100110111101100112000"+
"20400011021110110011001100110011021120321022102400322000"+
"10001000100010001000100010001000100010001000100010000000",

"00000000000000000000301110111011101110111011302310332000"+
"00000000000000000000201101111011001110112011001100112000"+
"30331023103310231033201120113000100010001000201110112000"+
"20110011001100110011001100112000000000000000201102112000"+
"30110011300010001000100010000000000000000000201110112000"+
"20110111200000000000000000000000000000000000201120112000"+
"20111011200000000000000000000000000000000000201100112000"+
"20112011101110111011101130111011101110111011001101112000"+
"20400011021130110011011100110011021120211034103400212000"+
"10001000100010001000100010001000100010001000100010000000",

"00000000000000000000301110111011101110111011302310232000"+
"00000000000000000000201101112011201110110011001100112000"+
"30331023103310231033201120112011101110111011201110112000"+
"20110011101110110011001100111011101130110011201102112000"+
"30110011011110111011103110210021103100110111001110112000"+
"20110111101120111011201110111011201110110011201120112000"+
"20111011201100111011101110110011001101111011201100112000"+
"30112011101110110011101130111011001110111011001101112000"+
"20100011021130110011011100110011021120311021102100312000"+
"10001000100010001000100010001000100010001000100010000000"};

    // variables specific to ladybug

    // datastructures for maze

    int mazeWall[] = new int [mazeWidth*mazeHeight];
    int mazeDot[] = new int [mazeWidth*mazeHeight];
    int mazeGate[] = new int [mazeWidth*mazeHeight];
    int mazeBgnd[] = new int [mazeWidth*mazeHeight];

    int noDots, noDotsEaten;

    int moveAt; // variable used for turtle animation

    void setGameState(int i) {
	gsca = frameCount;
	gameState = i;

    }

    void cropImages (String fn) {
	
	MediaTracker mt;
	int imageArea[] = { 
	    128,0,7,7, // prik 0
	    135,1,33,5, 129,7,5,33, // vaeg vandret og lodret 1 2
	    135,41,33,5, 169,7,5,33, // lem vandret og lodret 3 4
	    135,7,33,33, 175,7,33,33, 135,47,33,33, 175,47,33,33, // lem drejet 5 6 7 8
	    200,140,21,13, // memory 9
	    
	    0,64,32,32, 32,64,32,32, 64,64,32,32, 96,64,32,32, // mand 10 .. 25
	    0,96,32,32, 32,96,32,32, 64,96,32,32, 96,96,32,32,
	    0,128,32,32, 32,128,32,32, 64,128,32,32, 96,128,32,32,
	    0,160,32,32, 32,160,32,32, 64,160,32,32, 96,160,32,32,
	    
	    128,96,32,32, 160,96,32,32, 192,96,32,32, 224,96,32,32, // spoegelse 26 .. 29
	    
	    256,0,41,41, // baggrund 30
	    297,0,41,41, // baggrund 31

	    260,130,138,46, // score & hiscore 32

	    187,172,15,15, // ost 33
	    150,160,32,21, // netkort 34
	    
	    0,212,400,120, // opening screen 35

	    0,356,190,14, // start 36
	    0,340,190,14, // roed knap 37

	    260,70,140,28, // game over 38
	    224,42,176,28, // game paused 39

	    260,178,8,14, 270,178,8,14, 280,178,8,14, 290,178,8,14, 300,178,8,14,
	    310,178,8,14, 320,178,8,14, 330,178,8,14, 340,178,8,14, 350,178,8,14, // numbers 40 .. 49

	    280,112,7,5, 288,113,4,3, 294,113,3,2, 300,114,1,1, // puf 50 .. 53

	    243,148,15,15 // bonus liv 54
	    

	};

	Image collection;
	mt = new MediaTracker (ladybug.this);
	showStatus("loading images ...");

	collection = getImage(getCodeBase(),fn);

	// crop images
	
	image = new Image [imageArea.length / 4];
	for (int i=0; i < imageArea.length / 4; i++)
	    image[i]=createImage(new FilteredImageSource(collection.getSource(),
							 new CropImageFilter(imageArea[i*4], imageArea[i*4+1], 
									     imageArea[i*4+2], imageArea[i*4+3])));
	
	for (int i =0; i < imageArea.length / 4; i++)
	    mt.addImage(image[i],1);
	
	try { mt.waitForID(1); }
	
	catch(InterruptedException e) {};
	
	showStatus("done loading images ...");
	
    }


    public String getAppletInfo() {
	// TODO: return name & credits
	return "Ladybug - Christian Hvid 2000";

    }

    void initLevel() {
	for (int i=0; i < maxSprites; i++) kind[i]=-1;
	setGameState(gsEnter);

	// TODO: initialise level

	for (int i = 0; i < mazeHeight; i++) {
	    for (int j = 0; j < mazeWidth; j++) {
		mazeWall[i*mazeWidth+j]=maze[(level-1) % maze.length].charAt(j*4+i*mazeWidth*4)-48;
		mazeGate[i*mazeWidth+j]=maze[(level-1) % maze.length].charAt(j*4+i*mazeWidth*4+1)-48;
		mazeBgnd[i*mazeWidth+j]=maze[(level-1) % maze.length].charAt(j*4+i*mazeWidth*4+2)-48;
		if (mazeBgnd[i*mazeWidth+j] == 3) mazeBgnd[i*mazeWidth+j] = 2;
		if (mazeBgnd[i*mazeWidth+j] == 4) mazeBgnd[i*mazeWidth+j] = 1;
		mazeDot[i*mazeWidth+j]=maze[(level-1) % maze.length].charAt(j*4+i*mazeWidth*4+3)-48;

	    }
	}

	noDots=0;
	noDotsEaten=0;
	for (int i = 0; i < mazeWidth*mazeHeight; i++)
	    if (mazeDot[i] == 1) noDots++;

	drawBack();
	startLevel();

    }

    void nextLevel () {
	level ++;
	initLevel();

    }

    void startLevel () {
	for (int i=0; i < maxSprites; i++) kind[i]=-1;

	x[0] = 0; y[0] = mazeBS*8; status[0] = 0; kind[0]=0; dx[0]=0; dy[0]=0;
	int k = 0;
	for (int i = 0; i < mazeHeight; i++)
	    for (int j = 0; j < mazeWidth; j++) {
		if (maze[(level-1) % maze.length].charAt(j*4+i*mazeWidth*4+2) == '3') {
		    k++;
		    x[k] = mazeBS*j; y[k] = mazeBS*i; status[k] = 50*k; kind[k]=1; dx[k]=0; dy[k]=0;
		}

		if (maze[(level-1) % maze.length].charAt(j*4+i*mazeWidth*4+2) == '4') {
		    x[0] = mazeBS*j; y[0] = mazeBS*i; status[0] = 0; kind[0]=0; dx[0]=0; dy[0]=0;
		}
	    }

	setGameState(gsEnter);

    }

    void die () {
	lives--;
	setGameState(gsDie);

    }

    void newGame () {

	score = 0;
	level = 0;
	lives = initialNoLives;
	nextLevel ();

    }
     
    public void init() {
	resize(screenWidth, screenHeight);
	frontImage = createImage(screenWidth, screenHeight);
	backImage = createImage(screenWidth, screenHeight);
	front = frontImage.getGraphics();
	back = backImage.getGraphics();

	// TODO: insert name of graphics file
	cropImages(graphicsFN);

	setGameState(gsOpening);

    }


    public void paint(Graphics g) {
	g.drawImage(frontImage,0,0,this);
	paintAt = frameCount;

    }

    synchronized public void update(Graphics g) {
	draw();
	g.drawImage(frontImage,0,0,this);
	paintAt = frameCount;

    }

   public void run() {
	while (true)
	    {
		int paintAt1;
		try
		    {
			move();
			paintAt1 = frameCount;
			repaint();
			Thread.sleep(animationSpeed);
			while (paintAt1 > paintAt) {
			    showStatus("Frame skipped at "+frameCount);
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

    public void start() {
	if (m == null) {
	    m = new Thread(this);
	    m.start();
	}
	
    }

    public void stop() {
	if (m != null) {
	    m.stop();
	    m = null;
	}
	
    }

    boolean canMove(int x1, int y1, int dx, int dy) {
	return canMove1(x1,y1,dx,dy,false,false);
    }

    boolean canMove1(int x1, int y1, int dx, int dy, boolean canMoveGates, boolean moveGates) {
	int x = x1 / mazeBS;
	int y = y1 / mazeBS;
	int t = 0;

	if (y1 % mazeBS != 0) return (dx == 0);
	if (x1 % mazeBS != 0) return (dy == 0);

	if (dx == 0) {
	    if (dy > 0) {
		if ((mazeWall[(y+1)*mazeWidth+x]&1) == 1) t=1;
		
	    } else {
		if ((mazeWall[y*mazeWidth+x]&1) == 1) t=1;		
	    }
	} else {
	    if (dx > 0) {
		if ((mazeWall[y*mazeWidth+x+1]&2) == 2) t=1;	       
	    
	    } else {
		if ((mazeWall[y*mazeWidth+x]&2) == 2) t=1;

	    }
	}
	
	int gate = -1;
	int gate1;

	if (dx == 0) {
	    if (dy > 0) {
		gate1 = (y+1)*mazeWidth+x;
		if (mazeGate[gate1] == 2) gate=gate1; else {
		    gate1 = (y+1)*mazeWidth+x+1;
		    if (mazeGate[gate1] == 2) gate=gate1; 
		}
		
	    } else {
		gate1 = y*mazeWidth+x;
		if (mazeGate[gate1] == 2) gate=gate1; else {
		    gate1 = y*mazeWidth+x+1;
		    if (mazeGate[gate1] == 2) gate=gate1; 
		}
	    }
	    
	} else {
	    if (dx > 0) {
		gate1 = y*mazeWidth+x+1;
		if (mazeGate[gate1] == 1) gate=gate1; else {
		    gate1 = (y+1)*mazeWidth+x+1;
		    if (mazeGate[gate1] == 1) gate=gate1; 
		}
		    
	    } else {
		gate1 = y*mazeWidth+x;
		if (mazeGate[gate1] == 1) gate=gate1; else {
		    gate1 = (y+1)*mazeWidth+x;
		    if (mazeGate[gate1] == 1) gate=gate1; 
		}
	    }
	}

	if ((gate != -1) && (moveGates == true)) {
	    boolean b = !(((gate1-y*mazeWidth-x) == mazeWidth) || ((gate1-y*mazeWidth-x) == 1));
	    if (mazeGate[gate] == 1) { if ((dy >0)==b) mazeGate[gate] = 5; else mazeGate[gate]=6; } else 
		{ if ((dx > 0)==b) mazeGate[gate] = 3; else mazeGate[gate]=4; }

	}
	
	if (canMoveGates == true) return (t==0); else return (t==0) && (gate == -1);

    }   

    void moveMaze() {
	 for (int i=0; i < mazeWidth; i++)
	     for(int j=0; j < mazeHeight; j++) {
		 int k = mazeGate[i+j*mazeWidth];

		 switch (k) {
		 case 3: case 4: k=1; break;
		 case 5: case 6: k=2; break;
		 }

		 mazeGate[i+j*mazeWidth]=k;

	     }
	 
    }

    void moveSprites() {
	for (int i=0; i < maxSprites; i++) {

	    // TODO: move sprite
	    switch (kind[i]) {
	    case 0:
		// move player

		// collision detect /w dots

		if (gameState == gsPlay) {
		    if (((x[i]+16) % mazeBS <= 32) && ((y[i]+16) % mazeBS <= 32)) {
			int p = ((x[i]+16) / mazeBS) + mazeWidth * ((y[i]+16) / mazeBS);
			switch (mazeDot[p]) {
			case 1:
			    mazeDot[p] = 0;
			    noDotsEaten++;
			    score += 128;
			    break;
			case 2:
			    mazeDot[p] = 0;
			    score += 9233;
			    break;
			case 3:
			    mazeDot[p] = 0;
			    score += 16384;
			    break;
			case 4:
			    mazeDot[p] = 0;
			    lives ++;
			    break;
			}

			if (noDotsEaten == noDots)
			    setGameState(gsNextLevel);
		
		    }

		    if ((kRight == true) || (kLeft == true) || (kUp == true) || (kDown == true))
			moveAt = frameCount;
		    
		    switch (frameCount - moveAt) {
		    case 0:
		    case 1:
		    case 3:
		    case 6:
			status[i]=((status[i]+1) & 3)+(status[i] & (255-3));
			break;
			    
		    }
		

		    if (kRight == true) {
			status[i]=status[i] % 4 + 4; 
			if (canMove1(x[i],y[i],microStep,0,true,true) == true) { x[i]+=microStep; } else {
			    if (canMove1(x[i],y[i]-microStep,microStep,0,true,true) == true) { x[i]+=microStep; y[i]-=microStep; } else
				if (canMove1(x[i],y[i]+microStep,microStep,0,true,true) == true) { x[i]+=microStep; y[i]+=microStep; }
			}
		    } else 
			if (kLeft == true) {
			    status[i]=status[i] % 4 + 12; 
			    if (canMove1(x[i],y[i],-microStep,0,true,true) == true) { x[i]-=microStep; } else
				if (canMove1(x[i],y[i]-microStep,-microStep,0,true,true) == true) { x[i]-=microStep; y[i]-=microStep; } else
				    if (canMove1(x[i],y[i]+microStep,-microStep,0,true,true) == true) { x[i]-=microStep; y[i]+=microStep; }

			} else
			    if (kDown == true) {
				status[i]=status[i] % 4 + 8;
				if (canMove1(x[i],y[i],0,microStep,true,true) == true) { y[i]+=microStep; } else
				    if (canMove1(x[i]-microStep,y[i],0,microStep,true,true) == true) { y[i]+=microStep; x[i]-=microStep; } else
					if (canMove1(x[i]+microStep,y[i],0,microStep,true,true) == true) { y[i]+=microStep; x[i]+=microStep; }
			    } else
				if (kUp == true) {
				    status[i]=status[i] % 4;
				    if (canMove1(x[i],y[i],0,-microStep,true,true) == true) { y[i]-=microStep; } else
					if (canMove1(x[i]-microStep,y[i],0,-microStep,true,true) == true) { y[i]-=microStep; x[i]-=microStep; } else
					    if (canMove1(x[i]+microStep,y[i],0,-microStep,true,true) == true) { y[i]-=microStep; x[i]+=microStep; }
				}
		}

		break;

	    case 1:
		// move system analyst

		x[i]+=dx[i]; y[i]+=dy[i];
		if (status[i]>0) { 
		    if (x[i] % mazeBS == 0) {
			if (Math.random() > .5) dx[i]=microStep; else dx[i]=-microStep;
			if (canMove(x[i],y[i],dx[i],dy[i]) == false) dx[i]=0;

		    }
		    status[i]--;
		} else {
		    if (x[i] % mazeBS == 0) {
			
			if ((canMove(x[i],y[i],dx[i],dy[i]) == false) || (canMove(x[i],y[i],dy[i],dx[i]) == true) ||
			    (canMove(x[i],y[i],-dy[i],-dx[i]) == true)) {
			    
			    int j = (int)Math.round(Math.random()*4);
			    if (Math.random() < .5) {
				if (Math.random() <.5) {
				    if (x[i] > x[0]) j=3; else j=1;

				} else {
				    if (y[i] > y[0]) j=0; else j =2;

				}
			    }
			
			    if ((Math.random() < .7) || (canMove(x[i],y[i],dy[i],dx[i]) == false)) { dx[i] = 0; dy[i]=0; }

			    while ((dx[i]+dy[i] == 0) || (canMove(x[i],y[i],dx[i],dy[i]) == false)) {
				switch (j) {
				case 0: dx[i] = 0; dy[i] = -microStep; break;
				case 1: dx[i] = microStep; dy[i] = 0; break;
				case 2: dx[i] = 0; dy[i] = microStep; break;
				case 3: dx[i] = -microStep; dy[i] = 0; break;
				    
				}
				j = (j+1)%4;
			    }
			}
		    }

		}

		if (gameState == gsPlay) {
		    // collision detect with player

		    if ((x[i]<x[0]+30) && (y[i]<y[0]+30) && (x[i]+30 > x[0]) && (y[i]+30 > y[0])) {
			die();

		    }

		}

		break;
		
	    }
	}
    }
    
    synchronized void move() {
	if (gamePaused == false) {
	    frameCount ++;
	    switch (gameState) {
	    case gsOpening:
		break;

	    case gsEnter:
		if (frameCount-gsca > ctEnter)
		    setGameState(gsPlay);
		
		break;

	    case gsDie:
		if (frameCount-gsca > ctDie) {
		    if (lives == 0) setGameState(gsGameOver); 
		    else startLevel(); // call startLevel rather than initLevel to avoid restarting the level completely
		    
		}
		moveMaze();
		moveSprites();
		break;
		
	    case gsGameOver:
		if (frameCount-gsca > ctGameOver)
		    setGameState(gsOpening);
		
		moveMaze();
		moveSprites();
		break;
		
	    case gsPlay:
		moveMaze();
		moveSprites();
		break;

	    case gsNextLevel:
		if (frameCount-gsca > ctNextLevel) {
		    nextLevel();
		}
		
	    }
	}

	if (score > hiscore) hiscore = score;

    }

    synchronized void drawSprites () {
	for (int i=0; i < maxSprites; i++) {

	    // TODO: draw sprite
	switch1:
	    switch (kind[i]) {
	    case 0:
		if (gameState == gsGameOver) {
		    break switch1; 
		}
		if (gameState == gsDie) {
		    if (frameCount-gsca < 10)
			front.drawImage(image[10 + 4*(frameCount % 4) ],x[i]+skewX+4,y[i]+skewY+4,this); else
			    {
				switch (frameCount-gsca) {
				    case 11: case 12: case 13: case 15: case 16: case 18: case 20: case 23: case 26:
				    front.drawImage(image[10 + 4*(frameCount % 4) ],x[i]+skewX+4,y[i]+skewY+4,this);
				    break;
				}

				int k = (frameCount-gsca-20)/4;

				if (k < 0) k = -k;

				for (int j = 0; j < 5; j++)
				    front.drawImage(image[50+j%2+k],x[i]+(int)Math.round(18+(3*k+20)*Math.sin(1.4*(j+frameCount)))+skewX,
						    y[i]+(int)Math.round(18+(3*k+20)*Math.sin(0.6*(j+frameCount)))+skewY,this);

			    }

		    break switch1;
		}

		front.drawImage(image[10 + status[0] ],x[i]+skewX+4,y[i]+skewY+4,this);
		
		break;
		
	    case 1:
		front.drawImage(image[26 + frameCount % 4],x[i]+skewX+4,y[i]+skewY+4,this);
		
		break;
		
	    }
	}
    }

    void drawBack () {
        back.setColor(bgColor);
	back.fillRect(0, 0, screenWidth, screenHeight);

	// TODO: draw background graphics

	back.drawImage(image[32],40,24,this);

	/*        back.setColor(new Color(255,255,255) );
		  back.fillRect(skewX-1, skewY-1, (mazeWidth-1)*mazeBS+4, (mazeHeight-1)*mazeBS+4);*/

	for (int i=0; i < mazeWidth; i++)
	    for(int j=0; j < mazeHeight; j++) 
		switch (mazeBgnd[i+j*mazeWidth]) {
		case 1:
		    back.drawImage(image[30],mazeBS*i+skewX+1,mazeBS*j+skewY+1,this);
		    break;
		case 2:
		    back.drawImage(image[31],mazeBS*i+skewX+1,mazeBS*j+skewY+1,this);
		    break;
		}

	for (int i=0; i < mazeWidth; i++)
	    for(int j=0; j < mazeHeight; j++) {
		int k = mazeWall[i+j*mazeWidth];
		if ((mazeBgnd[i+j*mazeWidth] >0) ||
		    ((j > 0) && (mazeBgnd[i+(j-1)*mazeWidth] > 0)) ||
		    ((i > 0) && (mazeBgnd[i-1+j*mazeWidth] > 0)) ||
		    ((i > 0) && (j > 0) && (mazeBgnd[i-1+(j-1)*mazeWidth] > 0))

		    ) back.drawImage(image[0],mazeBS*i+skewX-2,mazeBS*j+skewY-2,this);
		

		if ((k & 1) == 1) back.drawImage(image[1],mazeBS*i+skewX+5,mazeBS*j+skewY-1,this);
		if ((k & 2) == 2) back.drawImage(image[2],mazeBS*i+skewX-1,mazeBS*j+skewY+5,this);

	    }


    }

    synchronized void drawMaze () {
	for (int i=0; i < mazeWidth; i++)
	    for(int j=0; j < mazeHeight; j++) {
		if ((i < mazeWidth-1) && (j < mazeHeight-1)) {
		    front.setColor(Color.red);

		}

		switch (mazeGate[i+j*mazeWidth]) {
		    case 1:
			front.drawImage(image[4],mazeBS*i+skewX-1,mazeBS*j+skewY+5,this); 
			front.drawImage(image[4],mazeBS*i+skewX-1,mazeBS*(j-1)+skewY+5,this); 
			break;
		    case 2: 
			front.drawImage(image[3],mazeBS*i+skewX+5,mazeBS*j+skewY-1,this); 
			front.drawImage(image[3],mazeBS*(i-1)+skewX+5,mazeBS*j+skewY-1,this); 
			break;
		    case 3: case 5: 
			front.drawImage(image[5],mazeBS*(i-1)+skewX+6,mazeBS*(j-1)+skewY+6,this); 
			front.drawImage(image[8],mazeBS*i+skewX+4,mazeBS*j+skewY+4,this); 
			break;
		    case 4: case 6: 
			front.drawImage(image[6],mazeBS*i+skewX+4,mazeBS*(j-1)+skewY+6,this); 
			front.drawImage(image[7],mazeBS*(i-1)+skewX+6,mazeBS*j+skewY+4,this); 
			break;

		}

		switch (mazeDot[i+j*mazeWidth]) {
		    case 1: front.drawImage(image[9],mazeBS*i+skewX+6+4,mazeBS*j+skewY+9+4,this); break;
		    case 2: front.drawImage(image[33],mazeBS*i+skewX+8+4,mazeBS*j+skewY+8+4,this); break;
		    case 3: front.drawImage(image[34],mazeBS*i+skewX+6,mazeBS*j+skewY+8+4,this); break;
		    case 4: front.drawImage(image[54],mazeBS*i+skewX+12,mazeBS*j+skewY+12,this); break;

		}




	    }

    }

    void drawOpening() {
	front.setColor(bgColor); 
	front.fillRect(0, 0, screenWidth, screenHeight);

	// TODO: draw background graphics

	front.drawImage(image[35], 80, 90, this);

	if (rollOverHotspot == 0)
	    front.drawImage(image[37], 185, 230, this); else
	    front.drawImage(image[36], 185, 230, this);

    }

    void drawNumber (Graphics g, int x, int y, int n, int digits) {
	int e = 1;
	for (int i = 0; i < digits; i++) {
	    front.drawImage(image[40+(n / e) % 10], x + (digits-i-1)*10, y, this);
	    e = e * 10;

	}
    }
    
    synchronized void draw() {
	switch (gameState) {
	case gsOpening:
	    drawOpening();
	    break;
	case gsEnter:
	case gsPlay:
	case gsDie:
	case gsNextLevel:
	case gsGameOver:
	    front.drawImage(backImage,0,0,this);

	    drawNumber(front, 120, 24, score, 8);
	    drawNumber(front, 120, 40, hiscore, 8);
	    drawNumber(front, 180, 56, lives, 2);

	    drawMaze();
	    drawSprites();

	    if (gameState == gsGameOver) 
		front.drawImage(image[38],210,164,this);

	    break;
	}
	if (gamePaused == true)
	    front.drawImage(image[39],192,200,this);

    }


    public boolean keyDown(java.awt.Event e,int key) {
	switch (key) {
	case 1006: kLeft = true; break;
	case 1007: kRight = true; break;
	case 1004: kUp = true; break;
	case 1005: kDown = true; break;
	case 32: kSpace = true; break;

	case 1015:
	    if (gameState != gsOpening) gamePaused = !gamePaused;
	    break;

	}
	return false;
    }
    
    public boolean keyUp(java.awt.Event e,int key) {
	switch (key) {
	case 1006: kLeft = false; break;
	case 1007: kRight = false; break;
	case 1004: kUp = false; break;
	case 1005: kDown = false; break;
	case 32: kSpace = false; break;

	}
	return false;
    }

    public boolean mouseDown(java.awt.Event e,int x,int y) {
	mouseDownHotspot = -1;
	for (int i=0; i < hotspot.length / 4; i++)
	    if (within(x,y,hotspot[i*4],hotspot[i*4+1],hotspot[i*4+2],hotspot[i*4+3]) == true)
		mouseDownHotspot = i;

	return false;
    }

    public boolean mouseMove(java.awt.Event e,int x,int y) {
	rollOverHotspot = -1;
	for (int i=0; i < hotspot.length / 4; i++)
	    if (within(x,y,hotspot[i*4],hotspot[i*4+1],hotspot[i*4+2],hotspot[i*4+3]) == true)
		rollOverHotspot = i;

	return false;
    }

    public boolean mouseUp(java.awt.Event e,int x,int y) {
	int mouseUpHotspot = -1;
	for (int i=0; i < hotspot.length / 4; i++)
	    if (within(x,y,hotspot[i*4],hotspot[i*4+1],hotspot[i*4+2],hotspot[i*4+3]) == true)
		mouseUpHotspot = i;

	if (mouseDownHotspot == mouseUpHotspot)
	    hotspotClick (mouseDownHotspot);

	return false;
    }

    boolean within(int x1, int y1, int x, int y, int w, int h) {
	return ((x <= x1) && (y <= y1) && (x+w > x1) && (y+h > y1));
    }

    void hotspotClick (int spot) {
	// TODO: handle hotspot clicks

	if ((gameState == gsOpening) && (spot == 0))
	    newGame();

    }

}

