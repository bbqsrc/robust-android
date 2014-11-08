package so.brendan.robust.services;

import android.util.Log;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import so.brendan.robust.listeners.OnSessionEventListener;
import so.brendan.robust.utils.Constants;

/**
 * A callback for connection completion.
 *
 * Handles the success and failure states asynchronously and gracefully.
 */
public class MessengerChannelFutureListener implements ChannelFutureListener {
    private static String TAG = Constants.createTag(MessengerChannelFutureListener.class);

    private RobustSession mSession;
    private OnSessionEventListener mListener;

    protected MessengerChannelFutureListener(RobustSession session,
                                             OnSessionEventListener listener) {
        mSession = session;
        mListener = listener;
    }

    /**
     * Checks if the connection was successful.
     *
     * If it was, attempt to authenticate; otherwise, finish with the thrown exception.
     *
     * @param future
     */
    @Override
    public void operationComplete(ChannelFuture future) {
        if (!future.isSuccess()) {
            Log.e(TAG, "Connection error: " + future.cause().getMessage());

            // This broadcasts error state.
            mSession.finish(future.cause());
        } else {
            mSession.getState().setConnectionState(RobustSession.STATE_CONNECTED);
            mListener.onSessionStateChange(mSession, true, false);
            mSession.authenticate();
        }
    }
}
