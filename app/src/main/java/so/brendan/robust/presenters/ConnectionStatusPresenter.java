package so.brendan.robust.presenters;

import so.brendan.robust.models.TLSSessionData;

/**
 * A presenter for a ConnectionStatusView.
 */
public interface ConnectionStatusPresenter {
    /**
     * Start the presenter.
     */
    public void start();

    /**
     * Finish the presenter.
     */
    public void finish();

    /**
     * Update the TLS session data for the view.
     *
     * @param session
     */
    public void updateTLSSession(TLSSessionData session);
}
