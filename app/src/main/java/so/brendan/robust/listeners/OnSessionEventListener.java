package so.brendan.robust.listeners;


import so.brendan.robust.services.RobustSession;

/**
 * Listener for relevant session events.
 */
public interface OnSessionEventListener {
    /**
     * Reports when a connection or authentication state has changed.
     *
     * @param session
     * @param connectionChanged
     * @param authChanged
     */
    public void onSessionStateChange(RobustSession session, boolean connectionChanged, boolean authChanged);

    /**
     * Reports when a session finishes.
     *
     * @param session
     */
    public void onSessionFinished(RobustSession session);

    /**
     * Reports when a session has no authenticator and authentication was attempted.
     *
     * @param session
     */
    public void onAuthenticatorMissing(RobustSession session);

    /**
     * Reports a received message from the underlying socket of the provided session.
     *
     * @param session
     * @param message
     */
    public void onMessageReceived(RobustSession session, String message);
}
