import java.applet.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;

final class Editor {
    Image image[];
    Graphics g;
    platfoot p;
    int selectedIcon = 0;
    String iconchars = "123udlry*wbgaWUzcxpqhsSm";
    String explanation[] = {"std. wall","green box","yellow bouncy box","std. wall up","std. wall down",
			     "std. wall left","std. wall right","grey falling box",
			     "gold","gold half round mark","bomb","grey ghost","blue ghost",
			     "wasp","hands","quasi magnetic dust","comments","exit portal","internal portal",
			     "portal destination","heart","start (player right)","start (player left)","unknown"};
    
    final void drawIcon(int x, int y) {
	int ic = -1;
	g.setColor(new Color (74,104,105)); 
	g.fillRect (20+x*7,5+y*7,7,7);
	for (int i=0; i<24; i++) 
	    if (p.bane[y].charAt(x) == iconchars.charAt(i)) ic=i;

	if (ic >= 0) g.drawImage(image[ic],20+x*7,5+y*7,p);

    }

    final void drawStatusLine() {
	for (int i = 0; i < 24; i++) {
	    if (i == selectedIcon) g.setColor(new Color (195,219,219)); else
		g.setColor(new Color (74,104,105)); 
	    g.fillRect(19+i*20,455,8,8);
	    g.drawImage(image[i],20+i*20,456,p);
	    
	}

	g.setColor(new Color (74,104,105)); 
	g.fillRect(0,463,512,17);
	g.setColor(Color.white);
	g.drawString(explanation[selectedIcon],20,475);

    }

    public void draw (Graphics g_, platfoot p_) {
	g = g_;
	p = p_;
	g.setColor(new Color (74,104,105));
	g.fillRect (0,0,512,480);
	for (int y=0; y<64; y++)
	    for (int x=0; x<64; x++) drawIcon(x,y);

	drawStatusLine();

    }

    public void saveFile (String fn) {
	try {
	    FileWriter f = new FileWriter(fn);
	    PrintWriter udf = new PrintWriter(f);
	    
	    udf.println(" {");
	    for (int i = 0; i < 64; i++)
		if (i != 63) udf.println("  \""+p.bane[i]+"\","); else
		    udf.println(" \""+p.bane[i]+"\"");
	    udf.println(" }");
	    udf.close();

	}
	catch(Exception e) {};

    }

    public void save() {
	System.out.print(" { ");
	for (int i = 0; i < 64; i++)
	    if (i == 0) System.out.println("\""+p.bane[i]+"\","); else
	    if (i != 63) System.out.println("   \""+p.bane[i]+"\","); else 
		System.out.println("   \""+p.bane[i]+"\"");

	System.out.println(" }");
	
    }

    public void mouseDown(platfoot p, java.awt.Event e,int x,int y) {
	g = p.getGraphics();
	if ((x > 20) && (x < 468) && (y > 5) && (y < 453)) {
	    String s = p.bane[(y-5) / 7];

	    if (e.metaDown())
		s = s.substring(0,(x-20) / 7) + " " + s.substring((x-20) / 7+1,64); else
		s = s.substring(0,(x-20) / 7) + iconchars.charAt(selectedIcon) + s.substring((x-20) / 7+1,64);

	    p.bane[(y-5) / 7] = s;
	    drawIcon((x-20) / 7,(y-5) / 7);
	    
	}
	if ((x > 15) && (x < 500) && (y > 455) && (y < 464)) {
	    selectedIcon = (x-15)/20;
	    drawStatusLine();
	}
    }

    public void mouseDrag(platfoot p, java.awt.Event e,int x,int y) {
	g = p.getGraphics();
	if ((x > 20) && (x < 468) && (y > 5) && (y < 453)) {
	    String s = p.bane[(y-5) / 7];

	    if (e.metaDown())
		s = s.substring(0,(x-20) / 7) + " " + s.substring((x-20) / 7+1,64); else
		s = s.substring(0,(x-20) / 7) + iconchars.charAt(selectedIcon) + s.substring((x-20) / 7+1,64);

	    p.bane[(y-5) / 7] = s;
	    drawIcon((x-20) / 7,(y-5) / 7);
	    
	}
    }

    public void init (platfoot p) {
	Image collection; 
	MediaTracker mt = new MediaTracker (p);
	
	collection = p.getImage(p.getCodeBase(),"platfooticons.gif");
	
	image = new Image [24];
	for (int i=0; i < 24; i++)
	    image[i]=p.createImage(new FilteredImageSource(collection.getSource(),
							 new CropImageFilter(i*6,0,6,6)));
	
	for (int i =0; i < 24; i++)
	    mt.addImage(image[i],1);
	
	try { mt.waitForID(1); }
	
	catch(InterruptedException e) {};
    }
}

public final class platfoot extends Applet implements Runnable {

    // Editor editor;

    // constants

    final int mrPlatfootLogoY = 120;
    final int startClickY = 320;
    final int gtexty = 270;
    final int gtextwidth = 16;
    final int bRange = 32;
    final int screenWidth = 512;
    final int screenHeight = 480;
    final int blockSize = 64;
    final int noSprites = 16;
    final int maxShots = 3;
    final double downwardGravity = 0.25;
    final double airResistance = 0.9;
    final double wallElasticity = 0.5;
    final double portalSop = 0.9;
    final int bgBlockSize = 24;
    final int animationSpeed = 80;

    // game control

    Thread m_platfoot = null;

    Image offScreenImage, backgroundImage;
    Graphics offScreen, background;

    int animationCount, score = 0, hiscore = 0, lives = 0, round, gsca = 0;
    int gameOverAt, gameStartAt, introStartAt;
    String enteredCode = "_";
    boolean fastGFX = false;
    boolean gamePaused = false;
    int gameStatus;
    int startClick = 0, linkClick = 0, redoGameClick = 0;
    int magicX[], magicY[];
    Image image [];
    Image map;
    Image mazeG[];
    int paintAt = -1;

    // sprites

    double dx[], dy[], x[], y[], xr[], yr[];
    int status[], kind[];

    // maze (playground)

    int pg[][];
    int portaldx, portaldy;

    // list controlling which maze element to be moved

    int lp[][];

    // variables for saving game state

    int s_lp[];
    int s_pg[][];
    int s_round, s_direction;
    double s_x, s_y;
    boolean positionSaved;

    // control variables

    int viewx,viewy;
    int direction, purge, skyd;
    int noShots;

    String gameCodes[] = {"XYZ","KJL","AQP","BJZ","WPX","TEW" /*,"DOR"*/ };

    String gtext[] = {
	"this game is written during the summer / fall 1999 by Christian Hvid","visit my homepage at","http://vredungmand.dk",
	"do you have any comments or suggestions?","there are very welcome at","chvxx".substring(0,3)+"xxid@fastmail.fmx".substring(2,16)
    };

    final String endText[] = { "Contragulations!", "You have completed the game.", "You have reunited mr. Foot with his friend.",
			       "This is so sweet!", "And you are so good!", "click here to do it all again" };

    int etWidth[] = new int[6];
	

    int gtextw[];

    int linkAvaible = -1;

    final String comments[][] = { {"Hey mr. Foot!","Let's see you getting","of this mess!",
				   "Take the teleport!", "The other way is way", "too tough for you, dude.",
				   "Uh oh","Better get out of","here fast!"},
				  {"This round has code:","    F10 K J L",""},
				  {"This round has code:","    F10 A Q P",""}, 
				  {"This round has code:","    F10 B J Z",""},
				  {"This round has code:","    F10 W P X",""},
				  {"This round has code:","    F10 T E W",""} /*, {"This round has code:","    F10 D O R",""} */ };

    final int noRounds = 6;

    // editor - ingen final

