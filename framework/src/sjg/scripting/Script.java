package sjg.scripting;

import java.util.*;

/**
 * Class representing a script as a list of command.
 *
 * @author Christian Hvid
 */

public class Script {
    private String name;
    private Vector commands = new Vector();
    public static final int FINISHED_SCRIPT = 0;
    public static final int FINISHED_COMMAND = 1;
    public static final int UNFINISHED_COMMAND = 2;

    public String getName() {
        return name;
    }

    public Script(String name) {
        this.name = name;
    }

    public int size() {
        return commands.size();
    }

    public Command getCommand(int index) {
        return (Command) commands.elementAt(index);
    }

    public void add(Command command) {
        commands.addElement(command);
    }
}
