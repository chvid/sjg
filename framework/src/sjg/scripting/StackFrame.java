package sjg.scripting;

/**
 * Program stack for script.
 *
 * @author Christian Hvid
 */

public class StackFrame {
    private Script script;
    private int count = 0;
    private int wait = 0;
    private int programCounter = 0;
    private Command currentCommand;

    public void newTick() {
        count++;
    }

    public boolean hasNext() {
        return // false is we are waiting && false if we are out of commands
                ((wait <= count) && (programCounter < script.size()));
    }

    public Command next() {
        currentCommand = script.getCommand(programCounter);
        if (currentCommand instanceof Wait) {
            count = 0;
            wait = ((Wait) currentCommand).getWait();
        }

        programCounter++;
        return currentCommand;
    }

    public boolean isFinished() {
        return (programCounter >= script.size());
    }

    public Script getScript() {
        return script;
    }

    public StackFrame(Script script) {
        this.script = script;
        programCounter = 0;
    }
}
