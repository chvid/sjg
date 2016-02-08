package sjg.scripting;

import java.util.Stack;

/**
 * Abstract superclass for a command in the scripting language.
 *
 * @author Christian Hvid
 */

public abstract class Command {
    public abstract void move(ScriptEngine engine, Stack stack, Callback callback);

    public int getWait() {
        return 0;
    }
}
