//******************************************************************************
// noHats.java:	Applet
//
//******************************************************************************
import java.applet.*;
import java.awt.*;
import java.awt.image.*;

public final class noHats extends Applet implements Runnable
{
	Thread	 m_noHats = null;
	Image collection, small[], offScreenImage, backgroundImage;
	int clickHereWidth,clickHereHeight,score,level,hiscore,
		animationCount, x[],y[],dx[],dy[],status[],kind[], gameStatus,
		bubbleTextX, bubbleTextY, bubbleTextTime,
		monsterMachineX, monsterMachineY, 		  
		policeBoxX, policeBoxY, lives;

	double pNewMonster, pHat, pGhost;
	String bubbleText;
	int playGround[][];
	boolean drawClickHereLine, canGotoNextLevel;
	Graphics offScreen, background;

	public noHats()
	{
	}

	public String getAppletInfo()
	{
		return "Name: noHats\r\n" +
		       "Author: Christian von der Hvid\r\n" +
		       "Created with Microsoft Visual J++ Version 1.0";

	}

	public void init()
	{
		MediaTracker mt;
		mt = new MediaTracker (this);
		showStatus("noHats by Christian Hvid 1998 - loading images ...");
		resize(512, 240);
		collection = getImage(getCodeBase(),"noHats.gif");
		small = new Image [63];
		x = new int [10];
		y = new int [10];
		dx = new int [10];
		dy = new int [10];
		status = new int [10];
		kind = new int [10];
		playGround = new int [16][30];

		// crop images
		
		// player
		
		for (int i=0; i < 8; i++)
			small[i]=createImage(new FilteredImageSource(
				collection.getSource(),new CropImageFilter(i*32,0,20,32)));

		// death sequence
		
		for (int i=0; i < 8; i++)
			small[i+11]=createImage(new FilteredImageSource(
				collection.getSource(),new CropImageFilter(i*32,64,20,32)));

		// death sequence
		
		for (int i=0; i < 8; i++)
			small[i+19]=createImage(new FilteredImageSource(
				collection.getSource(),new CropImageFilter(i*32,96,20,32)));

		// hat
		
		for (int i=0; i < 7; i++)
			small[i+27]=createImage(new FilteredImageSource(
				collection.getSource(),new CropImageFilter(32+i*32,32,20,12)));

		// ice cream
		
		for (int i=0; i < 3; i++)
			small[i+35]=createImage(new FilteredImageSource(
				collection.getSource(),new CropImageFilter(256+i*20,0,18,24)));

		// dollar bag
		
		for (int i=0; i < 2; i++)
			small[i+38]=createImage(new FilteredImageSource(
				collection.getSource(),new CropImageFilter(320+i*32,0,22,20)));

		// floor
		
		small[8]=createImage(new FilteredImageSource(
			collection.getSource(),new CropImageFilter(0,32,32,8)));

		
		small[49]=createImage(new FilteredImageSource(
			collection.getSource(),new CropImageFilter(0,40,32,8)));
		
		// police box
		
		small[40]=createImage(new FilteredImageSource(
			collection.getSource(),new CropImageFilter(255,24,40,48)));

		// large plant
		
		small[41]=createImage(new FilteredImageSource(
			collection.getSource(),new CropImageFilter(300,42,26,30)));

		// large plant /w blue flowers
		
		small[42]=createImage(new FilteredImageSource(
			collection.getSource(),new CropImageFilter(333,36,14,36)));

		// small plant /w red flowers
		
		small[43]=createImage(new FilteredImageSource(
			collection.getSource(),new CropImageFilter(300,22,12,12)));

		// grass 1
		
		small[44]=createImage(new FilteredImageSource(
			collection.getSource(),new CropImageFilter(316,28,8,6)));

		// grass 2
		
		small[45]=createImage(new FilteredImageSource(
			collection.getSource(),new CropImageFilter(328,28,10,6)));

		// grass 3
		
		small[46]=createImage(new FilteredImageSource(
			collection.getSource(),new CropImageFilter(343,26,10,8)));

		// barrel
		
		for (int i=0; i < 2; i++)
			small[9+i]=createImage(new FilteredImageSource(
				collection.getSource(),new CropImageFilter(13*i,51,13,13)));

		for (int i=0; i < 4; i++)
			small[59+i]=createImage(new FilteredImageSource(
				collection.getSource(),new CropImageFilter(13*i,51,13,13)));

		// monster machine
		
		small[47]=createImage(new FilteredImageSource(
			collection.getSource(),new CropImageFilter(354,24,40,44)));
													  
		// small player
		
		small[48]=createImage(new FilteredImageSource(
			collection.getSource(),new CropImageFilter(344,74,10,20)));
		
		// opening logo
		
		small[34]=createImage(new FilteredImageSource(
			collection.getSource(),new CropImageFilter(0,128,430,140)));
		
		// background
		
		small[58]=createImage(new FilteredImageSource(
			collection.getSource(),new CropImageFilter(0,270,512,240)));
		
		// fish
		
		for (int i=0; i < 4; i++)
			small[i+50]=createImage(new FilteredImageSource(
				collection.getSource(),new CropImageFilter(256+i*20,74,14,10)));

		// flash
		
		for (int i=0; i < 4; i++)
			small[i+54]=createImage(new FilteredImageSource(
				collection.getSource(),new CropImageFilter(16*i+256,86,16,14)));


		animationCount = 0;

		offScreenImage = createImage(512,240);
		offScreen = offScreenImage.getGraphics();
		
		backgroundImage = createImage(512,240);
		background = backgroundImage.getGraphics();

		for (int i =0; i < 63; i++)
			mt.addImage(small[i],1);

		try {
			mt.waitForID(1); }
		catch(InterruptedException e) {};
		
		clickHereWidth=offScreen.getFontMetrics().stringWidth("... click here to start ...")/2;
		clickHereHeight=offScreen.getFontMetrics().getHeight();

		score = 0;
		hiscore = 0;

		doStart();
		
		showStatus("game started ...");

	}

