package sjg.scripting;

import sjg.*;
import sjg.xml.*;

import java.util.*;

/**
 * ScriptEngine responsible for parse an xml-script. Holding a collection of scripts. And executing them.
 *
 * @author Christian Hvid
 */

public class ScriptEngine {
    private SJGame sjg;
    private Hashtable scripts = new Hashtable();
    private Vector executionStacks = new Vector();

    private Callback callback;

    public int move(Stack stack, StackFrame frame, Callback callback) {
        if (frame.isFinished()) return Script.FINISHED_SCRIPT;
        for (; frame.hasNext(); ) {
            Command command = frame.next();
            command.move(this, stack, callback);
        }
        if (frame.hasNext()) return Script.UNFINISHED_COMMAND;
        return Script.FINISHED_COMMAND;
    }

    public void move(Stack stack) {
        StackFrame frame = (StackFrame) stack.peek();
        frame.newTick();
        switch (move(stack, frame, callback)) {
            case Script.FINISHED_SCRIPT:
                stack.pop();
                move();
                break;
            case Script.FINISHED_COMMAND:
                break;
            case Script.UNFINISHED_COMMAND:
                move();
                break;
        }
    }

    public void move() {
        for (Enumeration e = executionStacks.elements(); e.hasMoreElements(); ) {
            Stack stack = (Stack) e.nextElement();
            if (stack.empty())
                executionStacks.removeElement(stack);
            else
                move(stack);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public Script getScript(String name) {
        return (Script) scripts.get(name);
    }

    public void jumpto(Stack stack, String name) {
        stack.pop();
        execute(stack, name);
    }

    public void execute(Stack stack, String name) {
        Script script = (Script) scripts.get(name);
        stack.push(new StackFrame(script));
    }

    public void spawn(String name) {
        Stack stack = new Stack();
        executionStacks.addElement(stack);
        execute(stack, name);
    }

    public void clear() {
        executionStacks.removeAllElements();
    }

    public ScriptEngine(SJGame sjg) {
        this.sjg = sjg;
        sjg.showStatus("Scripting engine (parsing xml) ...");
        Element t = sjg.getConfiguration().getRoot();
        sjg.showStatus("Scripting engine (creating script) ...");

        for (Enumeration e = t.elements("scripts"); e.hasMoreElements(); ) {
            Element t1 = (Element) e.nextElement();
            for (Enumeration e2 = t1.elements("script"); e2.hasMoreElements(); ) {
                Element t2 = (Element) e2.nextElement();
                String name = t2.getAttribute("name").getValue();
                Script script = new Script(name);
                for (Enumeration e3 = t2.elements(); e3.hasMoreElements(); ) {
                    Object o = e3.nextElement();
                    if (o instanceof Element) {
                        Element t3 = (Element) o;
                        if (t3.getName().equals("command")) {
                            if (t3.hasAttribute("x"))
                                script.add(new CommandWithPosition(t3.getAttribute("name").getValue(),
                                        t3.getAttribute("x").getIntValue(),
                                        t3.getAttribute("y").getIntValue()));
                            else {
                                if (t3.hasAttribute("text"))
                                    script.add(new CommandWithText(t3.getAttribute("name").getValue(), t3.getAttribute("text").getValue()));
                                else
                                    script.add(new UserCommand(t3.getAttribute("name").getValue()));
                            }
                        }

                        if (t3.getName().equals("wait"))
                            script.add(new Wait(t3.getAttribute("time").getIntValue()));

                        if (t3.getName().equals("jumpto"))
                            script.add(new JumpTo(t3.getAttribute("script").getValue()));

                        if (t3.getName().equals("execute"))
                            script.add(new Execute(t3.getAttribute("script").getValue()));

                        if (t3.getName().equals("spawn"))
                            script.add(new Spawn(t3.getAttribute("script").getValue()));
                    }
                }
                scripts.put(name, script);
            }
        }
        sjg.showStatus("");
    }
}
