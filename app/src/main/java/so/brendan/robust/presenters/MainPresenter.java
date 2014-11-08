package so.brendan.robust.presenters;

import so.brendan.robust.models.RobustSessionState;
import so.brendan.robust.models.commands.BacklogCommand;
import so.brendan.robust.models.commands.MessageCommand;
import so.brendan.robust.models.commands.PartCommand;

/**
 * A presenter for a MainView.
 */
public interface MainPresenter {
    /**
     * Start the presenter.
     */
    public void start();

    /**
     * Finish the presenter.
     */
    public void finish();

    /**
     * Sends a message received from the view.
     *
     * @param target
     * @param message
     */
    public void sendMessage(String target, String message);

    /**
     * Provides updates to the view about the current session state.
     *
     * @param state
     */
    public void updateStatus(RobustSessionState state);

    /**
     * Responds to a request from the view for backlog.
     *
     * @param target
     */
    public void requestBacklog(String target, Long fromTs, Long toTs);

    /**
     * A callback for received message commands.
     *
     * @param command
     */
    public void messageReceived(MessageCommand command);

    /**
     * A callback for received backlog commands.
     *
     * @param command
     */
    public void backlogReceived(BacklogCommand command);

    /**
     * A callback for received part commands.
     *
     * @param command
     */
    public void partReceived(PartCommand command);
}
