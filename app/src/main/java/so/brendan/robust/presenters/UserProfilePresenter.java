package so.brendan.robust.presenters;

import so.brendan.robust.models.RobustUser;

/**
 * A presenter for a UserProfileView.
 */
public interface UserProfilePresenter {
    /**
     * Starts the user profile presenter with the provided user ID.
     *
     * @param userId
     */
    public void start(String userId);

    /**
     * Finishes the presenter.
     */
    public void finish();

    /**
     * Sets the current user for the presenter.
     *
     * @param user
     */
    public void setUser(RobustUser user);
}
