package sjg.network;

import sjg.*;

/**
 * The default single player {@link sjg.NetworkModel network model}.
 *
 * @author Christian Hvid
 */

public class NullNetworkModel extends NetworkModel {
    public NullNetworkModel(SJGame game1) {
        super(game1);
        addPlayer(game.getLocalPlayer());
        setNmState(new NMStateNotConnected());
    }
}
