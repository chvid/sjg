package sjg.hiscore;

import java.util.Vector;

/**
 * Date: Jan 2, 2007
 *
 * @author Christian Hvid
 */

public interface HiscoreServer {
    Vector listEntries();

    void addEntry(Hiscore.Entry entry);
}
