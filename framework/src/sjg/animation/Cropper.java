package sjg.animation;

import sjg.*;
import sjg.xml.*;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;

/**
 * Crops an image into {@link Frame frames} and {@link Animation animations} based on an XML-file.
 * <p>
 * The cropping takes place when the Cropper is constructed.
 * <p>
 * You must provide Cropper with an URL of the XML-file.
 *
 * @author Christian Hvid
 */

public class Cropper {
    private Image bigImage;
    private String bigImageFN = "";
    private MediaTracker mt;
    private SJGame sjg;
    private Vector frames = new Vector();
    private Vector animations = new Vector();

    private void addAnimation(Animation animation) {
        animations.addElement(animation);
    }

    private void addFrame(Frame frame) {
        frames.addElement(frame);
    }

    public Frame getFrame(String name) {
        for (Enumeration e = frames.elements(); e.hasMoreElements(); ) {
            Frame f = (Frame) e.nextElement();
            if (f.getName().equals(name)) return f;
        }
        throw new Error("Frame (" + name + ") not found in frame list");
    }

    public Animation getAnimation(String name) {
        for (Enumeration e = animations.elements(); e.hasMoreElements(); ) {
            Animation a = (Animation) e.nextElement();
            if (a.getName().equals(name)) return a;
        }
        throw new Error("Animation (" + name + ") not found in animation list");
    }

    private Image getImageFromJar(SJGame sjg, String f) {
        Image img;

        try {

            // Attempt to access JAR file first

            DataInputStream in = new DataInputStream(sjg.getClass().getResourceAsStream(f));

            byte[] data = new byte[in.available()];
            in.readFully(data);
            in.close();
            img = Toolkit.getDefaultToolkit().createImage(data);
        } catch (Exception e) {
            // Get the image via Applet.GetImage
            e.printStackTrace();
            img = sjg.getImage(sjg.getCodeBase(), f);
        }

        mt.addImage(img, 0);

        return img;
    }

    private Image getImageFromWeb(SJGame sjg, String fn) {
        return sjg.getImage(sjg.getDocumentBase(), fn);
    }

    public Image cropFromJar(String fn, int x, int y, int w, int h) {
        if (bigImageFN.equals(fn) == false)
            bigImage = getImageFromJar(sjg, fn);

        try {
            mt.addImage(bigImage, 1);
            mt.waitForID(1);
        } catch (Exception e) {
            // ignored
        }

        Image img = sjg.createImage(new FilteredImageSource(bigImage.getSource(),
                new CropImageFilter(x, y, w, h)));

        mt.addImage(img, 0);

        return img;
    }

    public Image cropFromWeb(String fn, int x, int y, int w, int h) {
        if (bigImageFN.equals(fn) == false)
            bigImage = getImageFromWeb(sjg, fn);

        try {
            mt.addImage(bigImage, 1);
            mt.waitForID(1);
        } catch (Exception e) {
            // ignored
        }

        Image img = sjg.createImage(new FilteredImageSource(bigImage.getSource(),
                new CropImageFilter(x, y, w, h)));

        mt.addImage(img, 0);

        return img;
    }

    public Cropper(SJGame sjg) {
        this.sjg = sjg;
        sjg.showStatus("Cropping images ...");
        mt = new MediaTracker(sjg);

        sjg.showStatus("Cropping images (parsing xml) ...");
        Element t = sjg.getConfiguration().getRoot();

        sjg.showStatus("Cropping images (interpretating xml) ...");

        for (Enumeration e = t.elements("frames"); e.hasMoreElements(); ) {
            Element t1 = (Element) e.nextElement();
            for (Enumeration e2 = t1.elements("frame"); e2.hasMoreElements(); ) {
                Element t2 = (Element) e2.nextElement();
                String name = t2.getAttribute("name").getValue();

                if (t.hasAttribute("name"))
                    name = t1.getAttribute("name").getValue() + "." + name;

                int x = t2.getAttribute("x").getIntValue();
                int y = t2.getAttribute("y").getIntValue();
                int width = t2.getAttribute("width").getIntValue();
                int height = t2.getAttribute("height").getIntValue();

                if (t1.hasAttribute("file"))
                    addFrame(new Frame(name, cropFromJar(t1.getAttribute("file").getValue(), x, y, width, height), width, height));
                else if (t1.hasAttribute("web"))
                    addFrame(new Frame(name, cropFromWeb(t1.getAttribute("web").getValue(), x, y, width, height), width, height));
            }
        }

        for (Enumeration e = t.elements("animations"); e.hasMoreElements(); ) {
            Element t1 = (Element) e.nextElement();
            for (Enumeration e2 = t1.elements("animation"); e2.hasMoreElements(); ) {
                Element t2 = (Element) e2.nextElement();

                String name = t2.getAttribute("name").getValue();
                if (t1.hasAttribute("name"))
                    name = t1.getAttribute("name").getValue() + "." + name;

                boolean looped = true;
                if (t2.hasAttribute("noloop"))
                    looped = false;

                Animation a = new Animation(name, looped);

                StringTokenizer st = new StringTokenizer(t2.getContents());
                for (; st.hasMoreTokens(); ) {
                    String token = st.nextToken();
                    a.addFrame(getFrame(token));
                }
                addAnimation(a);
            }
        }

        sjg.showStatus("Cropping images (waiting for images) ...");

        try {
            mt.waitForID(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sjg.showStatus("");
    }
}