   final String bane[]={

// bane 1

   "                     g11111                                     ",
   "                    g1     1                                    ",
   "                    1       1                                   ",
   "                   1         1                                  ",
   "                  1           1                                 ",
   "                 1           y1                                 ",
   "                1 yyy        y122222222222                      ",
   "               1  bbb        bbb         2                      ",
   "              1   b          bbb         2                      ",
   "   11111111111    b          bbb         2                      ",
   "   dbbbbb         b          bbb cccS    2                      ",
   "   dbgbgb         b           222222221bb2                      ",
   "   dbbgbb         b           ********1bb2                      ",
   "   dbbbbb         22222  1111111**11111bb2                      ",
   "   dbbbbb         g      1bbbbbbbbbbbbbbb2                      ",
   "   dbbbbb         g      1b111111111111111                      ",
   "   d*111111111111111111111b1                                    ",
   "   d*1                   1 1                                    ",
   "   d*1              111111 1                                    ",
   "   d*1              1      1                                    ",
   "   d*1              1      1                                    ",
   "   d*1             a1   1111                                    ",
   "   d*1              1   1                                       ",
   "   d*1              1   1                                       ",
   "   d*1              1   1      111111111                        ",
   "   d*1              1   1      1aaaaaaa1                        ",
   "   d*1              1   1      1a11a11a1                        ",
   "   d*1              1   1      1a11a11a1                        ",
   "   d*1111111111111111   11111111aaaaaaa1                        ",
   "   1***************** p ccc   bbbbbbbbb1                        ",
   "   1rrrrrrrrrrrrrrrr122211111111111111b1                        ",
   "                                     1b1                        ",
   "                                     1b1            11d         ",
   "                                     1b1            dqd         ",
   "                                     1b1            d*d         ",
   "                                     1b1            d*d         ",
   "                                   111b11111111111111*d         ",
   "                                   1            b     d         ",
   "                                   1   a        b     d         ",
   "                                   1  g a       b     11111     ",
   "                                   1 g x a      b         1     ",
   "                                   1 22222      b      gg 1     ",
   "                                   1            b      ga 1     ",
   "                                   111111111    b      gg 1     ",
   "                                           1    333333    1     ",
   "                                           1    bbbbbb    1     ",
   "                                       111y11111b1111111111     ",
   "                                       1bbbbbbbbb1              ",
   "                                       1b1111111 1              ",
   "                                       1b1bbb1   1              ",
   "                                       1b1b1b1   1              ",
   "                                       1b1b1b111 1              ",
   "                                       1b1b1b1y1 1              ",
   "                                 a a   1bbb1bby1 1              ",
   "                         11111111111111111111bb1 1              ",
   "                         1ph ccc                 1              ",
   "                         111lllllllllllllllllllll1              ",
   "                                  a a                           ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",


// bane 2

   "                                                                ",
   "                                                                ",
   "                              1                                 ",
   "                             1 1                                ",
   "                            1   1     1111111111                ",
   "                           1     1   1     1   1                ",
   "                          1       111      1   1                ",
   "                         1  yyyyy          1y  111111           ",
   "                  1111111   bbbbb          1y  1   W11111       ",
   "                  1         b   b           y  1y   W 1y1       ",
   "                  1  22     bs  b           y   y     1y1       ",
   "                  1  Wg     b   b          1b   y     1y1       ",
   "                  1  gW     bcccb          1 111y   U 1y1       ",
   "                  1  Wg   222222222            1b 1 1 1b1111    ",
   "                  1  gW    g     g             1  1 1  y hp1    ",
   "                  1  22                    U   1  1 1 1y1111    ",
   "                  1   bbU   y       U      11111  1b1 1y1       ",
   "                  1    b2   y       1      1g   bbbb111b1       ",
   "                  111  b2   y      1 1     1       b1bbb1       ",
   "                    1  b2   y     1   1    1       bbb 1        ",
   "                    1  bbbbby11111     1111111111111111         ",
   "                    1      bb1                                  ",
   "                    1       b1111                               ",
   "                    11111   b   1                               ",
   "                        1   bhp 1                               ",
   "                        111111111                               ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "  111111111111111                                               ",
   "  1             1                                               ",
   "  1             1                                               ",
   "  1   W  g  W   1                      111111111111111111       ",
   "  1             1                      1bbbbb      bbbbb1       ",
   "  1             111111111111111        1b              b1       ",
   "  1             1            y1        1b      W W     b1       ",
   "  1   g  q  g   1            y1        1      W   W    b1       ",
   "  1             1 yyyyyyyyyyyy1        1                1       ",
   "  1             1 bbbbbbbbbbbb1        1                1       ",
   "  1   W  g  W   11b           1        1     U  x  U    1       ",
   "  1              bb           1        1     2222222    1       ",
   "  1 U            b            1        1                1       ",
   "  1111111111111bbb            1        1                1       ",
   "              1               1        1b     a   a     1       ",
   "              1               1        1bbbbbb a a      1       ",
   "              1               1    1111111111b          1       ",
   "              1               1111 1   bbbbbbb          1       ",
   "              1                  1 1                    1       ",
   "              1                  111                    1       ",
   "              1                 **w     W              b1       ",
   "              1111111111111111111111               U   b1       ",
   "                                   1     3333333333333 b1       ",
   "                                   1                   b1       ",
   "                                   1                   b1       ",
   "                                   1b                  b1       ",
   "                                   1bbbbb           bbbb1       ",
   "                                   1111111111111111111111       ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",


// bane 3

   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                111111111111111111111111111111111               ",
   "                1g           2                  1               ",
   "                1 g          2                  1               ",
   "                1  22222222222   111111   1111  1               ",
   "                1  2               1         1  1               ",
   "                1  2 a             1 g  x    1  1               ",
   "                1  2  11111 b1111111  33333  1  1               ",
   "                1  2  1b b b b 1 b b  3b b   1  1               ",
   "                1  2  1 b b b b1baba  3 bab  1  1               ",
   "                1  2  1b 3333b 1 b33333b b1  1  1               ",
   "                1  2  1 b3gg3 b1b b b b ba1  1  1               ",
   "                1  2   b 3gg3b 1 b b b b b1  1  1               ",
   "                1  2  b b3333 b11111111   1  1  1               ",
   "                1  2  1b b b b        1   1  1  1               ",
   "                1     1 b b b b       1   1  1  1               ",
   "                1     1111111  33333  1   1111  1               ",
   "                1     1 b b 1  3WWW3  1         1               ",
   "                1  1  1bab b1  3WgW3  1      a  1               ",
   "                1  1  1 b3b 1  3WWW3      2222a 1               ",
   "                1  1111ba3 b1  33333      2  2  1               ",
   "                1  1     3b 1       b     2  2  1               ",
   "                1  1 g   3 b1        b   U2  2  1               ",
   "                1  1  3333b 111111    22222  2  1               ",
   "                1     3b b b          2   g  2  1               ",
   "                1     3 b b           2    g 2  1               ",
   "                1     3babab         U2  h3  2  1               ",
   "                1  1  3 b1111111  22222g 33  2  1               ",
   "                1  1           1  2     g    2  1               ",
   "                1  1           1  2             1               ",
   "                1s 1111111111111 a2222222222    1               ",
   "                1                 a   2         1               ",
   "                1  ccc                2         1               ",
   "                111111111111111111111111111111111               ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",


// bane 4

   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                     y          ",
   "                                      111111111111   y          ",
   "                                      1yyyyyyyyyy1  2y2         ",
   "                                      1bbbbbbbbbb1  2b2     2   ",
   "                                      1         b1222b222222 2  ",
   "           333333333333333            1         bbbbbb    y   1 ",
   "           3             3            1                   y x 1 ",
   "           3   W     W   3            1          222221111y1111 ",
   "           3  bbbbbbbbb  3            1  W1331W      2    y     ",
   "           3 Wb       bW 3            1 WzzzzzzW     2    y     ",
   "           3  b       b  3            111zzzzzz111   2    y     ",
   "           3  b  ***  b  3              3zzzzzz3 1   2    y     ",
   "           3  b  *q*  b  1111111111111  3zzzzzz322   2    y     ",
   "           3  b  ***  b  *********bab1  1zzzzzz1      2   y     ",
   "           3  b       b  22222221b11a1  1zzzzzz      2    y     ",
   "           3 Wb       bW 3      1a11b1111 1331      2     b     ",
   "           3  bbbbbbbbb  3      1baba*w   1  2       2    bbb   ",
   "           3   W     W   3      11111111111  222222   2  111b   ",
   "           3             3                        b2   2    b   ",
   "           333333333333333                        b 2 2     b   ",
   "                                               bbbb  2      b   ",
   "                                               b            b   ",
   "                                               bbbbbbbbbbbbbb   ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "       222222222                                                ",
   "       2       2                                                ",
   "       2    WW 2                                                ",
   "       2    WW 2                                                ",
   "       2       2         111111111                              ",
   "       2  s    2         1bbbbbbb1                              ",
   "       2       2111111   1bbbbbbb11111111111                    ",
   "       2  ccc   zzzzz1   1bbbbbbbbbbbbbbbbb1                    ",
   "       2222222221zzzz11111bbb1111111111111b1                    ",
   "                1zzzzzzzzzzzz1bbbb1      1b1                    ",
   "                1zzzzzzzzzzzz1bzzb1      1b1                    ",
   "                1zzzzzzzzzzzzzzzzb1      1b1                    ",
   "                1zzzzzzzzzzzzzzzzb1      1b1                    ",
   "                1zzzzzzzzzzzzzzzzb1      1b1                    ",
   "                1111zzzzz22222222111111111b1                    ",
   "                   1zzzzzzzzzzzzz1bbbbbbbbb1                    ",
   "                   1zzzzzzzzzzzzz1b1111111h1                    ",
   "                   222222222zzzzz1b1     1p1                    ",
   "                       1zzzzzzz11111     222                    ",
   "                       1zzzzzzz1                                ",
   "                       1zzzzzzz1                                ",
   "                       1bbbbbbb1                                ",
   "                       111111111                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",


// bane 5

   "                                11111111111111111111111111      ",
   "                                1                         111   ",
   "                                1               g            1  ",
   "                                1  3  3 1       g         2   1 ",
   "                                1   aa   1      g         U2  1 ",
   "                                1   aa    b      l  bbbbbb2U2 1 ",
   "                                1  3  3    b     l       bb    1",
   "                                1           b    l        b    1",
   "                                1 WW         1   l        b    1",
   "                                1             1  l W  q   b    1",
   "                                1  3  3        1   W      b    1",
   "                                1   gg          1  W      b    1",
   "                                1   gg        W  1  WWW        1",
   "                                1  3  3           1            1",
   "                                1             a    1  ddddd    1",
   "                                1             a g   1      ggg 1",
   "                                11111111111111 gxg   1         1",
   "                                              1 g     1        1",
   "                                               1 aa W  1       1",
   "                                                1       b      1",
   "                                                1        b     1",
   "                                                1         b    1",
   "                                                1          1   1",
   "                                                1           1  1",
   "                                                1              1",
   "                                                1  3  3  3  3  1",
   "                              11111             1   gg         1",
   "                            1111   11           1   gg         1",
   "                           1111      1          1  3  3WW3  3  1",
   "                           1111   11 1          1              1",
   "                          11111   11  1         1              1",
   "                          111111      1         1111111111111111",
   "11111111111111111         1111111     1                         ",
   "1               1         11111111    1                         ",
   "1       W       1         111  1111   1                         ",
   "1  3  3 W 3  3  1          11  1111  1                          ",
   "1   aa     gg   1          11111111  1                          ",
   "1   aa     gg   1           111111 11                           ",
   "1  3  3   3  3  1             11111                             ",
   "1               1                                               ",
   "1  1            1                                               ",
   "1   1           1                                               ",
   "1    b          1                                               ",
   "1     b         1                                               ",
   "1      b        1                                               ",
   "1       1       1                                               ",
   "1        1  W aa 1                                              ",
   "1         1     g 1                                             ",
   "1 ggg      1   gpg 1111111111111                                ",
   "1    lllll  1   g a            1                                ",
   "1            1    a            1                                ",
   "1        WWW  1          3  3  1                                ",
   "1    b      W  1  W       gg   1                                ",
   "1    b      W   1         gg   1                                ",
   "1    b   S  W d  1       3  3  1                                ",
   "1    b        d   1         W  1                                ",
   "1    b ccc    d    b        W  1                                ",
   "1    bb       d     b    3  3  1                                ",
   " 1 2U2bbbbbb  d      b         1                                ",
   " 1  2U         g      1        1                                ",
   " 1   2         g       1 3  3  1                                ",
   "  1            g               1                                ",
   "   111                         1                                ",
   "      11111111111111111111111111                                ",


// bane 6

   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "        1111111111111111111111111111111111111111111111111111111 ",
   "        1                                                     1 ",
   "        1 g  1yyyyy111111111111111111111111111111111111111111 1 ",
   "        1  g 1yyyyy1                                       h1 1 ",
   "        1 g  1yyyyy1  b b b b b b b b b b b b b b b b b b b 1 1 ",
   "        1  g 1yyyyy1 r r r r r r r r r r r r r r r r r r d  1 1 ",
   "        1 g  1bbbbb1      W   W   W   W   W   W   W   W   b 1 1 ",
   "        1  g 1b   b1 r r r r r r r r r r r r r r r r r d d  1 1 ",
   "        1 g  1b q b1    W   W   W   W   W   W   W   W   W b 1 1 ",
   "        1  g 1b   b1 r r r r r r r r r r r r r r r r d d d  1 1 ",
   "        1 g  1bbbbb1      W   W   W   W   W   W   W   W   b 1 1 ",
   "        1  g 1     1 r r r r r r r r r r r r r r r d d d d  1 1 ",
   "        1 g  1     1    b b b b b b b b b b b b b   W   W b 1 1 ",
   "        1  g 1     1 u u                       w d d d d d  1 1 ",
   "        1 g  1     1     1111111111111111111111 b         b 1 1 ",
   "        1  g 1     1 u u 1                    1             1 1 ",
   "        1 g  1           1 111111111111111111 1 b         b 1 1 ",
   "        1  g 1111111111111 1                1 1W     g     W1 1 ",
   "        1                  1                1 1 b   a a   b 1 1 ",
   "        11111111111111111111                1 1W   g   g   W1 1 ",
   "                                            1 1 b   a a   b 1 1 ",
   "                                            1 1W     g     W1 1 ",
   "                                            1 1 b         b 1 1 ",
   "                                            1 1             1 1 ",
   "                                            1 1 b U     U b 1 1 ",
   "                                            1 1  222 w 222  1 1 ",
   "                                            1 1 3         3 1 1 ",
   "                                            1 1b           b1 1 ",
   "                         11111111111111111111 1g3   u u   3 1 1 ",
   "                         1                    1b           b1 1 ",
   "                         1 11111111111111111111g3 l W W r 3g1 1 ",
   "                         1 1        zzzzzz bgbgb           b1 1 ",
   "                         1 1  22222 lzlzlz3 3 3 3 l W W r 3g1 1 ",
   "                         1 1  2     zzzzzz                 b1 1 ",
   "                         1 1  2   b lzlzlz  W W     d d   3 1 1 ",
   "                         1 1 g2  W  zzzzzz                 b1 1 ",
   "                         1 1gh2 x b lzlzlz3 3 3 3 3 3 3 3 3g1 1 ",
   "                         1 1 g2  W  zzzzzz b b b b bgbgb bgb1 1 ",
   "                         1 1  2   b lzlzlz 111111111111111111 1 ",
   "                         1 1  2     zzzzzz 1                  1 ",
   "                         1 1  22222 lzlzlz 1 111111111111111111 ",
   "                         1 1        zzzzzz 1 1                  ",
   "                         1 11111111111111111 1                  ",
   "                         1                   1                  ",
   "                         111111111111111111111                  ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                         111111111111111111111                  ",
   "                         1        b b b      1                  ",
   "                         1 11rrrr rbrbrb222  1                  ",
   "                         1 1      b bWb  W2  1                  ",
   "                         1 1    W rbrbrb b2  1                  ",
   "                         1 1s  WW b b b p 2  1                  ",
   "                         1 1    W rbrbrb b2  1                  ",
   "                         1 1 ccc  b bWb  W2  1                  ",
   "                         1 11rrrr rbrbrb222  1                  ",
   "                         1        b b b      1                  ",
   "                         111111111111111111111                  ",
   "                                                                "

   /*



	"                                                                ",
	"                       1111111111111111111111111111             ",
	"                       1            bbbbbb        1             ",
	"                       1    bbbbbb  baaaab bbbbbb 1             ",
	"                       1    bWWWWb  bbbbbb bggggb 1             ",
	"                   11111    bbbbbb         bggggb 1             ",
	"                   1bbbbb  13333331        bbbbbb 1             ",
	"                   1b111b  1      1111111111111   1             ",
	"           111111111b1W1b111       ******bbbUU1   1             ",
	"           1        b1b1          1111111111111   1             ",
	"           1        b1W1b111  U   1        1      1             ",
	"           1        b111b1b1 llllllllllllll1111 q 1             ",
	"           1       1bbbbbbb1         bbb  Sccc1   1             ",
	"     1111111bbbbbbb1111b1111 llllllllllllll1111   1             ",
	"     1bbbbb1b     bbbb1bbbb1 1bbbb1        1      1             ",
	"     1bW  bbb    W   b1bzzz1 1bzzb1        1      1             ",
	"     1b              b1bzzzzzzzzzb1111111111      11111111111   ",
	"     1b          W   b1bzzzzzzzzzb1                         1   ",
	"     1bWW       WW   b1bzzzzzzzzzb1   W                 W   1   ",
	"     1bbbbbbbbbbbbbbbb1bbbbbbbbbbb1  bbbbbbbbbbbbbbbbbbbbb  1   ",
	"     11p111111111111111111   111111 Wb                   bW 1   ",
	"      111               U1   1U   1  b                   b  1   ",
	"                       111   111  1  b        ***        b  1   ",
	"                       1W     W1  1  b                   b  1   ",
	"                       1       1  1 Wb                   bW 1   ",
	"                       1g     g1  1  bbbbbbbbbbbbbbbbbbbbb  1   ",
	"                       1bbbbbbb1  1   W                 W   1   ",
	"                       111111111  1                         1   ",
	"                              1   111111111      111111111111   ",
	"                              1  Wgg      1      1     1 1      ",
	"                              1  gg       1      1     1 1      ",
	"           333333333333333    1        1W 1      1     1 1      ",
	"           3             3    1     1111111              11111  ",
	"           3   W     W   3    1                              1  ",
	"           3  bbbbbbbbb  3    1                w             1  ",
	"           3 Wb       bW 3 111111111333333    222222111      1  ",
	"           3  b       b  3   1                      1        1  ",
	"           3  b  ***  b  3   1                      1  1111  1  ",
	"           3  b  *h*  b  11111  111111  11111111111 1    1   1  ",
	"           3  b  ***  b      1   1zzz1         1         1   1  ",
	"           3  b       b  222 1   1 1z1         1         1   1  ",
	"           3 Wb       bW 3 1     1 1z111111    1         1   1  ",
	"           3  bbbbbbbbb  3 1     1 1zzzzzz1    11111111111   1  ",
	"           3   W     W   3 1       111111z1                  1  ",
	"           3             3 1  ppp1  zzzz1z1WW              111  ",
	"           333333333333333 1  p1p111111z1z111111     1       1  ",
	"                           1 pp1pp    1zzzzzzzz1     1       1  ",
	"                       111111p111     11111111z111111111111111  ",
	"                        1     1     WW  1zzzzzzzzzzzzzzz1       ",
	"                        1     1     WWa 11111111111111111       ",
	"                        1     111111 aa  bbb WW1      1         ",
	"                        1 W a W   1 b11111b 1zz1bbbbbb1         ",
	"                        1 a111a   1 bb 1  b 1zz111111b1         ",
	"                        1 a111a       bbbbb 1zz1 bbbbb1         ",
	"                        1 W a W        1   W1bbbbb11111         ",
	"                        111111111111111111111 WW1111 1          ",
	"                                      1              1          ",
	"                                      1111111     1111          ",
	"                                          2        2            ",
	"                                          2   cccx 2            ",
	"                                          2222222222            ",
	"                                                                ",
	"                                                                ",
        "                                                                ",


   "                                     1                          ",
   "                  1                 1y1                         ",
   "                 1 1               1yb 1                        ",
   "                1   1         1   1yb   1 1                     ",
   "               1  W  1       1 1 1yb     1 1                    ",
   "              1    W  1     1   1yb     1   1                   ",
   "             1         1   1   yyb     1     1                  ",
   "      1111111    U    1 1 1    yb     1       1                 ",
   "      1  1yyy   222  1   1     y               1                ",
   "      1   bbb       b          y  W 1222      1                 ",
   " 111111  rrrr11    b           y W 1 1g1   p 1                  ",
   " 1            1   1         1  yW 1   1 1 222                   ",
   " 1            1  1      bg   1 y 1       1 1                    ",
   " 1  11111111  1 1   w   gb  1 1y1 1  bbbbb1                     ",
   " 1  1      1  11   222 gbg 1   b   1 b111b 1                    ",
   " 1  1    S 1  1y1       gb1         1bbb1b  1                   ",
   " 1  1  ccc 1  1by1      bg           1 a1a   1                  ",
   " 1  1  22211  1bby1      b            2a1     1                 ",
   " 1  1         1 bby1       WWW        2 1      1                ",
   " 1  1         1  bby1         U U     2 1       1               ",
   " 1  11111111111   bby   1    22222    2 1        1              ",
   " 1       1         bb  1 1     1      2 1   U     1             ",
   " 1       1WWW       b 1   1   1       2    111 a   1            ",
   " 111111  11111       1     1 1        2             1           ",
   "      1  1yyy       1   x   1 1       2              1          ",
   "      1   bbb      1   222     1     1   1111111111   1         ",
   "      111rrrr  2221             11111                  1        ",
   "         2   1   1    U     aaa  1      aaa             1       ",
   "        333   1 1   11111       1              1         1      ",
   "       1   1   1           1   1     11111     1    U     1     ",
   "      1     1         U    1  1      1   1     1b1111111   1    ",
   "   W 1   b   1 W   111111  1 y 1     1   1111  1            1   ",
   "    1         1       1      y  1    1      1  1    1   aa   1  ",
   "   1     W     1   a  1      b   1   1U    U1 b1    1   aa    1 ",
   "  1     bbb     1     1111111b    1bb1111  11bb1111 1          1",
   " 1     bb bb     1                  b1              1  11111  1 ",
   "3     bb   bb     3        1111111  b1              1        1  ",
   "3  b Wb  q  bW b  3 11111        1b111111  1111  1  1       1   ",
   "3     bb   bb     3 1bbb1U       1b1  y       1  1  1   U  1    ",
   " 1     bb bb     1  1bbb11111111 1b1  y       1111  11111 1     ",
   "  1     bbb     1   1bbb1y    y1 1b1  y 1111  1          1      ",
   "   1     W       1111bbb1by   y1   1  y       1      aa 1       ",
   "    1                bbbbbby  y1   1  y  aa 22222  U    1       ",
   "   W 1   b     W          bby y1  1y1 y  aa   1   1111  1       ",
   "      1     1              bbyy1 1ybb1b1      1      1  1       ",
   "       1   1 1              bby11ybbb b1             1  1       ",
   "        333   1    U     g   bb1ybb b b1111   11111  1  1       ",
   "              1   222   g     b bb  bbbbb 1   1ggg1  1  1       ",
   "              1        g      b     11b b 1   1g g1  1  1       ",
   "              1               b     bbb b 1   1ggg1  1  1       ",
   "              1               b     1111b 1   11111     1       ",
   "               11111111111111111    yyy1b               1       ",
   "                               1    bbbbb               1       ",
   "                               1U   1ll1          22222 1       ",
   "                               11111 1   U   U          1       ",
   "                                     1  2222222         1       ",
   "                                     1                  1       ",
   "                                     11111111111111111111       ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                ",
   "                                                                "*/

};

    
    void cropImages (String fn) {
	
	MediaTracker mt;
	int imageArea[] = {0,0,32,32, 0,32,32,32, 0,64,64,64,
			   64,64,16,16, 80,64,16,16, 96,64,16,16, 112,64,16,16,
			   16,64,16,16, 0,80,16,16,

			   128,64,32,32, 160,64,32,32, 192,64,32,32, 224,64,32,32,

			   0,128,64,64, 64,128,64,64, 128,128,64,64, 192,128,64,64,
			   0,192,64,64, 64,192,64,64, 128,192,64,64, 192,192,64,64,
			   0,256,64,64, 64,256,64,64, 128,256,64,64, 192,256,64,64,
			   0,320,64,64, 64,320,64,64, 128,320,64,64, 192,320,64,64,

			   // Pile
			   0,64,16,16, 16,64,16,16, 32,64,16,16, 48,64,16,16,
			   0,80,16,16, 16,80,16,16, 32,80,16,16, 48,80,16,16,
			   0,96,16,16, 16,96,16,16, 32,96,16,16, 48,96,16,16,
			   0,112,16,16, 16,112,16,16, 32,112,16,16, 48,112,16,16,

			   // Mand - oerene nede (45, 46)

			   32,0,32,32, 32,32,32,32,

			   // Bombe

			   128,96,32,32, 160,96,32,32, 192,96,32,32, 224,96,32,32,

			   // Kugle (51)

			   0,384,16,16, 16,384,16,16, 32,384,16,16, 48,384,16,16,

			   // monster (55)

			   0,432,32,32, 32,432,32,32, 64,432,32,32, 96,432,32,32,

			   // monster udspyder (59)

			   0,448,64,64,

			   // question marks (60, 61)
			   
			   128,400,24,24, 32,400,32,32,

			   // Tal 0..9 (62)

			   256,0,12,14, 268,0,12,14, 280,0,12,14, 292,0,12,14, 304,0,12,14,
			   316,0,12,14, 328,0,12,14, 340,0,12,14, 352,0,12,14, 364,0,12,14,

			   // Game logo (72)

			   0,512,512,150,

			   // exploding monster (73 .. 76)

			   128,432,32,32, 160,432,32,32, 192,432,32,32, 224,432,32,32,

			   // game over (77)

			   256,16,174,32,

			   // Mand - doe (78 .. 83) - h

			   64,0,32,32, 96,0,32,32, 128,0,32,32, 160,0,32,32, 192,0,32,32, 224,0,32,32,

			   // Mand - doe (84 .. 89) - v

			   64,32,32,32, 96,32,32,32, 128,32,32,32, 160,32,32,32, 192,32,32,32, 224,32,32,32,

			   // "blaat" monster (90 .. 97)

			   0,464,32,32, 32,464,32,32, 64,464,32,32, 96,464,32,32,
			   128,464,32,32, 160,464,32,32, 192,464,32,32, 224,464,32,32,

			   // going down (98)

			   256,50,170,20,

			   // high (99)

			   256,100,16,5,

			   // head (100)

			   256,84, 30,16,

			   // click here to start (101 .. 102)

			   256,106, 326,14, 256,120,326,14,

			   // exit (103 .. 104)

			   256,136,26,14, 282,136,26,14,

			   // portal animation (105 .. 108)

			   256,150,32,32, 288,150,32,32, 320,150,32,32, 352,150,32,32,

			   // hjerter (109 .. 110)
			   
			   256,184,17,15, 274,184,7,6,

			   // teleportation (111 .. 114)

			   256,200,6,6, 262,200,6,6, 268,200,6,6, 274,200,6,6,

			   // bricks (115) 

			   260,220,64,64,
			   324,220,64,64,
			   388,220,64,64,

			   // magic dust ( 118 .. 121 )

			   260,284,8,8,
			   268,284,8,8,
			   276,284,8,8,
			   284,284,8,8,

			   // wasp ( 122 .. 124, 125 .. 127, 128 .. 131 )

			   256,300,32,32, 288,300,32,32, 320,300,32,32,
			   256,332,32,32, 288,332,32,32, 320,332,32,32,
			   256,364,32,32, 288,364,32,32, 320,364,32,32, 352,364,32,32,

			   // game paused (132)

			   256,400,262,20,

			   // sign (133)

			   256,430,192,64,

			   // udspyder (134 .. 137)

			   452,220,64,64, 516,220,64,64, 580,220,64,64, 644,220,64,64, 

			   // kugle (138 .. 141)

			   388,284,8,8, 396,284,8,8, 404,284,8,8, 412,284,8,8,

			   // "glas" 142

			   448,430,64,64, 512,430,64,128, 576,430,64,128, 640,430,64,128, 704,430,64,128,

			   // struds 147

			   360,300,27,46, 388,300,40,46, 430,300,41,46, 472,300,39,46, 512,300,27,46,

			   // afslutning 152

			   0,662,512,348,

			   // tada 153

			   512,660,170,52

   
	 
	};

	Image collection;
	mt = new MediaTracker (platfoot.this);
	showStatus("loading images ...");
	
	collection = getImage(getCodeBase(),fn);

	// crop images
	
	image = new Image [imageArea.length / 4];
	for (int i=0; i < imageArea.length / 4; i++)
	    image[i]=createImage(new FilteredImageSource(collection.getSource(),
							 new CropImageFilter(imageArea[i*4], imageArea[i*4+1], imageArea[i*4+2], imageArea[i*4+3])));
	
	for (int i =0; i < imageArea.length / 4; i++)
	    mt.addImage(image[i],1);
	
	try { mt.waitForID(1); }
	
	catch(InterruptedException e) {};

	// make images for the maze

	mazeG = new Image[16];
	int xx[] = new int[64*64];
	for (int i = 0; i < 16; i++)
	    {
		mazeG[i] = createImage(64,64);
		Graphics g = mazeG[i].getGraphics();
		
		if ((i & 1) == 0) 
		    { 
			// tv
			if ((i/4) % 2 == 0) g.drawImage(image[9],0,0,this); else
			    g.drawImage(image[7],0,0,this);

			// th
			if ((i/8) % 2 == 0) g.drawImage(image[10],48,0,this); else
			    g.drawImage(image[7],48,0,this);

			g.drawImage(image[7],16,0,this);
			g.drawImage(image[7],32,0,this);
		    }
		
		if ((i & 2) == 0) 
		    { 
			// bv
			if ((i/4) % 2 == 0) g.drawImage(image[11],0,48,this); else
			    g.drawImage(image[7],0,48,this);

			// bh
			if ((i/8) % 2 == 0) g.drawImage(image[12],48,48,this); else
			    g.drawImage(image[7],48,48,this);

			g.drawImage(image[7],16,48,this);
			g.drawImage(image[7],32,48,this);
		    }
		
		if ((i & 4) == 0) 
		    { 
			// tv
			if ((i/1) % 2 == 0) g.drawImage(image[9],0,0,this); else
			    g.drawImage(image[8],0,0,this);

			// bv
			if ((i/2) % 2 == 0) g.drawImage(image[11],0,48,this); else
			    g.drawImage(image[8],0,48,this);

			g.drawImage(image[8],0,16,this);
			g.drawImage(image[8],0,32,this);
		    }
		
		if ((i & 8) == 0)
		    { 
			// th
			if ((i/1) % 2 == 0) g.drawImage(image[10],48,0,this); else
			    g.drawImage(image[8],48,0,this);

			// bh
			if ((i/2) % 2 == 0) g.drawImage(image[12],48,48,this); else
			    g.drawImage(image[8],48,48,this);

			g.drawImage(image[8],48,16,this);
			g.drawImage(image[8],48,32,this);
		    }

		if (((i & 8) != 0) && ((i & 2) != 0)) 
		    g.drawImage(image[9],48,48,this);

		if (((i & 4) != 0) && ((i & 2) != 0)) 
		    g.drawImage(image[10],0,48,this);

		if (((i & 8) != 0) && ((i & 1) != 0)) 
		    g.drawImage(image[11],48,0,this);

		if (((i & 4) != 0) && ((i & 2) != 0)) 
		g.drawImage(image[12],0,0,this);
	    }
	
	showStatus("done loading images ...");
	
    }

