package sjg.network;

/**
 * {@link sjg.NetworkModel Network model} error state.
 * <p>
 * <p>Error message is returned by {@link #getErrorString getErrorString()}.
 *
 * @author Christian Hvid
 */

public class NMStateError extends NMState {
    protected String errorString;

    public NMStateError(String errorString) {
        super();
        this.errorString = errorString;
    }

    public String getErrorString() {
        return errorString;
    }
}
