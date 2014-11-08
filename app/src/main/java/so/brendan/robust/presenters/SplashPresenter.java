package so.brendan.robust.presenters;

/**
 * A presenter for a SplashView.
 */
public interface SplashPresenter {
    /**
     * Start the presenter.
     */
    public void start();

    /**
     * Finish the presenter.
     */
    public void finish();

    /**
     * A callback for when login is required.
     */
    public void loginRequired();

    /**
     * A callback for when login was successful.
     */
    public void loginSuccessful();

    /**
     * A callback for when the service is connecting.
     */
    public void connecting();

    /**
     * A callback for when the service is authenticating.
     */
    public void authenticating();

    /**
     * A callback for when the connection has failed to connect.
     *
     * @param reason
     */
    public void connectionFailed(String reason);

    /**
     * A callback for when the session is already authenticated.
     */
    public void alreadyAuthenticated();
}