    public void renderBackground()
    {
	background = backgroundImage.getGraphics();

	for (int i = 0; i < (screenHeight+bgBlockSize)/bgBlockSize+1; i++)
	    {
		background.setColor(new Color(i,100-i*4,20+i));
		background.fillRect(0,i*bgBlockSize,screenWidth+bgBlockSize,bgBlockSize);
	
	    }
	
	for (int i = -1; i < (screenWidth+bgBlockSize)/bgBlockSize+2; i++)
	    for (int j = 0; j < (screenHeight+bgBlockSize)/bgBlockSize+2; j++)
	    {
		double k = (80 - Math.pow(15.1-i,2) *.2- Math.pow(14.1-j,2)*.3) / 150;

		background.setColor(new Color((float)(k*.3+0.14),(float)(k*.37+0.11),(float)(k*0.45+0.18)));

		background.fillRect(i*bgBlockSize,j*bgBlockSize,bgBlockSize,bgBlockSize);
		background.drawImage(image[60],i*bgBlockSize,j*bgBlockSize,this);

	    }
		
    }

    public String getAppletInfo()
    {
	return "Platfoot - Christian Hvid 1999";

    }

    public void init()
    {

	cropImages("platfoot.gif");

	/*	editor = new Editor();
		editor.init(this);*/

	resize(screenWidth, screenHeight);
	animationCount = 0;
	
	offScreenImage = createImage(screenWidth, screenHeight);
	offScreen = offScreenImage.getGraphics();
	
	backgroundImage = createImage(screenWidth+bgBlockSize, screenHeight+bgBlockSize);
	renderBackground();

	pg = new int [64][64];
	s_pg = new int [64][64];

	// initialize sprites

	x = new double [noSprites];
	y = new double [noSprites];
	dx = new double [noSprites];
	dy = new double [noSprites];
	xr = new double [noSprites];
	yr = new double [noSprites];
	kind = new int [noSprites];
	status = new int [noSprites];

	lp = new int[2][200];
	s_lp = new int[200];

	// initialize magic dust

	magicX = new int[256];
	magicY = new int[256];

	for (int i = 0; i < 256; i++) {
	    int m = (int)Math.round(Math.sin(2*Math.PI/256*i)*25)+i;
	    magicX[i] = m%64+(int)Math.round(Math.sin(2*Math.PI/64*m)*32);
	    magicY[i] = m%64;

	}

	setupFont();

	doMenu();
	
    }