	public void playerDie()
	{
		gameStatus = 2;
		if ((status[0]==-1) || (status[0] ==1)) status[0] = 1; else status[0] = -1;

		dx[0] = 0;
		dy[0] = 0;

		newComment(3);

		
	}

	public void newComment(int style)
	{
		String start[] = {"let's play cool", "let's play hard",
			"let's play funk", "c'mon honey",
			"let's rock'n'roll","no time for blues",
			"upbeat reggae baby"};
		String jump[] = {"phew!","funkey!","yeehaa!","rock'n'roll",
			"well ...","okeh ..."};
		String die[] = {"ouch!","so - I die","let's die","sorry about this",
			"you're so stupid","that's it!","I quit!","you cool it boy"};
		String point[] = {"great","very good","initiv","storartet",
			"flubby yet good","funky but better","aloha","gufguf",
			"haalou!","gnufgnuf","fine","good","strange yet better",
			"surreal","cool it","this is funk","so good","bzzbzz",
			"hm hm ..."};
		
		if ((bubbleTextTime > 0) && ((style == 2) || (style == 4)))
		{}	else {


		switch (style) {
		case 1:
			bubbleText = start[(int)Math.round(Math.random()*(7-1))];
			break;
		
		case 2:
			bubbleText = jump[(int)Math.round(Math.random()*(6-1))];
			break;
		
		case 3:
			bubbleText = die[(int)Math.round(Math.random()*(7-1))];
			break;
		
		case 4:
			bubbleText = point[(int)Math.round(Math.random()*(19-1))];
			break;
		
		}

		bubbleTextTime = 25;
		if (x[0] > 256) bubbleTextX = x[0] - 10 - 
			offScreen.getFontMetrics().stringWidth(bubbleText); else
			bubbleTextX = x[0]+24;

		bubbleTextY = y[0]+16;
		}

	}

	public void nextLevel()

