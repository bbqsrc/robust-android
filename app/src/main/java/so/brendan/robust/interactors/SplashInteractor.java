package so.brendan.robust.interactors;

/**
 * An interactor for SplashPresenter.
 */
public interface SplashInteractor {
    /**
     * Registers receivers or event bus.
     */
    public void registerListeners();

    /**
     * Unregisters receivers or event bus.
     */
    public void unregisterListeners();

    /**
     * Attempts to initialise the session.
     */
    public void attemptToInitSession();
}