    void savePosition()
    {
	for (int i = 0; i < 64; i++)
	    for (int j = 0; j < 64; j++)
		s_pg[i][j] = pg[i][j];

	s_round = round;
	s_x = x[0];
	s_y = y[0];

	if (direction > 0) s_direction = 1; else s_direction = -1;

	for (int i=0; i < lp[animationCount % 2][0]+1; i++)
	    s_lp[i] = lp[animationCount % 2][i];

	positionSaved = true;
	
    }

    void loadPosition()
    {
	for (int i = 0; i < 64; i++)
	    for (int j = 0; j < 64; j++)
		pg[i][j] = s_pg[i][j];

	round = s_round;
	
	x[0] = s_x;
	y[0] = s_y;
  	for (int i = 0; i < noSprites; i++) kind[i] = -1;

 	dx[0]=0;
	dy[0]=0;
	direction = s_direction;
	kind[0] = 0;
	purge=0;
	noShots=0;
	status[0]=-24;
	gameStatus = 1;
	gameStartAt = animationCount;

	for (int i=0; i < s_lp[0]+1; i++)
	    lp[animationCount % 2][i] = s_lp[i];

    }

    public void doMenu()
    {
	gameStatus = 0;
	introStartAt = animationCount;
	showStatus(getAppletInfo() + " ...");

    }

    public void doGame(int round1)
    {
	score = 0;
	lives = 2;
	round = round1;
	enteredCode = "_";
	doRound();

    }

    void doFinitoGame() {
	gameStatus = 9;
	gsca = animationCount;

    }

    public void doNextRound()
    {
	round ++;

	if (round > noRounds)
	    doFinitoGame(); else
	    doRound();

    }

    public void doRound()
    {

	// make maze

	int a = ((round-1) % noRounds)*64;
	int commentCount = 0;

	for (int j=0; j<pg.length; j++)
	    for (int i=0; i<pg[j].length; i++)
		{
		    pg[i][j]=0;
		    switch (bane[j+a].charAt(i)) {
		    case '1': pg[i][j]=1; break;
		    case '2': pg[i][j]=4096; break;
		    case '3': pg[i][j]=8192; break;
		    case 'u': pg[i][j]=1 | 256; break;
		    case 'd': pg[i][j]=1 | 512; break;
		    case 'l': pg[i][j]=1 | 1024; break;
		    case 'r': pg[i][j]=1 | 2048; break;
		    case '*': pg[i][j]=-1; break;
		    case 'w': pg[i][j]=-71; break;
		    case 'b': pg[i][j]=-11; break;
		    case 'y': pg[i][j]=16384; break;
		    case 'm': pg[i][j]=-999; break;
		    case 'W': pg[i][j]=-541; break;
		    case 'g': pg[i][j]=-501; break;
		    case 'a': pg[i][j]=-511; break;
		    case 'z': pg[i][j]=-531; break;
		    case 'U': pg[i][j]=-551; break;
		    case 'A': pg[i][j]=-701; break;
		    
		    case 'c': 
			{
			    if (pg[i-1][j] == -602)
				pg[i][j]=-603; else
				    {
					if ((pg[i-1][j] <= -610) && (pg[i-1][j] > -620))
					    pg[i][j]=-602; else {
						pg[i][j]=-610-commentCount;
						commentCount ++;
					    }
				    }
			}
			break;

		    case 'x': pg[i][j]=-21; break;
		    case 'p': pg[i][j]=-22; break;
		    case 'h': pg[i][j]=-31; break;
		    case 'q': 
			{
			    portaldx = i * blockSize+2;
			    portaldy = j * blockSize+2;
			}
			break;

		    case 's': 
			{
			    x[0]= i*blockSize+2; y[0]= j*blockSize+2;
			    direction = 1;
			    dx[0] = 2;
			    pg[i][j]=31415927;
			    
			} 
			break;

		    case 'S': 
			{
			    x[0]= i*blockSize+2; y[0]= j*blockSize+2;
			    direction = -1;
			    dx[0] = -2;
			    pg[i][j]=31415927;
			    
			} 
			break;
		    }
		}

	for (int i=0; i<pg.length; i++)
	    for (int j=0; j<pg[i].length; j++)
		if ((pg[i][j] > 0) && (pg[i][j] < 4096))
		    {
			if ((i > 0) && (pg[i-1][j] > 0) && (pg[i-1][j] < 4096)) pg[i][j]+=4;
			if ((i < pg.length-1) && (pg[i+1][j] > 0) && (pg[i+1][j] < 4096)) pg[i][j]+=8;
			if ((j > 0) && (pg[i][j-1] > 0) && (pg[i][j-1] < 4096)) pg[i][j]+=1;
			if ((j < pg[i].length-1) && (pg[i][j+1] > 0) && (pg[i][j+1] < 4096)) pg[i][j]+=2;
		    }

	lp[0][0] = 0;
	lp[1][0] = 0;

	// make graphical map

	int bane[] = new int[361*384];
	for (int i=0; i < 64; i++)
	    for (int j=0; j < 64; j++) {
		int k = 0;
		
		if (pg[i][j] > 0) k = 255 << 24 | 30 << 16 | 50 << 8 | 100;
		if ((pg[i][j] == -21) || (pg[i][j] == -22)) k = 255 << 24 | 192 << 16 | 0 << 8 | 0;
		if (pg[i][j] == 31415927) { k = 255 << 24 | 96 << 16 | 156 << 8 | 102; pg[i][j] = 0; }
		if (pg[i][j] == -11) k = 255 << 24 | 0 << 16 | 0 << 8 | 0;
		if (pg[i][j] == -1) k = 255 << 24 | 150 << 16 | 150 << 8 | 50;
		if (pg[i][j] == -71) k = 255 << 24 | 150 << 16 | 150 << 8 | 50;
		if ((pg[i][j] == -501) || (pg[i][j] == -511) || (pg[i][j] == -541)) k = 255 << 24 | 120 << 16 | 120 << 8 | 150;

		if (k != 0) {
		    if (pg[i][j] > 0)
			for (int n = 0; n < 30; n++) bane[384*20+n % 6 + (n/6)*384+i*6+j*5*384] = k;

		    bane[384*22+2+i*6+j*5*384] = k;
		    bane[384*22+2+i*6+1+j*5*384] = k; 
		    bane[384*22+2+i*6+2+j*5*384] = k; 
		    bane[384*22+2+i*6+384+j*5*384] = k;
		    bane[384*22+2+i*6+385+j*5*384] = k;
		    bane[384*22+2+i*6+386+j*5*384] = k;

		}
		
	    }

	map = createImage(new MemoryImageSource (384,361,bane,0,384));

	// initialize sprites

	for (int i = 0; i < noSprites; i++) kind[i] = -1;

	// initialize player

	dy[0]=2;
	kind[0] = 0;
	purge=0;
	noShots=0;
	status[0]=-24;

	// initialize view

	gameStatus = 3;
	gameStartAt = animationCount;

	if (direction > 0) viewx = (int)Math.round(x[0])-256+32; else viewx = (int)Math.round(x[0])-256;
	viewy = (int)Math.round(y[0])-240-80;

	positionSaved = false;

    }

    synchronized public void paint(Graphics g)
    {	
	/*	if ((gameStatus == 4) || (gameStatus == 5)) {
	   editor.draw(g, this);
	   } else */


	g.drawImage(offScreenImage,0,0,Color.white,this);

	paintAt = animationCount;

    }

    public void start()
    {
	if (m_platfoot == null)
	    {
		m_platfoot = new Thread(this);
		m_platfoot.start();
	    }
	
    }

    public void stop()
    {
	if (m_platfoot != null)
	    {
		m_platfoot.stop();
		m_platfoot = null;
	    }
	
    }
    
