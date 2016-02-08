package synth;

import java.awt.event.*;

import sjg.*;

public class SynthManager {
    private Synth synth;
    private boolean enabled;
    private boolean listenToKeyboard;
    public SynthManager(SJGame game) {
	game.addKeyListener(new KeyAdapter() {
		public void keyTyped(KeyEvent e) {
		    if ((listenToKeyboard) && ((e.getKeyChar() == 's') || (e.getKeyChar() == 'S'))) {
			enabled = !enabled;
			if (enabled) synth.enable();
			else synth.disable();
		    }
		}
	    } );
	
	listenToKeyboard = true;

	try {
	    if (System.getProperty("java.version").startsWith("1.1") == false)
		synth = (Synth)Class.forName("synth.JavaSoundSynth").newInstance();
	    enabled = true;
	} catch (Exception e) {
	    System.out.println(""+e);
	    synth = null;
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