	{

	int l1[][] = {
		{ 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
		  0x0000, 0x3DC0, 0x0000, 0x0007, 0x0008, 
		  0x0010, 0x4000, 0x2000, 0x0020, 0x00C0, 
		  0x0000, 0x0000, 0x0B00, 0x101F, 0x0000, 
		  0x0000, 0x0000, 0x0000, 0x0000, 0xFFFF, 
		  0x0000, 0x0000, 0x0000, 0x0000, 0x0000 },
		{ 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
		  0x0000, 0x1F00, 0x2000, 0x4001, 0x0002, 
		  0x0024, 0x0008, 0x0000, 0x0040, 0x8000, 
		  0x4290, 0x2020, 0x1000, 0x0807, 0x0000, 
		  0x0000, 0x0000, 0x0000, 0x0000, 0xFFFF, 
		  0x0000, 0x0000, 0x0000, 0x0000, 0x0000 },
		{ 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
		  0x0000, 0x3800, 0x0000, 0x03C0, 0x0000, 
		  0x0000, 0x0004, 0x8208, 0x4400, 0x2000, 
		  0x0800, 0x0000, 0x0001, 0x101F, 0x0000, 
		  0x0000, 0x0000, 0x0000, 0x0001, 0xFFFF, 
		  0x0000, 0x0000, 0x0000, 0x0000, 0x0000 },
		{ 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
		  0x0000, 0x3C00, 0x0000, 0x0010, 0x0000, 
		  0x8000, 0x0000, 0x2200, 0x4420, 0x0000, 
		  0x0880, 0x0000, 0x0000, 0x0007, 0x0100, 
		  0x0000, 0x0000, 0x0000, 0x0000, 0xFFFF, 
		  0x0000, 0x0000, 0x0000, 0x0000, 0x0000 },
		{ 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
		  0x0000, 0x1C00, 0x0000, 0x0000, 0x8000, 
		  0x0000, 0x0000, 0x0000, 0x0000, 0x0900, 
		  0x4000, 0x0000, 0x0000, 0x0443, 0x0000, 
		  0x0000, 0x0010, 0x0000, 0x0000, 0xFFFF, 
		  0x0000, 0x0000, 0x0000, 0x0000, 0x0000 },
		{ 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
		  0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
		  0xFF88, 0x0004, 0x00E2, 0x0000, 0x0030, 
		  0x0000, 0x0000, 0x0000, 0x0000, 0x0001, 
		  0x0002, 0x0004, 0x0000, 0x0001, 0xFFFF, 
		  0x0000, 0x0000, 0x0000, 0x0000, 0x0000 }};

	int l2[][] = {
		{ // pNewMonster, pHat, pGhost			
		  18,300,50,
		  // start position
		  36,152,
		  // waffle
		  20,80,
		  // policebox
		  40,8,136, 
		  // machine
		  47,328,44,
		  42,15,196, 44,4,226, 43,19,220, 43,1,220, 44,16,226, 41,-10,202,
		  44,500,226, 45,502,226, 46,492,224, 41,470,202}, 
		{ 20,200,400,
		  36,152,
		  70,30,
		  40,8,136, 
		  47,328,44,
		  42,15,196, 44,4,226, 
		  44,4,178, 43,19,172, 43,1,172, 
		  44,367,178, 43,352,172,
		  44,16,226, 41,-10,202,
		  44,507,226, 45,498,226, 46,462,224, 41,480,202 },

		{ // pNewMonster, pHat, pGhost
		  26,200,50,
		  // start position
		  430,200,
		  // waffle
		  480,30,
		  // policebox
		  40,450,184, 
		  // machine
		  47,252,60,
		  42,55,196, 44,44,226, 43,79,220, 43,41,220, 44,46,226, 41,30,202,
		  44,200,226, 45,502,226, 46,492,224, 41,470,202},

		{ 28,700,200,
		  36,152,
		  470,40,
		  40,8,136, 47,328,44,
		  42,412,52, 44,420,82, 43,488,108, 45,132,98, 41,138,74,
		  42,25,196, 44,4,226, 43,23,220, 43,4,220, 42,16,226, 41,-8,202,
		  44,487,226, 45,492,226, 46,497,224, 41,471,202},
		
		{ 23,200,400,
		  36,152,
		  470,30,
		  40,8,136, 47,328,44,
		  41,470,202},

		{ // pNewMonster, pHat, pGhost
		  40,300,100,
		  // start position
		  430,200,
		  // waffle
		  480,20,
		  // policebox
		  40,450,184, 
		  // machine
		  47,352,76,
		  42,35,196, 44,24,226, 43,49,220, 43,21,220, 44,16,226, 41,10,202,
		  44,300,226, 45,502,226, 46,492,224, 41,470,202}};

		int k = 1;

		level ++;

		if (level > 6) level = 1;

		pNewMonster = l2[level-1][0] / 1000.0;
		pHat = l2[level-1][1] / 1000.0;
		pGhost = l2[level-1][2] / 1000.0;

		policeBoxX = l2[level-1][8];
		policeBoxY = l2[level-1][9];

		monsterMachineX = l2[level-1][11];
		monsterMachineY = l2[level-1][12];
		
		for (int i=0; i < 16; i++)
		{
			for (int j=0; j < 30; j++)
			{
				if ((l1[level-1][j] & k) != 0)
					playGround[i][j] = 1; else
					playGround[i][j] = 0;
								
			}

			k = k * 2;

		}

		x[0] = l2[level-1][3];
		y[0] = l2[level-1][4];
		dy[0] = 0;
		dx[0] = 0;

		kind[0] = 1;

		if (x[0] < 256) status[0] = 100; else
			status[0] = -100;
		
		for (int i=1; i < 10; i++) kind[i]=0;

		x[1] = l2[level-1][5];								   
		y[1] = l2[level-1][6];
		dy[1] = 0;
		dx[1] = 0;
		kind[1] = 12;
		status[1] = 0;

		animationCount = 0;
		gameStatus= 1;
		canGotoNextLevel = false;
		background.setColor(Color.white);
		background.fillRect(0,0,512,240);
		background.setColor(Color.black);

		newComment(1);

		// funky background
		
		/*int x1 = 200, y1 = 140;
		for (int i = 0; i < 100; i ++)
		{
 			background.setColor(new Color(255-i,255-i,255-i)); 
			
			background.drawOval(x1-i*4,y1-i*4,i*8,i*8);

		}*/

		background.drawImage(small[58],0,0,this);

		for (int i = 7; i < l2[level-1].length; i += 3)
			background.drawImage(small[l2[level-1][i]],l2[level-1][i+1],l2[level-1][i+2],this);

		for (int i =0; i < 16; i++)
			for (int j =0; j < 30; j++)
		{
			if	((playGround[i][j] != 0) && 
				(
				  ((i > 0) && (playGround[i-1][j] != 0)) || 
			   	  ((i < 15) && (playGround[i+1][j] != 0))) 
				) 
				playGround[i][j] = 2;

			switch (playGround[i][j]) {
			case 1:
				background.drawImage(small[49],i*32,8*j+40,this);
				break;
			case 2:
				background.drawImage(small[8],i*32,8*j+40,this);
				break;
			}
		}

	}