    public void run()
    {
	while (true)
	    {
		int paintAt1;
		try
		    {
			if (gamePaused == false) move();
			paintAt1 = animationCount;
			repaint();
			Thread.sleep(animationSpeed);
			while (paintAt1 > paintAt) {
			    showStatus("Frame skipped at "+animationCount);
			    if (gamePaused == false) move();
			    Thread.sleep(animationSpeed);
			}
		    }
		catch (InterruptedException e)
		    {
			stop();
		    }
	    }
    }

    public int checkMaze(double x, double y, double dx, double dy, int sx, int sy)
    {
	int i = -999999;
	for (int cx = 0; cx < 2; cx++)
	    for (int cy = 0; cy < 2; cy++)
		{
		    int j = pg[(int)Math.round(x+dx+cx*(sx-1))/blockSize][(int)Math.round(y+dy+cy*(sy-1))/blockSize];
		    if (i < j) i = j;
		}
	
	return i;
    }

    
    public void newMonster(int x1, int y1, int k)
    {
	int i = 0;
	for (; (i < noSprites) && (kind[i]!=-1); i++) ;
	if (i < noSprites)
	    {
		x[i] = x1;
		y[i] = y1;
		dx[i]=Math.random()-0.5;
		dy[i]=Math.random()-0.5;

		switch (k) {
		case 0:
		    {
			status[i]=70;
			kind[i]=3;
		    }
		    break;

		case 1:
		    {
			status[i]=180;
			kind[i]=4;
		    }
		    break;

		case 2:
		    {
			status[i]=200;
			kind[i]=5;
		    }
		    break;

		case 3:
		    {
			status[i]=1010;
			kind[i]=6;
			
			double afstand = Math.sqrt((x[0]-x[i])*(x[0]-x[i]) + (y[0]-y[i])*(y[0]-y[i]));
			double speed = Math.random() * 5+5;
			
			if (afstand != 0) {
			    dx[i] = (x[0]-x[i]) / afstand*speed;
			    dy[i] = (y[0]-y[i]) / afstand*speed;
			}
		    }
		    break;

		case 4:
		    {
			status[i]=0;
			kind[i]=7;
		    }
		    break;

		}
	    }
    }

    public void newKugle()
    {
	int i =0;
	for (; (i < noSprites) && (kind[i]!=-1); i++) ;
	if ((i < noSprites) && (noShots < maxShots)) {
	    kind[i] = 2;
	    x[i] = x[0];
	    y[i] = y[0];
	    if (direction > 0) {
		x[i] += 28;
		dx[i] = Math.max(4,Math.min(32,dx[0] / 4 + 24));
	    } else {
		x[i] -= 12;
		dx[i] = Math.min(-4,Math.max(-32,dx[0] / 4 -24));
	    }

	    dy[i]=dy[0] / 4;
	    status[i] = bRange;
	    noShots++;
	}
    }

    public void newPuf()
    {
	int i =0;
	for (; (i < noSprites) && (kind[i]!=-1); i++) ;
	if (i < noSprites)
	    {
		kind[i]=1;
		status[i] = 3;
	        x[i]=x[0]+8;
		y[i]=y[0]+30;
	    }
    }

    public void moveSprites()
    {
	for (int i = 0; i < noSprites; i++) {
	    switch (kind[i]) {
	    case 0: // player

		// if player is dying - do nothing after a while

		if ((gameStatus == 2) && (animationCount - gameOverAt > 20))
		    break;

		// set view

		if (viewx-x[i] > -156) viewx=(int)Math.round(x[i]-156);
		if (viewx-x[i] < -356) viewx=(int)Math.round(x[i]-356);
		if (viewy-y[i] > -140) viewy=(int)Math.round(y[i]-140);
		if (viewy-y[i] < -340) viewy=(int)Math.round(y[i]-340);
		
		if (viewx < 0) viewx = 0;
		if (viewy < 0) viewy = 0;
		
		if (viewx > pg.length*blockSize - screenWidth) viewx=pg.length*blockSize - 512;
		if (viewy > pg[0].length*blockSize - screenHeight) viewy=pg[0].length*blockSize - 480;

		// move player

		if (gameStatus == 1) {
		    if (purge==1) 
			{
			    dy[i] -= 3;
			    newPuf(); 
			}
		    
		    if (purge==-1) 
			dy[i] += 1;
		}
		
		if (direction==-2) dx[i] -= 4;
		if (direction==2) dx[i] += 4;

		// do "physics"

		dy[0]+=downwardGravity;

		int checkM = checkMaze(x[0],y[0],dx[0],dy[0],32,32);
		if (checkM > 0)
		    {
			boolean cdx = false, cdy = false;
			int i1 = -999999, xe=0, ye=0, sx = 32, sy = 32;
			for (int cx = 0; cx < 2; cx++)
			    for (int cy = 0; cy < 2; cy++)
				{
				    int j = pg[(int)Math.round(x[0]+dx[0]+cx*(sx-1))/blockSize][(int)Math.round(y[0]+dy[0]+cy*(sy-1))/blockSize];
				    if (i1 < j) { 
					i1 = j; xe = (int)Math.round(x[0]+dx[0]+cx*(sx-1))/blockSize; 
					ye = (int)Math.round(y[0]+dy[0]+cy*(sy-1))/blockSize; 
				    }
				}
	
			if (checkMaze(x[0],y[0],-dx[0],dy[0],32,32) <= 0) {
			    dx[0] = -(wallElasticity*dx[0]);
			    cdx = true;
			    
			} else
			    if (checkMaze(x[0],y[0],dx[0],-dy[0],32,32) <= 0) {
				dy[0] = -(wallElasticity*dy[0])+0.25;
				cdy = true;
				
			    } else
				if (checkMaze(x[0],y[0],-dx[0],-dy[0],32,32) <= 0) { 
				    dx[0] = -dx[0]; dy[0]=-dy[0]; 
				    cdy = true; cdx = true;
				}
			
			if ((checkM & 4096) > 0)
			    { 
				dy[0] = 0.5*dy[0];

			    } 
			
			if ((checkM & 8192) > 0)
			    { 
				if (pg[xe][ye] == 8192) {
				    pg[xe][ye] = 8194;
				    addLP(xe,ye);
				}
				dy[0] = 2.5*dy[0];
				
			    } 
			
			if ((checkM & 256) > 0)
			    { 
				dx[0] = 0.5*dx[0];
				if (cdy == false) dy[0] = dy[0] -8;
			    } 
			
			if ((checkM & 512) > 0)
			    {
				dx[0] = dx[0] * 0.5;
				if (cdy == false) dy[0] = dy[0] + 8;
			    }
			
			if ((checkM & 1024) > 0)
			    { 
				dy[0] = 0.5*dy[0];
				if (cdx == false) dx[0] = dx[0] -8;
			    } 
			
			if ((checkM & 2048) > 0)
			    {
				dy[0] = dy[0] * 0.5;
				if (cdx == false) dx[0] = dx[0] + 8;
			    }
			
			if (dx[0] > 30) dx[0] = 30; 
			if (dx[0] < -30) dx[0] = -30;
			if (dy[0] > 30) dy[0] = 30; 
			if (dy[0] < -30) dy[0] = -30;
			
			
		    }
	
		int s1 = status[0];
		if (status[0] > 0) status[0] = 0;
		if (status[0] < 0) { status[0] ++; s1 = 0; }
		
		switch (pg[(int)Math.round(x[0]+dx[0]+16)/blockSize][(int)Math.round(y[0]+dy[0]+16)/blockSize]) {
		case -1:
		    {
			if (gameStatus == 1) {
			    score+=10;
			    pg[(int)Math.round(x[0]+dx[0]+16)/blockSize][(int)Math.round(y[0]+dy[0]+16)/blockSize] = -2;
			    addLP((int)Math.round(x[0]+dx[0]+16)/blockSize,(int)Math.round(y[0]+dy[0]+16)/blockSize);
			}
		    }
		    break;

		case -531:
		    {
			dx[0] = 1.2*dx[0];
			dy[0] = 1.1*dy[0];

			if (dx[0] > 30) dx[0] = 30; 
			if (dx[0] < -30) dx[0] = -30;
			if (dy[0] > 30) dy[0] = 30; 
			if (dy[0] < -30) dy[0] = -30;
		    }
		    break;
		
		case -71:
		    {
			if (gameStatus == 1) {
			    score+=10;
			    pg[(int)Math.round(x[0]+dx[0]+16)/blockSize][(int)Math.round(y[0]+dy[0]+16)/blockSize] = -72;
			    addLP((int)Math.round(x[0]+dx[0]+16)/blockSize,(int)Math.round(y[0]+dy[0]+16)/blockSize);
			}
		    }
		    break;

		case -11:
		    {
			pg[(int)Math.round(x[0]+dx[0]+16)/blockSize][(int)Math.round(y[0]+dy[0]+16)/blockSize] = -12;
			addLP((int)Math.round(x[0]+dx[0]+16)/blockSize,(int)Math.round(y[0]+dy[0]+16)/blockSize);

			if (gameStatus == 1)
			    doPlayerDie();
			
		    }
		    break;

		case -21:
		    {
			if (gameStatus == 1) {
			    dx[0]=portalSop*dx[0];
			    dy[0]=portalSop*dy[0];

			    status[0]=s1+1;
			    if (status[0] > 24) doNextRound();

			}
			
		    }
		    break;

	
		case -22:
		    {
			if (gameStatus == 1) {
			    dx[0]=portalSop*dx[0];
			    dy[0]=portalSop*dy[0];

			    status[0]=s1+1;
			    if (status[0] > 24) {
				x[0] = portaldx;
				y[0] = portaldy;
				status[0]=-24;
				savePosition();
			    }
			}			
		    }
		    break;

		case -31:
		    {
			if (gameStatus == 1) {
			    lives ++;
			    pg[(int)Math.round(x[0]+dx[0]+16)/blockSize][(int)Math.round(y[0]+dy[0]+16)/blockSize]=-32;
			    addLP((int)Math.round(x[0]+dx[0]+16)/blockSize,(int)Math.round(y[0]+dy[0]+16)/blockSize);
			}			
		    }
		    break;
	
		case -501:
		    {
			pg[(int)Math.round(x[0]+dx[0]+16)/blockSize][(int)Math.round(y[0]+dy[0]+16)/blockSize] = -502;
			addLP((int)Math.round(x[0]+dx[0]+16)/blockSize,(int)Math.round(y[0]+dy[0]+16)/blockSize);

			if (gameStatus == 1)
			    doPlayerDie();
			
		    }
		    break;
	
		case -541:
		    {
			pg[(int)Math.round(x[0]+dx[0]+16)/blockSize][(int)Math.round(y[0]+dy[0]+16)/blockSize] = -542;
			addLP((int)Math.round(x[0]+dx[0]+16)/blockSize,(int)Math.round(y[0]+dy[0]+16)/blockSize);

			if (gameStatus == 1)
			    doPlayerDie();
			
		    }
		    break;
	
		case -511:
		    {
			pg[(int)Math.round(x[0]+dx[0]+16)/blockSize][(int)Math.round(y[0]+dy[0]+16)/blockSize] = -512;
			addLP((int)Math.round(x[0]+dx[0]+16)/blockSize,(int)Math.round(y[0]+dy[0]+16)/blockSize);

			if (gameStatus == 1)
			    doPlayerDie();
			
		    }
		    break;
		}
	
		dx[0]=airResistance*dx[0];
		if (dx[0] > 35) dx[0] = 35; 
		if (dx[0] < -35) dx[0] = -35;
		if (dy[0] > 35) dy[0] = 35; 
		if (dy[0] < -35) dy[0] = -35;
		
		if ((dy[0] > -0.15) && (dy[0] < 0.15)) dy[0] = 0;
		if ((dx[0] > -0.15) && (dx[0] < 0.15)) dx[0] = 0;
		
		x[0]+=dx[0];
		y[0]+=dy[0];

		break;

	    case 1: // puf
		status[i]--;
		if (status[i] == -1) kind[i] = -1;
		break;

	    case 2: // boomerang
		xr[i] = (x[0]*(bRange-status[i])/bRange)+(status[i]*x[i]/bRange);
		yr[i] = (y[0]*(bRange-status[i])/bRange)+(status[i]*y[i]/bRange);

		int mx = (int)Math.round(xr[i]+24)/blockSize, my = (int)Math.round(yr[i]+24)/blockSize;
		
		switch (pg[mx][my]) {
		case -11:
		    pg[mx][my] = - 12;
		    addLP(mx, my);
		    break;

		case -501:
		    pg[mx][my] = -502;
		    addLP(mx,my);
		    score += 100;
		    break;
		    
		case -541:
		    pg[mx][my] = -542;
		    addLP(mx, my);
		    score += 750;
		    break;

		case -511:
		    pg[mx][my] = -512;
		    addLP(mx, my);
		    score += 500;
		    break;

		}
		
		// collision detect /w other sprites

		for (int j = 1; j < noSprites; j++)
		    if ((xr[i] <= x[j]+32) && (xr[i]+16 > x[j]) &&
			(yr[i] <= y[j]+32) && (yr[i]+16 >= y[j]) && status[j] >= 0) {
			if (kind[j] == 3) {
			    status[j] = -1;
			    score += 100;
			}
			if (kind[j] == 4) {
			    status[j] = -1;
			    score += 500;
			}
			if (kind[j] == 5) {
			    status[j] = -1;
			    score += 750;
			}
	    }

		status[i]--;
		if (status[i] == -1) {
		    noShots --;
		    kind[i] = -1;
		}
		
		x[i]+=dx[i];
		y[i]+=dy[i];

		break;

	    case 4:
	    case 3: // monster
		status[i]--;
		if (status[i] == -5) kind[i] = -1;
		x[i] += dx[i];
		y[i] += dy[i];

		dx[i] = 0.97*dx[i];
		dy[i] = 0.96*dy[i];

		double a = 0.4;
		if (kind[i]==4) a = 0.7;

		if (x[i] < x[0]) dx[i]+=a; else dx[i]-=a;
		if (y[i] < y[0]) dy[i]+=a; else dy[i]-=a;

		// collision detect /w player
		if ((x[i] <= x[0]+32) && (x[i]+32 > x[0]) &&
		    (y[i] <= y[0]+32) && (y[i]+32 >= y[0]) && (status[i] >= 0) && (gameStatus == 1)) 
		    doPlayerDie();
		
		break;

	    case 5: // wasp
		status[i]--;
		if (status[i] == 10000) status[i] = 0;
		if (status[i] == -5) kind[i] = -1;

		if ((status[i] > 0) && (status[i] < 10000) && (Math.random() < 0.05))
		    status[i] += 10000;    

		if ((status[i] > 10000) && (Math.random() < 0.05))
		    status[i] -= 10000;    

		x[i] += dx[i];
		y[i] += dy[i];

		dx[i] = 0.9*dx[i];
		dy[i] = 0.9*dy[i];

		if (status[i] < 10000) {
		    if (x[i] < x[0]) dx[i]+=1.5; else dx[i]-=1.5;
		    if (y[i] < y[0]) dy[i]+=1.5; else dy[i]-=1.5;

		}

		// collision detect /w player
		if ((x[i] <= x[0]+32) && (x[i]+32 > x[0]) &&
		    (y[i] <= y[0]+32) && (y[i]+32 >= y[0]) && (status[i] >= 0) && (gameStatus == 1))
		    doPlayerDie();
		
		break;

	    case 6: // bullet
		status[i]--;
		if (status[i] == 1000) status[i] = 50;
		if (status[i] < 1000) {
		    if (status[i] == 0) kind[i] = -1;

		    x[i] += dx[i];
		    y[i] += dy[i];

		    // collision detect /w player
		    if ((x[i] <= x[0]+32) && (x[i]+8 > x[0]) &&
			(y[i] <= y[0]+32) && (y[i]+8 >= y[0]) && (status[i] >= 0) && (gameStatus == 1)) 
			doPlayerDie();

		}
		
		break;

	    case 7: // goose

		switch (status[i] & 48) {
		case 0:
		    dx[i]=0; dy[i]=-1;
		    break;
		case 32:
		    dx[i]=0; dy[i]=1;
		    break;
		case 48:
		    dx[i]=-1; dy[i]=0;
		    break;
		case 16:
		    dx[i]=1; dy[i]=0;
		    break;

		}
		status[i]=(status[i]+1)%16+status[i]&48;
		if (Math.random() < 0.1) status[i]=(status[i]+16)%64;

		y[i]+=dy[i]*(1+status[i]%4);
		x[i]+=dx[i]*(1+status[i]%4);		    

		break;

	    }
	}
    }

