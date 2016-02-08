package synth;

public interface Synth {
    public void addTrack(String name, String configuration);
    public void startTrack(String name);
    public void stopTrack(String sound);
    public void enable();
    public void disable();
}
