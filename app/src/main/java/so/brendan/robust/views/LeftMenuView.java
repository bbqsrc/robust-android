package so.brendan.robust.views;

import java.util.List;

/**
 * A view for handling the left menu of the main activity.
 *
 * Currently used primarily for showing and interacting with channels.
 */
public interface LeftMenuView {
    /**
     * Updates the channel list based on the available channels of the current session.
     *
     * @param channels
     */
    public void updateChannelList(List<String> channels);

    /**
     * Updates the current target based on the current session.
     *
     * @param target
     */
    public void updateTarget(String target);
}
