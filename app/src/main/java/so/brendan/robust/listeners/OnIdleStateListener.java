package so.brendan.robust.listeners;

import io.netty.handler.timeout.IdleState;

/**
 * Listener for IdleState events.
 */
public interface OnIdleStateListener {
    /**
     * Listens for idle state from a netty channel.
     *
     * @param state
     */
    public void onIdleState(IdleState state);
}
