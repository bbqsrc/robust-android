package so.brendan.robust.views;

import so.brendan.robust.models.commands.BacklogCommand;
import so.brendan.robust.models.commands.MessageCommand;

/**
 * A fragment view for displaying received chat messages.
 */
public interface ChatMessagesView {
    /**
     * Insert messages to the view.
     *
     * @param command
     */
    public void insertMessages(BacklogCommand command);

    /**
     * Insert a message to the view.
     *
     * @param command
     */
    public void insertMessage(MessageCommand command);

    /**
     * Clear the state of the view and clear the list.
     */
    public void clear();

    /**
     * Sets the current target and gets the backlog from the database and server.
     * @param target
     */
    public void setTarget(String target);

    /**
     * Gets the current target.
     * @return
     */
    public String getTarget();

    /**
     * Scrolls the list to the bottom.
     */
    public void scrollToBottom();

    /**
     * Returns a message for the provided position.
     *
     * @param position
     * @return
     */
    public MessageCommand getItem(int position);
}
