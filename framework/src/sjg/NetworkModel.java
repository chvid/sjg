package sjg;

import sjg.network.*;
import java.util.*;

/**
 * The network model encapsulates network communication and maintains {@link sjg.network.RemotePlayer remote player}.
 * <p>
 * After each move call {@link sjg.SJGame SJGame} calls {@link #sendCommands sendCommands} on the selected network
 * model in order to signal that messages may be send without the user accessing shared ressources. After a pause
 * defined by animationSpeed in {@link sjg.SJGame SJGame}, {@link #receiveCommands receiveCommands} is called and the
 * network model may stop the program execution until data has been transported accross the network.
 *
 * @author Christian Hvid
 */

public class NetworkModel extends Thread {
    private NMState state;
    protected SJGame game;
    private String serverName = "";
    private Vector players = new Vector();

    public Player getPlayer(int i) {
        return (Player) players.elementAt(i);
    }

    public Enumeration getPlayers() {
        return players.elements();
    }

    public int getLocalPlayerIndex() {
        return players.indexOf(game.getLocalPlayer());
    }

    protected void addPlayer(Player player) {
        players.addElement(player);
    }

    protected void removePlayer(Player player) {
        players.removeElement(player);
    }

    public int getNoPlayers() {
        return players.size();
    }

    synchronized protected void setError(String errorString) {
        setNmState(new NMStateError(errorString));
    }

    protected void setServerName(String serverName) {
        this.serverName = serverName;
    }

    synchronized protected void setNmState(NMState state) {
        this.state = state;
    }

    public String getErrorString() {
        if (getNmState() instanceof NMStateError)
            return ((NMStateError) getNmState()).getErrorString();
        else
            return "";
    }

    public String getServerName() {
        return serverName;
    }

    public NetworkModel(SJGame game) {
        this.game = game;
    }

    public NMState getNmState() {
        return state;
    }

    protected void sendCommands() {
        getNmState().sendCommands();
    }

    protected void receiveCommands() {
        getNmState().receiveCommands();
    }
}
