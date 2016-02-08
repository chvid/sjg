package sjg.scripting;

import java.util.*;

/**
 * Class representing a spawn command. Creates a new thread and performs a method call.
 *
 * @author Christian Hvid
 */

public class Spawn extends Command {
    private String script;

    public void move(ScriptEngine engine, Stack stack, Callback callback) {
        callback.spawn(script);
        engine.spawn(script);
    }

    public String getScript() {
        return script;
    }

    public Spawn(String script) {
        this.script = script;
    }
}
