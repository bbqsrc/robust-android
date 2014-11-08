package so.brendan.robust.views;

import so.brendan.robust.models.commands.BacklogCommand;
import so.brendan.robust.models.commands.MessageCommand;
import so.brendan.robust.models.commands.PartCommand;

/**
 * A view for handling interaction between the Robust server and the application.
 */
public interface MainView {
    /**
     * Shows the loading dialog.
     */
    public void showLoadingDialog();

    /**
     * Hides the loading dialog.
     */
    public void hideLoadingDialog();

    /**
     * Shows the status bar.
     */
    public void showStatusBar();

    /**
     * Hides the status bar.
     */
    public void hideStatusBar();

    /**
     * Clears the screen when the state is reset to not be connected to a target.
     */
    public void clearScreen();

    /**
     * Sets the status bar text. Usually for showing connection status when not
     * currently connected.
     *
     * @param text
     */
    public void setStatusBarText(String text);

    public void updateMessages(MessageCommand command);
    public void updateMessages(BacklogCommand command);
    public void updateMessages(PartCommand command);
}
