package sjg.synth;

import sjg.SJGame;
import sjg.maze.Maze;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

public class SynthManager {
    private Synth synth;
    private boolean enabled;
    private boolean listenToKeyboard;

    public SynthManager() {
        try {
            if (System.getProperty("java.version").startsWith("1.1") == false) {
                synth = (Synth) Class.forName("sjg.synth.JavaSoundSynth").newInstance();
                enabled = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            synth = null;
        }
    }

    public SynthManager(SJGame game) {
        this();

        if (synth != null) {
            game.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if ((listenToKeyboard) && ((e.getKeyChar() == 's') || (e.getKeyChar() == 'S'))) {
                        enabled = !enabled;
                        if (enabled) synth.enable();
                        else synth.disable();
                    }
                }
            });

            listenToKeyboard = true;

            sjg.xml.Element t = game.getConfiguration().getRoot();

            for (Enumeration e = t.elements("tracks"); e.hasMoreElements(); ) {
                sjg.xml.Element t1 = (sjg.xml.Element) e.nextElement();
                for (Enumeration e2 = t1.elements("track"); e2.hasMoreElements(); ) {
                    sjg.xml.Element t2 = (sjg.xml.Element) e2.nextElement();
                    String name = t2.getAttribute("name").getValue();
                    String contents = t2.getContents();

                    addTrack(name, contents);
                }
            }
        }
    }

    public void setListenToKeyboard(boolean value) {
        listenToKeyboard = value;
    }

    public void addTrack(String name, String configuration) {
        if (synth == null) return;
        synth.addTrack(name, configuration);
    }

    public void startTrack(String sound) {
        if (synth == null) return;
        synth.startTrack(sound);
    }

    public void stopTrack(String sound) {
        if (synth == null) return;
        synth.stopTrack(sound);
    }

    public boolean available() {
        return (synth != null);
    }
}
