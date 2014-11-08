package so.brendan.robust.interactors;

/**
 * An interactor for UserProfilePresenter.
 */
public interface UserProfileInteractor {
    /**
     * Registers receivers or event bus.
     */
    public void registerListeners();

    /**
     * Unregisters receivers or event bus.
     */
    public void unregisterListeners();

    /**
     * Requests a RobustUser by provided id from the service.
     * @param id
     */
    public void requestUser(String id);

    /**
     * Requests session state from the service.
     */
    public void requestSessionState();
}
