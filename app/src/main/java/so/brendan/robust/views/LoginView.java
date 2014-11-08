package so.brendan.robust.views;

/**
 * A view for handling transitioning to various authentication screens.
 */
public interface LoginView {
    /**
     * Navigates the application to the Twitter login activity.
     */
    public void navigateToTwitterLogin();

    /**
     * Navigates the application to the plain login activity.
     */
    public void navigateToPlainLogin();
}
