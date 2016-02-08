package sjg.scripting;

import java.util.*;

/**
 * Class representing a wait command. Causes script to do nada for a number of cycles.
 *
 * @author Christian Hvid
 */

public class Wait extends Command {
    private int frames;

    public void move(ScriptEngine engine, Stack stack, Callback callback) {
    }

    public int getWait() {
        return frames;
    }

    public Wait(int frames) {
        this.frames = frames;
    }
}
