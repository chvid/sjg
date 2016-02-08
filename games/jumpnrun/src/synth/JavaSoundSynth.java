package synth;

import javax.sound.sampled.*;

import java.io.*;

import java.util.*;

import java.text.*;

public class JavaSoundSynth implements Synth {
    public static final float SAMPLE_RATE = 16000.0F;
    public static final int SAMPLE_SIZE_IN_BITS = 16;
    public static final int NO_OUTPUT_CHANNELS = 1;
    public static final boolean SIGNED = true;
    public static final boolean BIG_ENDIAN = true;
    public static final int BYTES_PR_SAMPLE = 2;
    public static final int ENVELOPE_LENGTH = 1024;
    public static final int WAVEFORM_LENGTH = 1024;
    public static final int DATA_BUFFER_SIZE = 4096;
    public static final int NO_CHANNELS = 4;

    private Channel channel[] = new Channel[NO_CHANNELS];

    private AudioFormat audioFormat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, NO_OUTPUT_CHANNELS, SIGNED, BIG_ENDIAN);;
    private byte audioData[] = new byte[DATA_BUFFER_SIZE];;

    private SourceDataLine sourceDataLine;

    private Hashtable waveforms = new Hashtable();
    private Hashtable envelopes = new Hashtable();
    private Hashtable tracks = new Hashtable();

    private boolean enabled = true;

    private void precalculate() {
	short envelope[] = new short[ENVELOPE_LENGTH];

	for (int i = 0; i < ENVELOPE_LENGTH; i ++)
	    envelope[i] = (short)(255*Math.pow(1-(double)i / ENVELOPE_LENGTH,0.5));

	for (int i = 0; i < 16; i ++)
	    envelope[i] = (short)(envelope[i]/16.0*i);

	envelopes.put("ping", envelope);

	envelope = new short[ENVELOPE_LENGTH];

	for (int i = 0; i < ENVELOPE_LENGTH; i ++)
	    envelope[i] = 255;

	envelopes.put("flat", envelope);

	short wf[] = new short[WAVEFORM_LENGTH];

	for (int i = 0; i < WAVEFORM_LENGTH; i ++)
	    wf[i] = (short)(128*(Math.sin(Math.PI*2 / WAVEFORM_LENGTH * i)*(1-0.3*Math.sin(Math.PI*4 / WAVEFORM_LENGTH * i))));

	waveforms.put("sine", wf);

	wf = new short[WAVEFORM_LENGTH];

	for (int i = 0; i < WAVEFORM_LENGTH/2; i ++)
	    wf[i] = (short)(128*(4 / WAVEFORM_LENGTH * i-1));
	for (int i = WAVEFORM_LENGTH/2; i < WAVEFORM_LENGTH; i ++)
	    wf[i] = (short)(128*(3-4 / WAVEFORM_LENGTH * i));

	waveforms.put("saw", wf);

	wf = new short[WAVEFORM_LENGTH];

	for (int i = 0; i < WAVEFORM_LENGTH/2; i ++)
	    wf[i] = -127;
	for (int i = WAVEFORM_LENGTH/2; i < WAVEFORM_LENGTH; i ++)
	    wf[i] = 128;

	waveforms.put("square", wf);

	wf = new short[WAVEFORM_LENGTH];

	for (int i = 0; i < WAVEFORM_LENGTH; i ++)
	    wf[i] = (short)(256*Math.random()-128);

	waveforms.put("noise", wf);
    }

    class Channel {
	/*
	  http://www.musicdsp.org/archive.php?classid=3#92

	  r = rez amount, from sqrt(2) to ~ 0.1 
	  f = cutoff frequency 
	  (from ~0 Hz to SampleRate/2 - though many 
	  synths seem to filter only up to SampleRate/4) 

	  The filter algo: 
	  out(n) = a1 * in + a2 * in(n-1) + a3 * in(n-2) - b1*out(n-1) - b2*out(n-2) 

	  Lowpass:

	    c = 1.0 / tan(pi * f / sample_rate); 

	    a1 = 1.0 / ( 1.0 + r * c + c * c); 
	    a2 = 2* a1; 
	    a3 = a1; 
	    b1 = 2.0 * ( 1.0 - c*c) * a1; 
	    b2 = ( 1.0 - r * c + c * c) * a1; 

	  Hipass:

	    c = tan(pi * f / sample_rate); 

	    a1 = 1.0 / ( 1.0 + r * c + c * c); 
	    a2 = -2*a1;
	    a3 = a1; 
	    b1 = 2.0 * ( c*c - 1.0) * a1; 
	    b2 = ( 1.0 - r * c + c * c) * a1; 
	*/

	// state for filter

	private int filter_in1;
	private int filter_in2;
	private int filter_out1;
	private int filter_out2;

	// parameters for filter

	private int filter_a1;
	private int filter_a2;
	private int filter_a3;
	private int filter_b1;
	private int filter_b2;

	private double envelopeCursor;
	private double waveformCursor;	
	private double waveformCursor2;
	private double envelopeDelta;
	private double waveformDelta;	
	private double waveformDelta2;
	private boolean playing;
	private int volume;
	private int volume2;
	private short envelope[];
	private short waveform[];
	private short envelope2[];
	private short waveform2[];
	private int value;
	public int delta() {
	    if (playing) {
		waveformCursor += waveformDelta *
		    (1 - (volume2*waveform[((int)waveformCursor2) % WAVEFORM_LENGTH] * 
			  envelope2[((int)envelopeCursor)]) * 0.0000001);

		waveformCursor2 += waveformDelta2;
		envelopeCursor += envelopeDelta;

		if (envelopeCursor >= ENVELOPE_LENGTH) {
		    playing = false;
		    return 0;
		}

		int unfiltered = volume*waveform[((int)waveformCursor) % WAVEFORM_LENGTH] * envelope[((int)envelopeCursor)];
		
	        int filtered = (filter_a1 * unfiltered + filter_a2 * filter_in1 + filter_a3 * filter_in2 - 
				filter_b1 * filter_out1 - filter_b2 * filter_out2) / 256;
		
		filter_in2 = filter_in1;
		filter_in1 = unfiltered;
		
		filter_out2 = filter_out1;
		filter_out1 = filtered;

		return filtered;
	    }  else return 0;
	}
	public void play(double frequency, double length, double volume, double lpf_frequency, double lpf_resonance, 
			 short waveform[], short envelope[], double frequency2, double volume2, short waveform2[], short envelope2[]) {
	    this.volume = (int)(256*volume);
	    this.waveform = waveform;
	    this.envelope = envelope;
	    this.volume2 = (int)(256*volume2);
	    this.waveform2 = waveform2;
	    this.envelope2 = envelope2;

	    envelopeDelta = ((double)ENVELOPE_LENGTH / SAMPLE_RATE / length);
	    waveformDelta = (frequency * (double)WAVEFORM_LENGTH / SAMPLE_RATE);
	    envelopeCursor = 0;
	    waveformCursor = 0;

	    waveformDelta2 = (frequency2 * (double)WAVEFORM_LENGTH / SAMPLE_RATE);
	    waveformCursor2 = 0;

	    value = 0;
	    playing = true;

	    if (lpf_frequency > 0)
		lowpass (lpf_frequency, lpf_resonance);
	    else {
		filter_a1 = 256;
		filter_a2 = 0;
		filter_a3 = 0;
		filter_b1 = 0;
		filter_b2 = 0;
	    }
	}

	private void lowpass(double frequency, double resonance) {
	    double c = 1.0 / Math.tan(Math.PI * frequency / SAMPLE_RATE); 

	    double a1 = 1.0 / ( 1.0 + resonance * c + c * c); 
	    double a2 = 2* a1; 
	    double a3 = a1; 
	    double b1 = 2.0 * ( 1.0 - c*c) * a1; 
	    double b2 = ( 1.0 - resonance * c + c * c) * a1; 
	    
	    filter_a1 = (int)(256*a1);
	    filter_a2 = (int)(256*a2);
	    filter_a3 = (int)(256*a3);
	    filter_b1 = (int)(256*b1);
	    filter_b2 = (int)(256*b2);

	    /*
	  Lowpass:

	    c = 1.0 / tan(pi * f / sample_rate); 

	    a1 = 1.0 / ( 1.0 + r * c + c * c); 
	    a2 = 2* a1; 
	    a3 = a1; 
	    b1 = 2.0 * ( 1.0 - c*c) * a1; 
	    b2 = ( 1.0 - r * c + c * c) * a1; 

	  Hipass:

	    c = tan(pi * f / sample_rate); 

	    a1 = 1.0 / ( 1.0 + r * c + c * c); 
	    a2 = -2*a1;
	    a3 = a1; 
	    b1 = 2.0 * ( c*c - 1.0) * a1; 
	    b2 = ( 1.0 - r * c + c * c) * a1; 
	    */
	}
    }

    class Track {
	String keyList[] = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3"};
	class Note {
	    Channel channel;
	    double frequency;
	    double frequency2;
	    double length;
	    double volume;
	    double volume2;
	    double lpf_frequency;
	    double lpf_resonance;
	    short waveform[];
	    short envelope[];
	    short waveform2[];
	    short envelope2[];
	    public void play() {
		if (frequency > 0)
		    channel.play(frequency, length, volume, lpf_frequency, lpf_resonance, waveform, envelope,
				 frequency2, volume2, waveform2, envelope2);
	    }	    
	    public Note() {
		frequency = 0;
	    }		
	    public Note(Channel channel, double frequency, double length, double volume, double lpf_frequency, double lpf_resonance, 
			short waveform[], short envelope[], double frequency2, double volume2, short waveform2[], short envelope2[]) {
		this.channel = channel;
		this.frequency = frequency;
		this.length = length;
		this.volume = volume;
		this.lpf_frequency = lpf_frequency;
		this.lpf_resonance = lpf_resonance;
		this.waveform = waveform;
		this.envelope = envelope;
		this.frequency2 = frequency2;
		this.volume2 = volume2;
		this.waveform2 = waveform2;
		this.envelope2 = envelope2;
	    }		    
	}
	private Vector notes = new Vector();
	private int cursor;
	private boolean looped = false;
	private boolean playing = false;
	public Track(String tune) {
	    // E 2 E 2 C 4 E 2 E 2 C 4 D 2 D 2 C 2 E 2 F 4 C 4	    
	    
	    double frequency = 220;
	    double frequency2 = 230;
	    double length = 1;
	    double volume = 0.2;
	    double volume2 = 0;
	    double lpf_frequency = 0;
	    double lpf_resonance = Math.pow(2, 0.5);
	    short waveform[] = (short[])waveforms.get("sine");
	    short envelope[] = (short[])envelopes.get("flat");
	    short waveform2[] = (short[])waveforms.get("sine");
	    short envelope2[] = (short[])envelopes.get("flat");
	    Channel channel1 = null;

	    for (StringTokenizer st = new StringTokenizer(tune); st.hasMoreTokens();) {
		String key = st.nextToken();
		String value = st.nextToken();

		if (key.equals("f")) {
		    frequency = Double.parseDouble(value);
		} else if (key.equals("f2")) {
		    frequency2 = Double.parseDouble(value);
		} else if (key.equals("l")) {
		    length = Double.parseDouble(value);
		} else if (key.equals("v")) {
		    volume = Double.parseDouble(value);
		} else if (key.equals("v2")) {
		    volume2 = Double.parseDouble(value);
		} else if (key.equals("u")) {
		    lpf_frequency = Double.parseDouble(value);
		} else if (key.equals("q")) {
		    lpf_resonance = Double.parseDouble(value);
		} else if (key.equals("w")) {
		    waveform = (short[])waveforms.get(value);
		} else if (key.equals("w2")) {
		    waveform2 = (short[])waveforms.get(value);
		} else if (key.equals("e")) {
		    envelope = (short[])envelopes.get(value);
		} else if (key.equals("e2")) {
		    envelope2 = (short[])envelopes.get(value);
		} else if (key.equals("c")) {
		    channel1 = channel[Integer.parseInt(value)];
		} else if (key.equals("o")) {
		    looped = (value.equals("+"));
		} else {
		    int toneLength = Integer.parseInt(value);
		    int keyIndex = -1;
		    for (int i = 0; i < keyList.length; i++)
			if (key.equals(keyList[i]))
			    keyIndex = i;
		    
		    if (keyIndex == -1) notes.add(new Note());
		    else notes.add(new Note(channel1, Math.pow(Math.pow(2, 1/12.0), keyIndex)* frequency, length, volume, 
					    lpf_frequency, lpf_resonance, 
					    waveform, envelope, Math.pow(Math.pow(2, 1/12.0), keyIndex)*frequency2, volume2, waveform2, envelope2));
		    
		    for (int i = 0; i < toneLength - 1; i++)
			notes.add(new Note());
		}
	    }
	}
	public void tick() {
	    if (playing == false) return;
	    if (notes.size() == 0) return;
	    ((Note)notes.get(cursor)).play();
	    cursor ++;
	    if (cursor >= notes.size()) {
		if (looped == false) playing = false;
		cursor = 0;
	    }
	}
	public void start() {
	    playing = true;
	    cursor = 0;
	}
	public void stop() {
	    playing = false;
	}
    }

    private void render(byte audioData[]) {
	for (int i = 0; i < audioData.length / 2; i++) {
	    int value = 0;
	    for (int j = 0; j < NO_CHANNELS; j++)
		value += channel[j].delta();

	    audioData[i*2+1] = (byte)(value >> 8);
	    audioData[i*2] = (byte)(value >> 16);
	}
    }

    public void addTrack(String name, String configuration) {
	tracks.put(name, new Track(configuration));
    }

    public void stopTrack(String name) {
	((Track)tracks.get(name)).stop();
    }

    public void startTrack(String name) {
	((Track)tracks.get(name)).start();
    }

    public void enable() {
	enabled = true;
    }
    
    public void disable() {
	enabled = false;
    }

    private void play() {
	new Thread() {
		public void run() {
		    setPriority(MAX_PRIORITY);
		    sourceDataLine.start();
		    while (true) {
			for (Enumeration e = tracks.elements(); e.hasMoreElements(); )
			    ((Track)e.nextElement()).tick();
			
			render(audioData);
			
			if (enabled == false)
			    for (int i = 0; i < audioData.length; i++) audioData[i] = 0;

			sourceDataLine.write(audioData, 0, audioData.length);
		    }
		}
	    }.start();
    }

    public JavaSoundSynth() throws Exception {
	DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
	
	sourceDataLine = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
	sourceDataLine.open(audioFormat, audioData.length);

	precalculate();

	for (int i = 0; i < NO_CHANNELS; i ++)
	    channel[i] = new Channel();

	play();
    }
}

