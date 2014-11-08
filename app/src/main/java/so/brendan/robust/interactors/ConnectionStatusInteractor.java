package so.brendan.robust.interactors;
/**
 * An interactor for retrieving the TLS connection status of the current Robust session.
 */
public interface ConnectionStatusInteractor {
    /**
     * Registers the receivers or event bus.
     */
    public void registerListeners();

    /**
     * Unregisters the receivers or event bus.
     */
    public void unregisterListeners();

    /**
     * Requests the SSLSession for the current Robust session.
     */
    public void requestSSLSession();
}