    public void drawSprites ()
    {
	for (int i = 0; i < noSprites; i++) {
	    switch (kind[i]) {
	    case 0: // player
		if (gameStatus == 2) {
		    int k;
		    if (direction > 0)
			k = 78; else k = 84;

		    switch (animationCount - gameOverAt) {
		    case 0: case 1: case 2: case 3:
			break;
		    case 4: case 5:
			k += 1;
			break;
		    case 6: 
			break;
		    case 7:
			k += 1;
			break;
		    case 8: 
			k += 2;
			break;
		    case 9: 
			k += 3;
			break;
		    case 10: case 11:
			k += 4;
			break;
		    case 12:
			k += 5;
			break;
		    case 13: case 14: case 15:
			k += 4;
			break;

		    case 16: case 17: 
			k += 5;
			break;

		    default:
			k = -1;
			break;
		    }

		    if (k > 0)
			offScreen.drawImage(image[k],(int)Math.round(x[0]-viewx),(int)Math.round(y[0]-viewy),this); 

		    break;
		}

		int s2 = status[0];

		if (s2 < 0) s2 = -s2;

		if ((s2 < 18) || (s2 % 2 == 0)) {

		    if (purge == -1) {
			if (direction > 0)
			    offScreen.drawImage(image[45],(int)Math.round(x[0]-viewx),(int)Math.round(y[0]-viewy),this); else
				offScreen.drawImage(image[46],(int)Math.round(x[0]-viewx),(int)Math.round(y[0]-viewy),this);
		    } else {
			if (direction > 0)
			    offScreen.drawImage(image[0],(int)Math.round(x[0]-viewx),(int)Math.round(y[0]-viewy),this); else
				offScreen.drawImage(image[1],(int)Math.round(x[0]-viewx),(int)Math.round(y[0]-viewy),this);
		    } 
		}

		// Teleportation

		if (s2 > 0) {
		    int s1 = 24-s2;
		    if (s1 == 23) s1 = 50;
		    if (s1 == 22) s1 = 38;
		    if (s1 == 21) s1 = 30;
		    if (s1 == 20) s1 = 24;
		    
		    for (int o = 0; o < 8; o++)
			offScreen.drawImage(image[111+o % 4],(int)Math.round(x[0]-viewx+13+(3*Math.sin(s1*1.1+o)*s1)),
					    (int)Math.round(y[0]-viewy+13+(3*Math.sin(2+0.9*s1+o)*s1)),this);

		}
		break;

	    case 1: // puf
		offScreen.drawImage(image[6-status[i]],(int)Math.round(x[i])-viewx,(int)Math.round(y[i])-viewy,this);
		break;

	    case 2: // boomerang
		offScreen.drawImage(image[51+status[i]%4],(int)Math.round(xr[i])-viewx,
				    (int)Math.round(yr[i])-viewy,this);
		break;

	    case 3: // monster
		if (status[i] >= 0)
		    offScreen.drawImage(image[55+(status[i] % 4)],(int)Math.round(x[i])-viewx,
					(int)Math.round(y[i])-viewy,this); else
		    offScreen.drawImage(image[72-(status[i])],(int)Math.round(x[i])-viewx,
					(int)Math.round(y[i])-viewy,this); 

		
		break;

	    case 4: // monster
		if (status[i] >= 0)
		    offScreen.drawImage(image[90+(status[i] % 4)],(int)Math.round(x[i])-viewx,
					(int)Math.round(y[i])-viewy,this); else
		    offScreen.drawImage(image[93-(status[i])],(int)Math.round(x[i])-viewx,
					(int)Math.round(y[i])-viewy,this); 

		
		break;
		
	    case 5: // wasp
		if (status[i] >= 0)
		    {
			if (dx[i] < 0) 
			    offScreen.drawImage(image[122 /*122 % 3 */ +(status[i] % 3)],(int)Math.round(x[i])-viewx,
						(int)Math.round(y[i])-viewy,this); else
						    offScreen.drawImage(image[125+(status[i] % 3)],(int)Math.round(x[i])-viewx,
									(int)Math.round(y[i])-viewy,this);

		    } else
			offScreen.drawImage(image[127-(status[i])],(int)Math.round(x[i])-viewx,
					(int)Math.round(y[i])-viewy,this); 

		
		break;
		
	    case 6: // bullet
		if (status[i] < 1000)
		    {
			offScreen.drawImage(image[138+(status[i] % 4)],(int)Math.round(x[i]-viewx),(int)Math.round(y[i]-viewy),this); 

		    } else {
			offScreen.drawImage(image[111+(status[i] % 4)],(int)Math.round(x[i]-viewx+5+Math.sin(status[i]*0.5)*4),
					    (int)Math.round(y[i]-viewy+5+Math.cos(status[i]*0.7)*4),this); 
			offScreen.drawImage(image[111+((status[i]+2) % 4)],(int)Math.round(x[i]-viewx+5+Math.sin(status[i]*0.8)*4),
					    (int)Math.round(y[i]-viewy+5+Math.cos(status[i]*0.6)*4),this); 
		    }
		
		break;
		
	    case 7: // goose
		offScreen.drawImage(image[147 + 4*(status[i] % 2)],(int)Math.round(x[i])-viewx,
				    (int)Math.round(y[i])-viewy,this); 

		break;
		
	    }
	}
    }

    public void addLP (int x, int y)
    {
	int i = x + y * 64;
	lp[animationCount % 2][0] ++;
	lp[animationCount % 2][lp[animationCount % 2][0]] = i;

    }

    public void moveMaze ()
    {
	for(int i = 1; i <= lp[(animationCount + 1) % 2][0]; i++) {
	    int x = lp[(animationCount + 1) % 2][i] % 64;
	    int y = lp[(animationCount + 1) % 2][i] / 64;

	    if ((pg[x][y] == 8193)) { pg[x][y]=8192; }
	    if ((pg[x][y] == 8194)) { pg[x][y]=8193; addLP(x,y); }

	    if (pg[x][y] == 16384) {
		if ((y < 64-1) && (pg[x][y+1] == 0)) { pg[x][y]++; addLP(x,y); }
	    }

	    if (pg[x][y] == 16388) { 
    		pg[x][y] = 0; pg[x][y+1] = 16384; addLP(x,y+1);
		if ((checkMaze(this.x[0],this.y[0],0,0,32,32) >= 16384) && (gameStatus == 1)) doPlayerDie();

		if ((y > 0) && (pg[x][y-1] == 16384)) { pg[x][y-1] ++; addLP(x,y-1); }
	    }

	    if ((pg[x][y] > 16384) &&(pg[x][y] < 16388)) { pg[x][y]++; addLP(x,y); }

	    if ((pg[x][y] < 70) && (pg[x][y] > -80)) {
		if (pg[x][y] <-71) { pg[x][y]--; addLP(x,y); }
		if (pg[x][y] <-74) { pg[x][y]=0; savePosition(); }

	    }
		
	    if ((pg[x][y] < 0) && (pg[x][y] > -10)) {
		if (pg[x][y] <-1) { pg[x][y]--; addLP(x,y); }
		if (pg[x][y] <-4) pg[x][y]=0; 

	    }
		
	    if ((pg[x][y] < 30) && (pg[x][y] > -50)) {
		if (pg[x][y] <-31) { pg[x][y]--; addLP(x,y); }
		if (pg[x][y] <-49) pg[x][y]=0; 

	    }
		
	    if ((pg[x][y] < 500) && (pg[x][y] > -510)) {
		if (pg[x][y] <-501) { pg[x][y]--; addLP(x,y); }
		if (pg[x][y] <-504) pg[x][y]=0; 

	    }
		
	    if ((pg[x][y] < 540) && (pg[x][y] > -550)) {
		if (pg[x][y] <-541) { pg[x][y]--; addLP(x,y); }
		if (pg[x][y] <-544) pg[x][y]=0; 

	    }
		
	    if ((pg[x][y] < 510) && (pg[x][y] > -520)) {
		if (pg[x][y] <-511) { pg[x][y]--; addLP(x,y); }
		if (pg[x][y] <-514) pg[x][y]=0; 

	    }
		
	    if ((pg[x][y] < -10) && (pg[x][y] > -20)) {
		if (pg[x][y] <-11) { pg[x][y]--; addLP(x,y); }
		if (pg[x][y] <-14) { pg[x][y]=0; addLP(x,y); }
		if (pg[x][y] == 0) {
		    if ((x > 0) && (pg[x-1][y] == -11)) { pg[x-1][y]=-12; addLP(x-1,y); }
		    if ((x < 64-1) && (pg[x+1][y] == -11)) { pg[x+1][y]=-12; addLP(x+1,y); }
		    if ((y > 0) && (pg[x][y-1] == -11)) { pg[x][y-1]=-12; addLP(x,y-1); }
		    if ((y < 64-1) && (pg[x][y+1] == -11)) { pg[x][y+1]=-12; addLP(x,y+1); }
		    if ((y > 0) && (pg[x][y-1] == 16384)) { pg[x][y-1]=16385; addLP(x,y-1); }
		}
	    }
	}
	lp[(animationCount + 1) % 2][0] = 0;
    }

