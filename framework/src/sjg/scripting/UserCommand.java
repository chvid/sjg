package sjg.scripting;

import java.util.*;

/**
 * Super class for user commands.
 *
 * @author Christian Hvid
 */

public class UserCommand extends Command {
    private String name;

    public void move(ScriptEngine engine, Stack stack, Callback callback) {
        callback.command(name);
    }

    public String getName() {
        return name;
    }

    public UserCommand(String name) {
        this.name = name;
    }
}
