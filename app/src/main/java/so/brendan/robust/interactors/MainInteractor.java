package so.brendan.robust.interactors;

/**
 * An interactor for MainPresenter.
 */
public interface MainInteractor {
    /**
     * Register the receivers or event bus.
     */
    public void registerListeners();

    /**
     * Unregister the receivers or event bus.
     */
    public void unregisterListeners();

    /**
     * Request backlog from the service.
     *
     * @param target
     * @param fromTs
     * @param toTs
     */
    public void requestBacklog(String target, Long fromTs, Long toTs);
}
