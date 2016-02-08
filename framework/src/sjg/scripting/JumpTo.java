package sjg.scripting;

import java.util.*;

/**
 * Class representing a jumpto command. Similar to goto.
 *
 * @author Christian Hvid
 */

public class JumpTo extends Command {
    private String script;

    public void move(ScriptEngine engine, Stack stack, Callback callback) {
        callback.jumpto(script);
        engine.jumpto(stack, script);
    }

    public String getScript() {
        return script;
    }

    public JumpTo(String script) {
        this.script = script;
    }
}