    public void drawMaze ()
    {
	for (int i=viewx/blockSize; i<Math.min(pg.length,screenWidth/blockSize+2+viewx/blockSize); i++) {
	    int gooseAt = -1;
	    for (int j=viewy/blockSize; j<Math.min(pg[i].length,screenHeight/blockSize+2+viewy/blockSize); j++) {

		if (pg[i][j] > 0) {
		    if ((pg[i][j] & 4096) > 0) {
			offScreen.drawImage(image[115],i*blockSize-viewx,j*blockSize-viewy,this); 

		    } else
		    if ((pg[i][j] & 16384) > 0) {
			offScreen.drawImage(image[142+pg[i][j]-16384],i*blockSize-viewx,j*blockSize-viewy,this);

		    } else
		    if ((pg[i][j] & 8192) > 0) {
			if (pg[i][j] == 8193)
			    offScreen.drawImage(image[117],i*blockSize-viewx,j*blockSize-viewy,this); else
			    offScreen.drawImage(image[116],i*blockSize-viewx,j*blockSize-viewy,this); 

		    } else
			    offScreen.drawImage(image[(pg[i][j] & 255) +12],i*blockSize-viewx,j*blockSize-viewy,this);
		    
		    if ((pg[i][j] & 256) > 0)
			offScreen.drawImage(image[12+29+(i+j+animationCount)%4],
					    i*blockSize-viewx+24,j*blockSize-viewy+24+(32-((animationCount*6) % 64)),this); 

		    if ((pg[i][j] & 512) > 0)
			offScreen.drawImage(image[8+29+(i+j+animationCount)%4],
					    i*blockSize-viewx+24,j*blockSize-viewy+24+((6*animationCount) % 64-32),this); 

		    if ((pg[i][j] & 1024) > 0)
			offScreen.drawImage(image[4+29+(i+j+animationCount)%4],
					    i*blockSize-viewx+24+(32-((animationCount*6) % 64)),j*blockSize-viewy+24,this); 

		    if ((pg[i][j] & 2048) > 0)
			offScreen.drawImage(image[29+(i+j+animationCount)%4],
					    i*blockSize-viewx+24+((6*animationCount) % 64-32),j*blockSize-viewy+24,this); 

		}

		
		if ((pg[i][j] < 0) && (pg[i][j] > -10)) {
		    offScreen.drawImage(image[8-pg[i][j]],i*blockSize-viewx+16,j*blockSize-viewy+16,this);
		}
		
		if ((pg[i][j] < -70) && (pg[i][j] > -80)) {
		    offScreen.drawImage(image[-70+8-pg[i][j]],i*blockSize-viewx+16,j*blockSize-viewy+16,this);
		}
		
		if ((pg[i][j] < -10) && (pg[i][j] > -20)) {
		    offScreen.drawImage(image[36-pg[i][j]],i*blockSize-viewx+16,j*blockSize-viewy+16,this);
		}
		
		if (pg[i][j] == -501) {
		    offScreen.drawImage(image[55+animationCount % 4],16+i*blockSize-viewx,16+j*blockSize-viewy,this);
		    if (Math.random() < .02) {
			newMonster(i*blockSize+16,j*blockSize+16,0);
			pg[i][j] = 0;
		    }
		}

		if (pg[i][j] == -551) {
		    offScreen.drawImage(image[134+(animationCount / 3) % 4],i*blockSize-viewx,j*blockSize-viewy,this);
		    if (Math.random() < .025) newMonster(i*blockSize+24,j*blockSize+30,3);
		    
		}
		
		if ((pg[i][j] <= -701) && (pg[i][j] > -800)) {
		    pg[i][j] = 0;
		    newMonster(i*blockSize+16,j*blockSize+16,4);
		    
		}
		
		if (pg[i][j] == -541) {
		    offScreen.drawImage(image[122+animationCount % 3],16+i*blockSize-viewx,16+j*blockSize-viewy,this);
		    if (Math.random() < .05) {
			newMonster(i*blockSize+16,j*blockSize+16,2);
			pg[i][j] = 0;
			}
		}
		
		if (pg[i][j] == -511) {
		    offScreen.drawImage(image[90+animationCount % 4],16+i*blockSize-viewx,16+j*blockSize-viewy,this);
		    if (Math.random() < .04) {
			newMonster(i*blockSize+16,j*blockSize+16,1);
			pg[i][j] = 0;
		    }
		}
		
		if ((pg[i][j] < -501) && (pg[i][j] > -510)) {
		    offScreen.drawImage(image[-429-pg[i][j]],16+i*blockSize-viewx,16+j*blockSize-viewy,this);
		}
		
		if ((pg[i][j] < -541) && (pg[i][j] > -550)) {
		    offScreen.drawImage(image[-1+128-541-pg[i][j]],16+i*blockSize-viewx,16+j*blockSize-viewy,this);
		}
		
		if ((pg[i][j] < -511) && (pg[i][j] > -520)) {
		    offScreen.drawImage(image[-418-pg[i][j]],16+i*blockSize-viewx,16+j*blockSize-viewy,this);
		}
		
		if (pg[i][j] == -21) {
		    offScreen.drawImage(image[105+(animationCount) % 4],16+i*blockSize-viewx,16+j*blockSize-viewy,this);
		    // offScreen.drawImage(image[103+(animationCount / 8) % 2],19+i*blockSize-viewx,26+j*blockSize-viewy,this);

		}

		if (pg[i][j] == -22) {
		    offScreen.drawImage(image[105+(animationCount) % 4],16+i*blockSize-viewx,16+j*blockSize-viewy,this);
		    // offScreen.drawImage(image[103+(animationCount / 8) % 2],19+i*blockSize-viewx,26+j*blockSize-viewy,this);

		}

		if ((pg[i][j] < -30) && (pg[i][j] > -50)) {
		    double f = (-pg[i][j]-26)/5.1;
		    offScreen.drawImage(image[109],24+(int)Math.round(Math.sin(animationCount / 4.1)*19*f)+i*blockSize-viewx,
					24+(int)Math.round(Math.cos(animationCount / 5.1)*19*f)+j*blockSize-viewy,this);

		    offScreen.drawImage(image[110],24+(int)Math.round(Math.sin(animationCount / 5.9)*22*f)+i*blockSize-viewx,
					24+(int)Math.round(Math.cos(animationCount / 6.5)*24*f)+j*blockSize-viewy,this);

		    offScreen.drawImage(image[109],24+(int)Math.round(Math.sin(animationCount / 3.1)*19*f)+i*blockSize-viewx,
					24+(int)Math.round(Math.cos(animationCount / 4.1)*19*f)+j*blockSize-viewy,this);

		    offScreen.drawImage(image[110],24+(int)Math.round(Math.sin(animationCount / 4.9)*22*f)+i*blockSize-viewx,
					24+(int)Math.round(Math.cos(animationCount / 4.5)*24*f)+j*blockSize-viewy,this);


		}


		if (pg[i][j] == -531) {
		    int magic = (i+j+animationCount) % 256;
		    if (magic < 0) magic+= 256;
		    
		    offScreen.drawImage(image[118+magic % 4],i*blockSize-viewx+
					magicY[magic],j*blockSize-viewy+magicX[magic],this); 
		    
		    magic = (magic+128)%256;

		    if (fastGFX == false) 
			offScreen.drawImage(image[118+magic % 4],i*blockSize-viewx+
					    magicY[magic],j*blockSize-viewy+magicX[magic],this); 

		}

		if ((pg[i][j] <= -610) && (pg[i][j] > -620)) {
		    doSign(i*blockSize-viewx,j*blockSize-viewy,-pg[i][j]-610);
		}

		if (pg[i][j] == -603) {
		    doSign((i-2)*blockSize-viewx,j*blockSize-viewy,-pg[i-2][j]-610);
		
		}
	    }
	}
    }

    void doSign (int x, int y, int i) {
	offScreen.setColor(Color.white);
	offScreen.drawImage(image[133],x,y,this);
	offScreen.drawString(comments[(round-1)%noRounds][i*3],x+12,y+20);
	if (comments[(round-1)%noRounds][i*3+2].equals(""))
	    offScreen.drawString(comments[(round-1)%noRounds][i*3+1],x+12,y+42); else
		{
		    offScreen.drawString(comments[(round-1)%noRounds][i*3+1],x+12,y+36);
		    offScreen.drawString(comments[(round-1)%noRounds][i*3+2],x+12,y+52);
		}

    }

    void setupFont() {

	// find the helvetica font which is nearest to 16 in height

	int diff = 200, no = 0;
	for (int i = 8; i < 30; i++) {    
	    offScreen.setFont(new Font("Helvetica",Font.PLAIN,i));
	    int diff2 = 16-offScreen.getFontMetrics().getHeight();
	    if (diff2 < 0) diff2 = - diff2;
	    if (diff2 < diff) { no = i; diff = diff2; }

	}

	offScreen.setFont(new Font("Helvetica",Font.PLAIN,no));

	gtextw = new int[6];

	for (int i = 0; i < 6; i++)
	    gtextw[i] = offScreen.getFontMetrics().stringWidth(gtext[i]);

	//	int etWidth[] = new int [6];

	for (int i = 0; i < 6; i++) {
	    etWidth[i] = offScreen.getFontMetrics().stringWidth(endText[i]);
	    System.out.println(""+etWidth[i]);

	}
    }

    synchronized public void move()
    {
	animationCount++;

	switch (gameStatus) {
	case 0:
	    break;

	case 1:
	    if (skyd == 1) {
		newKugle();
		skyd = 0;
	    }
	
	case 2:
	    moveMaze();
	    moveSprites();
	    break;

	case 3:
	    break;

	case 9: // game completed
	    break;

	}


    }

    final void doCirclingBackground() {

	offScreen.setColor(new Color(0,49,74));
	offScreen.fillRect(0,0,512,480); 
	
	for (int j = 0; j < 6; j++) {
	    for (int i = 0; i < 5; i++) {
		double a = 4*Math.sin(j*Math.PI+(animationCount) / 40.0);
		for (int k = 0; k < 3; k ++) {
		    double x1 = 256+(14+(59*j-k*1+2*(animationCount)) % 350)*Math.cos(i/5.0*2.0*Math.PI+a);
		    double y1 = 240+(14+(59*j-k*1+2*(animationCount)) % 350)*Math.sin(i/5.0*2.0*Math.PI+a);
		    double x2 = 256+(14+(59*j-k*1+2*(animationCount)) % 350)*Math.cos(i/5.0*2.0*Math.PI+1+a);
		    double y2 = 240+(14+(59*j-k*1+2*(animationCount)) % 350)*Math.sin(i/5.0*2.0*Math.PI+1+a);
		    
		    offScreen.setColor(new Color(14+(int)Math.round((10-k*4)*Math.sin(a)),
						 64+(int)Math.round((22-k*10)*Math.sin(a)),
						 90+(int)Math.round((30-k*14)*Math.sin(a))));
		    
		    offScreen.drawLine((int)Math.round(x1),(int)Math.round(y1),(int)Math.round(x2),(int)Math.round(y2));
		    
		}		    
	    }
	}
    }

