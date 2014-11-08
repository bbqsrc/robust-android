package so.brendan.robust.presenters;

import java.util.List;

/**
 * A presenter for a LeftMenuFragment.
 */
public interface LeftMenuPresenter {
    /**
     * Start the presenter.
     */
    public void start();

    /**
     * Finish the presenter.
     */
    public void finish();

    /**
     * Sets the current target.
     *
     * @param target
     */
    public void setTarget(String target);

    /**
     * Parts the nominated channel.
     *
     * @param channel
     */
    public void partChannel(String channel);

    /**
     * Updates the list of available channels for the current user.
     *
     * @param channels
     */
    public void setAvailableChannels(List<String> channels);
}
