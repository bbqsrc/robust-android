package so.brendan.robust.views;

/**
 * A view for handling the initial loading of the application and handling connection status.
 */
public interface SplashView {
    /**
     * Navigates the application to the login view.
     */
    public void navigateToLogin();

    /**
     * Navigates the application to the main view.
     *
     * Can be fired to not show a transition, to hide the fact the activity ever loaded,
     * such as when application state is lost but the service is already fully authenticated.
     *
     * @param noTransition Determines whether a transition should occur.
     */
    public void navigateToMain(boolean noTransition);

    /**
     * Open the server preferences, for when no server preferences have been set.
     */
    public void navigateToServerPreferences();

    /**
     * Shows an error with specified reason.
     *
     * @param reason
     */
    public void showError(String reason);

    /**
     * Shows connecting status.
     */
    public void showConnecting();

    /**
     * Shows authenticating status.
     */
    public void showAuthenticating();
}