    synchronized public void update(Graphics g)
    {
	if (gameStatus == 4) { paintAt = animationCount; return; }
	if (gameStatus == 5) gameStatus = 4;

	switch (gameStatus) {
	case 0:

	    doCirclingBackground();

	    offScreen.drawImage(image[72],0,mrPlatfootLogoY,this);

	    if (startClick == 0) offScreen.drawImage(image[101],93,startClickY,this); else
		offScreen.drawImage(image[102],93,startClickY,this);

	    if (gtexty > -1) {
		int a = 0, b = (animationCount - introStartAt) % 400, c = 0;
		if (b > 200) c = 1;
		for (int i = 0; i < 3; i++) {
		    a = b - 20*i-20-200*c;
		    if ((a > 70) && (i!=2)) a = 120-a;
		    if (a > 50) a = 50;
		    if (a > 0) {
			offScreen.setColor(new Color((a * 207) / 50+48, (a * 200) / 50+55, (a * 176) / 50 +79));
			offScreen.drawString(gtext[i+c*3],256-gtextw[i+c*3]/2,gtexty+gtextwidth*i);
		    }
		}
		
		if (a > 0) {
		    if (linkClick > 0) offScreen.drawLine(256-gtextw[2+c*3]/2,1+gtexty+gtextwidth*2,256+gtextw[2+c*3]/2,1+gtexty+gtextwidth*2);
		    linkAvaible = c;
		} else linkAvaible = -1;
	    }

	    
	    break;

	case 2:
	case 1:
	    if (fastGFX == true) {		
		offScreen.setColor(new Color(53,58,81));
		offScreen.fillRect(0,0,512,480); } else
		    offScreen.drawImage(backgroundImage,-((int)Math.round(viewx/4) % bgBlockSize), 
					-((int)Math.round(viewy/4) % bgBlockSize), this);

	    drawMaze();
	    drawSprites();

	    // if game over ...

	    if (gameStatus == 2) {
		if (lives == 0) {
		    if (gameOverAt + 30 < animationCount) offScreen.drawImage(image[77],169,210,this);
		    if (gameOverAt + 70 < animationCount) doMenu();
		} else
		    if (gameOverAt + 45 < animationCount) { 
			lives--; 
			if (positionSaved == true) loadPosition(); else doRound(); }

	    }
	    
	    break;

	case 3:
	    doCirclingBackground();

	    offScreen.drawImage(map,61,36,this);
	    offScreen.drawImage(image[98],153,400,this);
	    int k1 =(20-animationCount+gameStartAt);
	    if (k1 < 0) k1=0;
	    offScreen.drawImage(image[62+(round / 10) % 10],153+179+k1*k1/2,403+k1,this);
	    int k2 =(30-animationCount+gameStartAt);
	    if (k2 < 0) k2=0;
	    offScreen.drawImage(image[62+round % 10],153+192+k2*k2/2,401-k2,this);

	    if ((animationCount - gameStartAt) > 40) gameStatus = 1;

	    break;

	case 9:
	    drawEnding();
	    break;

	}

	drawScore();

	if (gamePaused == true)
	    offScreen.drawImage(image[132],125,230,this);

	paint(g);


    }

    void drawEnding() {
	int rfc = animationCount - gsca; // relative frame count

	if (redoGameClick == 0) 
	    redoGameClick = -1;

	

	if (rfc < 30) 
	    {
		doCirclingBackground(); 
		offScreen.drawImage(image[153],171,205,null);
	    
		return; 
	    }

	if (rfc < 130) {
	    // make red background and fade first part of text

	    if (rfc > 40) {
		offScreen.setColor(new Color (132, 32, 16));
		offScreen.fillRect(0,0,512,480);

	    } else {
		int a = rfc -30;
		offScreen.setColor(new Color((a * (132-48)) / 10 + 48, (a * (32-55)) / 10 + 55, (a * (16-79)) / 10 + 79));
		offScreen.fillRect(0,0,512,480);
		offScreen.drawImage(image[153],171,205,null);

	    }
	    
	    for (int i = 0; i < 2; i++) {
		int a = rfc - (50+i*20);

		if (a > 50) a = 50;

		if (a > 0) {
		    offScreen.setColor(new Color((a * (255-132)) / 50+132, (a * (255-32)) / 50+32, (a * (255-16)) / 50 +16));
		    offScreen.drawString(endText[i],50,150+i*20);
		}
	    }
	}

	if (rfc > 130) {
	    doCirclingBackground();
	    double zoom = (rfc-130) ;
	    if (zoom > 40) zoom = 40;

	    zoom = zoom / 40d;

	    int mx = (int)Math.round(482 * (1-zoom*zoom) + 256 * (zoom*zoom) );
	    int my = (int)Math.round(307 * (1-zoom*zoom) + 174 * (zoom*zoom) );

	    int w = 18 + (int)Math.round((512-18)*zoom*zoom);
	    int h = 12 + (int)Math.round((480-12)*zoom*zoom);
	    
	    int sx1 = mx - w/2;
	    int sy1 = my - h/2;
	    int sx2 = mx + w/2;
	    int sy2 = my + h/2;

	    int dx1 = 0;
	    int dx2 = 512;
	    int dy1 = 0; // 50 398
	    int dy2 = 480;
	    
	    if (sx2 > 512) {
		double p = sx2/512d;
		dx2=dx1 + (int)Math.round((dx2-dx1)*(1-(p-1)/p));
		sx2 = 512;
		
	    }

	    if (sy2 > 348) {
		double p = sy2/348d;
		dy2=dy1 + (int)Math.round((dy2-dy1)*(1-(p-1)/p));
		sy2 = 348;

	    } 

	    if (sy1 < 0) {
		double p = sy1/348d;
		dy1=dy1 + (int)Math.round((dy2-dy1)*(-p));
		sy1 = 0;

	    }

	    offScreen.drawImage(image[152],dx1,dy1,dx2,dy2,sx1,sy1,sx2,sy2,null);

	}

	if (rfc > 180) {
	    // make red background and fade first part of text

	    /*	    offScreen.setColor(new Color (132, 32, 16));
		    offScreen.fillRect(0,0,512,480);*/
	    
	    for (int i = 0; i < 3; i++) {
		int a = rfc - (200+i*20);

		if (a > 50) a = 50;

		if (a > 0) {
		    offScreen.setColor(new Color((a * 207) / 50+48, (a * 200) / 50+55, (a * 176) / 50 +79));
		    // offScreen.setColor(new Color((a * (255-132)) / 50+132, (a * (255-32)) / 50+32, (a * (255-16)) / 50 +16));
		    offScreen.drawString(endText[i+2],462-etWidth[i+2],110+i*20);
		}
	    }
	}

	if (rfc > 330) {
	    int a = rfc - (330);

	    if (a > 50) a = 50;
	    
	    if (a > 0) {
		if (redoGameClick == -1) redoGameClick = 0;
		offScreen.setColor(new Color((a * 207) / 50+48, (a * 200) / 50+55, (a * 176) / 50 +79));
		// offScreen.setColor(new Color((a * (255-132)) / 50+132, (a * (255-32)) / 50+32, (a * (255-16)) / 50 +16));
		offScreen.drawString(endText[5],462-etWidth[5],412);

		if (redoGameClick > 0)
		    offScreen.drawLine(462-etWidth[5],413,462,413);
		

	    }

	}



	//	offScreen.drawImage(image[152],0,50,512,398,mx,my,mx+w,my+h,null);
    }

    public void drawScore()
    {
	if (score > hiscore) hiscore = score;

	offScreen.drawImage(image[62+score % 10],490,10,this);
	offScreen.drawImage(image[62+(score / 10) % 10],478,10,this);
	offScreen.drawImage(image[62+(score / 100) % 10],466,10,this);
	offScreen.drawImage(image[62+(score / 1000) % 10],454,10,this);
	offScreen.drawImage(image[62+(score / 10000) % 10],442,10,this);

	offScreen.drawImage(image[62+hiscore % 10],68+8,10,this);
	offScreen.drawImage(image[62+(hiscore / 10) % 10],56+8,10,this);
	offScreen.drawImage(image[62+(hiscore / 100) % 10],44+8,10,this);
	offScreen.drawImage(image[62+(hiscore / 1000) % 10],32+8,10,this);
	offScreen.drawImage(image[62+(hiscore / 10000) % 10],20+8,10,this);

	offScreen.drawImage(image[99],8,15,this);

	if (lives > 0) offScreen.drawImage(image[100],410,9,this);
	if (lives > 1) offScreen.drawImage(image[100],410-32,9,this);
	if (lives > 2) offScreen.drawImage(image[100],410-64,9,this);
	if (lives > 3) offScreen.drawImage(image[100],410-96,9,this);

    }

    public void doPlayerDie()
    {
	gameOverAt = animationCount;
	gameStatus = 2;

    }

    void checkCode() {
	int j = -1;
	for (int i = 0; i < noRounds; i++)
	    if (enteredCode.equals(gameCodes[i])) j=i+1;

	if (j > 0) doGame(j);

    }

    public boolean keyDown(java.awt.Event e,int key)
    {
	// editor specific

	/* if ((key == 1009) && (gameStatus == 4)) { 
	    for (int i=0; i < 64; i++)
		{
		    for (int j=0; j < 64; j++)

			{
			    bane[i]=bane[i].substring(0,j) + bane[j].charAt(i) + bane[i].substring(j+1,64);
			}

		}

	    gameStatus=5;

	}

		if ((key == 1011) && (gameStatus == 4)) { editor.save(); }
		if ((key == 1012) && (gameStatus == 0)) { gameStatus = 5; }*/
	
	if ((key >= 65) && (key <= 90)) { 
	    enteredCode=enteredCode+(char)key;
	    checkCode();
	}
	if ((key >= 97) && (key <= 122)) { 
	    enteredCode=enteredCode+(char)(key-32);
	    checkCode();
	}

	switch (key) {
	case 1013: case 1017:
	    enteredCode = "";
	    break;

	case 1015:
	    if (gameStatus != 0) gamePaused = !gamePaused;
	    break;

	case 1016:
	    fastGFX = !fastGFX;
	    break;

	case 1018:
	    if (gamePaused == false) doMenu();
	    break;

	case 1006:		
  	    direction=-2;
	    break;
	
	case 1007:
	    direction=2;
	    break;

	case 1004:
	    purge=1;
	    break;

	case 1005:
  	    purge=-1;
	    break;

	case 32:
  	    skyd=1;
	    break;
	}
	return false;
    }
    
    public boolean keyUp(java.awt.Event e,int key)
    {
	switch (key) {
	case 1006:
	    direction=-1;
	    break;

	case 1007:
	    direction=1;
	    break;

	case 1004:
	    purge=0;
	    break;

	case 1005:
	    purge=0;
	    break;
	
	case 32:
  	    skyd=0;
	    break;

	case 'q':
	    gsca = animationCount;
	    gameStatus = 9;
	    break;

	}
	return false;
    }
	
    public boolean mouseDown(java.awt.Event e,int x,int y)
    {
	if (redoGameClick == 1) redoGameClick = 2;
	if (gameStatus != 0) return false;
	if (startClick == 1) startClick = 2;
	if (linkClick == 1) linkClick = 2;
	return false;
    }

    public boolean mouseMove(java.awt.Event e,int x,int y)
    {
	if (startClick < 2) {
	    if ((x > 93) && (x < 419) && (y > startClickY) && (y < startClickY+14)) startClick = 1; else startClick = 0;
	}

	if (gtexty > -1) {
	    if (linkClick < 2) {
		if ((x > 256-gtextw[0]/2) && (x < 256+gtextw[0]/2) && (y > gtexty+gtextwidth*2-12) && (y < gtexty+gtextwidth*2+4)) linkClick = 1; else linkClick = 0;
	    }
	}

	if ((gameStatus == 9) && (x > 462-etWidth[5]) && (x < 462) && (y > 398) && (y <414)) {
	    if (redoGameClick == 0) redoGameClick = 1; 

	} else if (redoGameClick > 0) redoGameClick = 0;

	return false;
    }

    public boolean mouseDrag(java.awt.Event e,int x,int y)
    {
	/*	if ((gameStatus == 4) || (gameStatus == 5))
		editor.mouseDrag(this,e,x,y);*/

	return false;
    }

    public boolean mouseUp(java.awt.Event e,int x,int y)
    {
	/*	if ((gameStatus == 4) || (gameStatus == 5)) {
	    editor.mouseDown(this,e,x,y);
	    return false;
	    }*/

	showStatus(""+redoGameClick);

	if (redoGameClick == 2)
	    doMenu();


	if (gameStatus != 0) return false;

	if (startClick == 2) {
	    startClick = 0;
	    mouseMove(e,x,y);
	    if (startClick == 1) doGame(1);
	}

	if (linkClick == 2) {
	    linkClick = 0;
	    mouseMove(e,x,y);
	    if (linkClick == 1) followLink();
	}

	return false;
    }

    public void followLink () {
	try { 
	    if (linkAvaible == 0) getAppletContext().showDocument(new URL(gtext[2])); 
	    if (linkAvaible == 1) getAppletContext().showDocument(new URL("mailto:"+gtext[5])); 

	}
	catch (Exception e) {};
    }

}
