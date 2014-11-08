package so.brendan.robust.presenters;

/**
 * A presenter for a TwitterLoginView.
 */
public interface TwitterLoginPresenter {
    /**
     * Start the presenter.
     */
    public void start();

    /**
     * Finish the presenter.
     */
    public void finish();

    /**
     * A callback for verifying the validity of the provided URL and providing
     * an action depending on context.
     *
     * @param url
     */
    public void validateURL(String url);
}