	public void startGame()
	{
		score = 0;
		level = 0;
		lives = 2;
		nextLevel();		
		
	}


	public void createNewBarrel()
	{
		int j = -1;
		for (int i= 0; (i < 10) && (j == -1); i++)
			if (kind[i] == 0) j=i;

		if (j != -1)
		{
			if (Math.random() < pGhost) 
			{
				kind[j] = 11;
				if (Math.random() < .5) {
					x[j] = 490;
					kind[j] = 14; 
				} else {
					x[j] = 1;
					kind[j] = 15;
				}

				y[j] = 20;
				dx[j] =0;
				dy[j] = 0;
				status[j] = -104;
			} else
			{
				x[j] = monsterMachineX;
				y[j] = monsterMachineY+15;
				dx[j] =-12+(int)Math.round(4*Math.random());
				// if ((level > 2) && (Math.random() > .5)) dx[j] = -11;
				dy[j] = -5;
				kind[j] = 10;
				status[j] = -104;
				if (Math.random() < pHat) kind[j] = 11;
			}
		}
	}

	public void destroy()
	{
	}

	public void paint(Graphics g)
	{	
		// copy offScreen to screen
		g.drawImage(offScreenImage,0,0,Color.white,this);

	}

	public void start()
	{
		if (m_noHats == null)
		{
			m_noHats = new Thread(this);
			m_noHats.start();
		}

	}

	public void stop()
	{
		if (m_noHats != null)
		{
			m_noHats.stop();
			m_noHats = null;
		}

	}

	public void run()
	{
		while (true)
		{
			try
			{
				repaint();
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				stop();
			}
		}
	}

