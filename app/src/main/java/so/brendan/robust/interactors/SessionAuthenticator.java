package so.brendan.robust.interactors;

import so.brendan.robust.models.commands.RobustCommand;

/**
 * An interface for implementing Robust session authentication objects.
 */
public interface SessionAuthenticator {
    /**
     * Attempts to authenticate the session.
     *
     * @return
     */
    public RobustCommand authenticate();
}
