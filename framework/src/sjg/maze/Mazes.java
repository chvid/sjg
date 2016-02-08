package sjg.maze;

import sjg.*;

import java.util.*;

/**
 * Mazes.
 *
 * @author Christian Hvid
 */

public class Mazes {
    private Hashtable h = new Hashtable();

    public Maze getMaze(String name) {
        return (Maze) h.get(name);
    }

    public void init() {
        for (Enumeration e = h.elements(); e.hasMoreElements(); ) ((Maze) e.nextElement()).init();
    }

    public Mazes(SJGame sjg, ElementFactory ef) {
        sjg.showStatus("Initializing mazes ...");
        sjg.xml.Element t = sjg.getConfiguration().getRoot();
        System.out.println(t.getName());
        for (Enumeration e = t.elements("mazes"); e.hasMoreElements(); ) {
            sjg.xml.Element t1 = (sjg.xml.Element) e.nextElement();
            for (Enumeration e2 = t1.elements("maze"); e2.hasMoreElements(); ) {
                sjg.xml.Element t2 = (sjg.xml.Element) e2.nextElement();
                String name = t2.getAttribute("name").getValue();
                h.put(name, new Maze(t2, ef));
            }
        }
    }
}
