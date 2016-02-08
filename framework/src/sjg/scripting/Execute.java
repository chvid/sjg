package sjg.scripting;

import java.util.Stack;

/**
 * Class representing an execute command. Similar to method call.
 *
 * @author Christian Hvid
 */

public class Execute extends Command {
    private String script;

    public void move(ScriptEngine engine, Stack stack, Callback callback) {
        engine.execute(stack, script);
    }

    public String getScript() {
        return script;
    }

    public Execute(String script) {
        this.script = script;
    }
}
