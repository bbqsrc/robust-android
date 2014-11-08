package so.brendan.robust.views;

/**
 * A view for logging in using Twitter (OAuth 1.0a).
 */
public interface TwitterLoginView {
    /**
     * Opens the web view with the relevant URL.
     *
     * This URL is usually a callback to the Robust server for OAuth.
     *
     * @param url
     */
    public void openWebView(String url);

    /**
     * Navigates the application to the main activity.
     */
    public void navigateToMain();

    /**
     * Shows the loading dialog with the provided content.
     *
     * @param text
     */
    public void showLoadingDialog(String text);

    /**
     * Hides the loading dialog.
     */
    public void hideLoadingDialog();
}
