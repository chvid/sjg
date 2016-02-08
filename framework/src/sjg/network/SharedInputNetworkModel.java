package sjg.network;

import sjg.*;

import java.net.*;
import java.util.*;
import java.io.*;

/**
 * A Peer-to-peer {@link sjg.NetworkModel network model}.
 * <p>
 * <p>This network model maintains player so input from all users
 * connected to the game can be read by every client.
 *
 * @author Christian Hvid
 */

public class SharedInputNetworkModel extends NetworkModel {
    static final int PORT = 15337;
    Socket socket;
    DataOutputStream ud;
    DataInputStream ind;

    int allowedLatency = 0;
    int gameStartedAt = 0;
    int intendedNoPlayers = 2;

    private NMStateNotConnected notConnected = new NMStateNotConnected();
    private NMStateConnected connected = new NMStateConnected();
    private NMStateGame gameState = new NMStateGame() {
        boolean isSynchronized = false;
        boolean haveSendSomething = false;
        int latency = 0;

        public void sendCommands() {
            try {
                ud.writeInt(game.getFrameCount() - gameStartedAt);
                game.getLocalPlayer().write(ud);
                haveSendSomething = true;
            } catch (Exception e) {
            }
        }

        public void receiveCommands() {
            if (haveSendSomething == false) return;
            try {
                if ((allowedLatency > latency) && (isSynchronized == true))
                    latency++;
                else {
                    for (Enumeration e = getPlayers(); e.hasMoreElements(); ) {
                        Player p = (Player) e.nextElement();
                        if (p != game.getLocalPlayer()) {
                            int fc = ind.readInt();
                            ((RemotePlayer) p).read(ind);
                        }
                    }
                    isSynchronized = true;
                }
            } catch (Exception e) {
            }
        }
    };

    public void run() {
        try {
            socket = new Socket(getServerName(), PORT);
            ud = new DataOutputStream(socket.getOutputStream());
            ind = new DataInputStream(socket.getInputStream());

            setNmState(connected);

            int playerIndex = ind.readInt();

            removePlayer(game.getLocalPlayer());

            for (int i = 0; i < intendedNoPlayers; i++)
                if (i == playerIndex)
                    addPlayer(game.getLocalPlayer());
                else
                    addPlayer(new RemotePlayer());

            setNmState(gameState);
            gameStartedAt = game.getFrameCount();
        } catch (Exception e) {
            setError(e.toString());
        }
    }

    public SharedInputNetworkModel(SJGame game, int allowedLatency) {
        super(game);

        setServerName(game.getCodeBase().getHost());

        this.allowedLatency = allowedLatency;

        addPlayer(game.getLocalPlayer());
        setNmState(notConnected);
        start();
    }
}
