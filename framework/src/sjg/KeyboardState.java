package sjg;

import java.io.*;

/**
 * Represents the state of the keyboard.
 *
 * @author Christian Hvid
 */

public class KeyboardState implements Cloneable {
    private boolean key[] = new boolean[1024];

    public Object clone() {
        KeyboardState ks = new KeyboardState();
        for (int i = 0; i < 1024; i++)
            ks.key[i] = key[i];

        return ks;
    }

    /**
     * Returns true if a given key is down otherwise it returns false.
     */

    public boolean isDown(int i) {
        return key[i];
    }

    protected void setDown(int i, boolean value) {
        key[i] = value;
    }

    public void read(DataInputStream ind) throws IOException {
        int noKeysDown;

        for (int i = 0; i < 1024; i++)
            setDown(i, false);

        noKeysDown = ind.readInt();

        for (int i = 0; i < noKeysDown; i++)
            setDown(ind.readInt(), true);
    }

    public void write(DataOutputStream ud) throws IOException {
        int noKeysDown = 0;

        for (int i = 0; i < 1024; i++)
            if (isDown(i) == true) noKeysDown++;

        ud.writeInt(noKeysDown);

        for (int i = 0; i < 1024; i++)
            if (isDown(i) == true) ud.writeInt(i);
    }
}
