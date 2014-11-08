package so.brendan.robust.interactors;

/**
 * An interactor for the LeftMenuPresenter.
 */
public interface LeftMenuInteractor {
    /**
     * Register the receivers or event bus.
     */
    public void registerListeners();

    /**
     * Unregister the receivers or event bus.
     */
    public void unregisterListeners();

    /**
     * Parts a channel.
     *
     * @param channel
     */
    public void partChannel(String channel);

    /**
     * Requests state from the service.
     */
    public void requestState();
}
