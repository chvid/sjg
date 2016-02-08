package sjg.network;

import sjg.*;

import java.io.*;

/**
 * Represents a player not sitting at the local machine.
 *
 * @author Christian Hvid
 */

public class RemotePlayer extends Player {
    synchronized void read(DataInputStream ind) throws IOException {
        MouseState ms = new MouseState();
        KeyboardState ks = new KeyboardState();
        ms.read(ind);
        setMouseState(ms);
        ks.read(ind);
        setKeyboardState(ks);
    }
}
