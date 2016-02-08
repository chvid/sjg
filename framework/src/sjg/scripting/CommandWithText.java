package sjg.scripting;

import java.util.Stack;

/**
 * Class representing an user command with name and a text parameter.
 *
 * @author Christian Hvid
 */

public class CommandWithText extends UserCommand {
    private String text;

    public void move(ScriptEngine engine, Stack stack, Callback callback) {
        callback.command(getName(), text);
    }

    public CommandWithText(String name, String text) {
        super(name);
        this.text = text;
    }
}

