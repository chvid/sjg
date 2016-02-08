package sjg.scripting;

import sjg.*;

import java.util.*;

/**
 * Class representing an user command with name and x, y position. (Usually for introducing sprites into the game).
 *
 * @author Christian Hvid
 */

public class CommandWithPosition extends UserCommand {
    private int x;
    private int y;

    public void move(ScriptEngine engine, Stack stack, Callback callback) {
        callback.command(getName(), x, y);
    }

    public CommandWithPosition(String name, int x, int y) {
        super(name);
        this.x = x;
        this.y = y;
    }
}
