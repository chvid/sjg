# Java Applet Games by Christian Hvid 1996-2004

![](./media-friendly.jpg)

This is a collection of Java Applet games made 1996 to 2004 and published on my personal websites (diami.au.dk/~chvid and vredungmand.dk).

Due to changed security policies and later deprecation of Java Applets by Oracle, they are no longer available online.

The games are:

* No Hats (1997)
* Mr. Platfoot (1999)
* Minor Bug (2000)
* Taleban vs. Robot (2002)
* Spacerace to Timbuktu (2002)
* Erik vs. Erik (2002)
* Mr. Hopwit and the mysterious maze (2004)
* Mr. Hopwit hits the roof (2004)

This is a youtube playlist with recordings of each of the games:

[![Mr. Hopwit hits the roof](http://img.youtube.com/vi/PZxyG85sVD4/0.jpg)](https://youtu.be/PZxyG85sVD4?list=PL6oesj_Ic5Mx3EWYHin0Gs0qSK0WD66Ij)

## Building and running

The games build with Apache Ant and a Java Development Kit. They require Java version 1.1 and 1.2 for sound support (only available in the games "Mr. Hopwit and the mysterious maze" and "Mr. Hopwit hits the roof").

Assuming you have Apache Ant and a JDK in your path you build it by first building the framework, then the game and finally run it with the appletviewer i.e:

```sh
cd framework
ant
cd ../games/hopwit
ant
appletviewer index.html
```