    // check whether an object of dim wxh is colliding with the floor	
	public boolean checkFloorExact(int x, int y)
	{
		return ((x < 0) || (x >= 64) || (y < 0 ) ||
			((y >= 5 )) && (playGround[x / 4][y-5]!=0));
		
	}

	
	public boolean checkFloor(int x, int y, int w, int h)
	{
		boolean b = false;
		
		for (int i = (x+2) /8; i <= (x+w-1) / 8; i++)
			for (int j = (y+2) /8; j <= (y+h-1) / 8; j++)
			b = b || checkFloorExact(i,j);

		b = b || (x < 0);

		return b;
			
	}

	public void doStart()
	{
		gameStatus = 0;
		lives = 0;
		renderStartScreen();

	}

	public void doLostLife()
	{
		lives --;
		if (lives < 0) doStart(); else
		{
			level--;
			nextLevel();

		}
		
	}

	public void renderStartScreen()
	{
		/*background.setColor(Color.white);
		background.fillRect(0,0,512,240);
		
		int x1 = 200, y1 = 140;
		for (int i = 0; i < 100; i ++)
		{
 			background.setColor(new Color(255-i,255-i,255-i)); 
			background.drawOval(x1-i*4,y1-i*4,i*8,i*8);

		}*/
		background.drawImage(small[58],0,0,this);

		background.drawImage(small[34],36,48,this);
				
		background.setColor(Color.black); 

		background.drawString(
			"... click here to start ...",
			256-clickHereWidth,180);

		
	}
	
	public void update(Graphics g)
	{
		int dxo,dyo;
		animationCount++;
		offScreen.drawImage(backgroundImage,0,0,this);
		offScreen.drawString("Score "+new Integer(score).toString()
			//+" count "+new Integer(animationCount).toString()
			,10,20);

		for (int i = 0; i < lives; i++) 
			offScreen.drawImage(small[48],380-i*10,10,this);

		if (hiscore < score) hiscore=score;
		offScreen.drawString("Hi Score "+new Integer(hiscore).toString(),400,20);
			
		
		switch (gameStatus) 
		{
		case 0:
			if (drawClickHereLine)
				offScreen.drawLine(256-clickHereWidth,181,
					256+clickHereWidth,181);
				
			break;
			
		case 2:
			// game over sign

			if (lives == 0)
				offScreen.drawString(
					"GAME OVER",256-offScreen.getFontMetrics().
					stringWidth("GAME OVER")/2,140);

		case 3:
		case 1:

			// bubble text

			if (bubbleTextTime > 0) {
				bubbleTextTime --;

				offScreen.setColor(new Color(255,255,234));

				offScreen.fillRect(bubbleTextX-10, bubbleTextY-
					offScreen.getFontMetrics().getHeight()+2,
					offScreen.getFontMetrics().stringWidth(bubbleText)+20,
					offScreen.getFontMetrics().getHeight());
										
				offScreen.setColor(new Color(192,192,192));

				offScreen.drawRect(bubbleTextX-10, bubbleTextY-
					offScreen.getFontMetrics().getHeight()+2,
					offScreen.getFontMetrics().stringWidth(bubbleText)+20,
					offScreen.getFontMetrics().getHeight());
										
				offScreen.setColor(Color.black);
				
				offScreen.drawString(
					bubbleText, bubbleTextX, bubbleTextY);

				offScreen.setColor(Color.black);
				
			}

			if ((
				(((animationCount > 30) && (animationCount < 110)) ||
				(animationCount > 400)) &&
				(Math.random() < pNewMonster*3)) ||
				(Math.random() < pNewMonster))
				createNewBarrel ();

			for(int i = 0; i < 10; i++)
				if ((status[i] < -100) && (kind[i] != 1)) {
				status[i] ++;
				offScreen.drawImage(small[status[i]+157],x[i],
					y[i],this);
				if (status[i] == -100) status[i] = 0;
				

			} else
				if (kind[i] != 0) {
					int w = 0,h = 0;
					switch (kind[i])
					{
					case 1:
						w = 20; h =32;
						dy[i] ++;
						if (dy[i] > 9) dy[i]=9;

						if ((x[i] >= policeBoxX) && (x[i] <= policeBoxX+32) && (y[i] == policeBoxY+16) && (canGotoNextLevel) && (gameStatus == 1))
						{	
							dx[i] = 0;
							gameStatus = 3;
							if ((status[i]==-1) || (status[i] ==1)) status[0] = 1; else status[0] = -1;


						} /*else
							showStatus(Integer.toString(x[i])+", "+Integer.toString(y[i])+", "+Integer.toString(level));*/

						break;

					case 10:
						w = 13; h = 13;
						dy[i] ++;
						if (dy[i] > 9) dy[i]=9;
						break;

					case 11:
						w = 20; h = 12;
						dy[i] ++;
						if (dy[i] > 9) dy[i]=9;
						break;

					case 12:
						w = 18; h = 24;
						break;

					case 13:
						w = 21; h = 20;
						break;
					
					case 14:
					case 15:
						w = 14; h = 12;
						break;

					}
				
					// Collision detection /w floor

					dxo = dx[i]; dyo = dy[i];
					while ((dx[i] != 0) || (dy[i] != 0)) {
						if (dx[i] > 0) { dx[i]--; x[i]++; if (checkFloor(x[i],y[i],w,h)) {dx[i]=0; dxo=0; x[i]--;}}
						if (dx[i] < 0) { dx[i]++; x[i]--; if (checkFloor(x[i],y[i],w,h)) {dx[i]=0; dxo=0; x[i]++;}}
						if (dy[i] > 0) { dy[i]--; y[i]++; if (checkFloor(x[i],y[i],w,h)) {dy[i]=0; dyo=0; y[i]--;}}
						if (dy[i] < 0) { dy[i]++; y[i]--; if (checkFloor(x[i],y[i],w,h)) {dy[i]=0; dyo=0; y[i]++;}}
					}
					dx[i] = dxo;
					dy[i] = dyo;

					// Collision detection /w player

					if ((i != 0) && (x[0] <= x[i]+w) && (x[0]+20 > x[i]) &&
						(y[0] <= y[i]+h) && (y[0]+20 >= y[i]) && 
						(gameStatus == 1)) 
					{
						if ((kind[i] == 11) && (status[i] > 5))
						{
							if (status[i] < 30) {
								status[i] = 30; 
								score += 10;
								newComment(4);
							}

						} else
						if (kind[i] == 12) {
							if (status[i] < 8) {
								status[i] = 8; 
								score += 100;
								newComment(4);
								canGotoNextLevel = true;
							}

						} else
						if (kind[i] == 13) {
							if (status[i] == 0) {
								status[i] = 1; 
								score += 5;
								newComment(4);
							}

						} else
							playerDie();
					}

					
					switch (kind[i])
					{
					case 1:

						if ((gameStatus == 2) || (gameStatus == 3))
						{
							if (status[i] > 0) 
							{	if (status[i] < 7)
									offScreen.drawImage(small[status[i]+
									12],x[i],y[i],this);
								
								status[i]++; 
								
								
							} else 
							{	if (status[i] > -7)
									offScreen.drawImage(small[-status[i]+19],x[i],y[i],this);
								status[i]--;
									
							}
							if ((status[i] == -35) || (status[i] == 35))
							{
								if (gameStatus == 2)
									doLostLife(); else
									nextLevel();

							}							
						
						} else
						{

							if ((status[i] > 99) && (status[i] < 108)) {
								offScreen.drawImage(small[7-(status[i]-100)+19],x[i],y[i],this);
								status[i]++;
								if (status[i] == 108)
									status[i] = -2; } else
							if ((-status[i] > 99) && (-status[i] < 108)) {
								offScreen.drawImage(small[7-(-status[i]-100)+11],x[i],y[i],this);
								status[i]--;
								if (status[i] == -108)
									status[i] = -1; }
							else if (status[i] > 0) offScreen.drawImage(small[animationCount % 4 + status[i] * 4 - 4],x[i],y[i],this);
							else offScreen.drawImage(small[-status[i] * 4 - 4],x[i],y[i],this);

						}


		
						break;

					case 10:
						if ((x[i] == 0) || (x[i] == 499)) kind[i] = 0; else
							offScreen.drawImage(small[59+animationCount % 3],x[i],y[i],this);
						if ((dy[i]!=0) && (dx[i] > 0)) dx[i]--; 
						if ((dy[i]!=0) && (dx[i] < 0)) dx[i]++; 
						if ((dy[i]==0) && (dx[i]==0))
							if (Math.random() > 0.5) dx[i] =5+(int)Math.round(3*Math.random());
							else dx[i] = -5-(int)Math.round(3*Math.random());
					
						break;

					case 11:
						if ((x[i] == 0) || (x[i] == 492)) kind[i] = 0; else
						{
							// status 0..5

							if (status[i] < 6)
							{
								offScreen.drawImage(small[27+status[i] / 2],x[i],y[i],this);

								status[i] = (status[i] +1) % 6;

								if ((dy[i]==0) && (dx[i] > 0)) dx[i]--; 
								if ((dy[i]==0) && (dx[i] < 0)) dx[i]++; 
								if ((dy[i]==0) && (dx[i]==0))
									status[i] = 6; // hat goes to sleep

							} else
							if (status[i] < 10)
							{
								offScreen.drawImage(small[30],x[i],y[i],this);
								status[i] ++;

							} else
							if (status[i] < 20)
							{
								offScreen.drawImage(small[31],x[i],y[i],this);
								status[i] ++;
								if (status[i] == 20)
								{
									if (Math.random() < 0.3) 
										status[i] = 21;
									else
										status[i] = 10;
								}
							} else
							if (status[i] < 30)
							{
								offScreen.drawImage(small[30 + 2*(status[i] % 2)],x[i],y[i],this);
								status[i] ++;
								if (status[i] == 30)
								{
									status[i] = 0;
									dy[i] = (int)Math.round(-10*Math.random()-2);
									dx[i] = (int)Math.round(10*Math.random()-5);
								}
							} else
							{
								offScreen.drawImage(small[33],x[i],y[i],this);
								status[i] ++;
								if (status[i] == 40)
									kind[i] = 0;
							}
						
						}

					
						break;
					case 12:
						if (status[i] < 8) {
							offScreen.drawImage(small[35 + status[i] / 4],x[i],y[i],this);
							status[i] =(1+status[i]) % 8; }
						else if (status[i] < 18) {
							offScreen.drawImage(small[37],x[i],y[i],this);
							status[i] ++; }
						else kind[i] = 0;
						break;

					case 13:
						if (status[i] == 0)
							offScreen.drawImage(small[38],x[i],y[i],this);
						else if (status[i] < 10) {
							offScreen.drawImage(small[39],x[i],y[i],this);
							status[i] ++; }
						else kind[i] = 0;
						break;


					case 14:
					case 15:
						status[i]++;
						if (status[i] > 15) status[i] = 0;
						if (status[i] == 2) dx[i] = -1;
						if (status[i] == 7) dx[i] = -2;
						if ((status[i] == 9) ||
							((dx[i] == 0) && (dy[i] == 0))) dy[i] = (int)Math.round(5*Math.random()-2);
						if (status[i] == 12) dx[i] = -3;
						offScreen.drawImage(small[50 + status[i] / 4],x[i],y[i],this);
						if (kind[i] == 15) {
							if (dx[i] < 0) dx[i] = -dx[i];
							if (x[i] == 512-w) kind[i] = 0;
						} else
							if (x[i] == 0) kind[i] = 0;

						break;
					
					}
					
				}

		}

		paint(g);

	}
	
	public boolean mouseMove(java.awt.Event e,int x,int y)
	{
		drawClickHereLine = (
			(x >= 256-clickHereWidth) && (x <= 256+clickHereWidth) &&
			(y >= 180-clickHereHeight) && (y <= 180));
		
		return false;

	}

	
	public boolean mouseUp(java.awt.Event e,int x,int y)
	{
		if (drawClickHereLine && (gameStatus == 0))
		{
			drawClickHereLine = false;
			startGame();

		}
			

		return false;

	}
	public boolean keyDown(java.awt.Event e,int key)
	{
		if (gameStatus == 1)
		{
			if (key == 1006) 
			{
				dx[0] = -6;
				status[0] = 1;

			}
	
			if (key == 1007) 
			{
				dx[0] = 6;
				status[0] = 2;
			}

			if ((key == 32) && (checkFloor(x[0],y[0]+1,20,32)))
			{
				dy[0] = -12;
				if (Math.random() < 0.2) newComment(2);
		
			}
		}

		
		return false;

	}

	public boolean keyUp(java.awt.Event e,int key)
	{
		if ((gameStatus == 1) && ((key == 1006) || (key == 1007)))
		{
			dx[0] = 0;
			status[0] = -status[0];

		}

		return false;
	}
	
}
