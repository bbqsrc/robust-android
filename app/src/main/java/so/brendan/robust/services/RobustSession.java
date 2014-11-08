package so.brendan.robust.services;

import android.util.Log;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import so.brendan.robust.interactors.SessionAuthenticator;
import so.brendan.robust.listeners.OnIdleStateListener;
import so.brendan.robust.listeners.OnSessionEventListener;
import so.brendan.robust.models.RobustSessionState;
import so.brendan.robust.models.commands.AuthCommand;
import so.brendan.robust.models.commands.RobustCommand;
import so.brendan.robust.utils.Constants;

/**
 * Handles all session-related functionality for connecting with the Robust server.
 *
 * The session authenticates through a <code>SessionAuthenticator</code>, allowing different
 * authentication mechanisms to be defined with a standard interface.
 *
 * Automatically attempts to reconnect when connection lost, and will automatically handle
 * reauthentication where possible.
 */
public class RobustSession implements OnIdleStateListener {
    private static final String TAG = Constants.createTag(RobustSession.class);

    // Connection states
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    // Authentication states
    public static final int STATE_NOT_AUTHENTICATED = 0;
    public static final int STATE_AUTHENTICATING = 1;
    public static final int STATE_AUTHENTICATED = 2;
    public static final int STATE_UNREGISTERED = 3;

    private RobustSessionState mState;
    private boolean mFinished = false;
    private Throwable mError = null;
    private SSLSession mSSLSession = null;
    private boolean mRestarting = false;

    private EventLoopGroup mEventLoopGroup;
    private Channel mChannel;

    private String mHost;
    private int mPort;

    private SessionAuthenticator mAuthenticator;
    private OnSessionEventListener mListener;
    private int mRetries;

    RobustSession(EventLoopGroup eventLoopGroup,
                  String host, int port,
                  SessionAuthenticator authenticator,
                  OnSessionEventListener listener) {

        mEventLoopGroup = eventLoopGroup;
        mHost = host;
        mPort = port;
        mListener = listener;
        mState = new RobustSessionState();
        mRetries = 0;

        if (authenticator != null) {
            setAuthenticator(authenticator);
        }

        connect();
    }

    /**
     * Returns how many retries have been attempted for this session when connection has been lost.
     *
     * @return
     */
    public int getRetries() {
        return mRetries;
    }

    /**
     * Forces the session to close and start again.
     *
     * @throws ChannelException
     */
    public void restart() throws ChannelException {
        if (isFinished()) {
            throw new ChannelException("Channel has been finalised.");
        }

        mRestarting = true;

        finish().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                reconnect();
            }
        });
    }

    /**
     * Reconnects the session only when in a finished state.
     *
     * @throws ChannelException
     */
    public void reconnect() throws ChannelException {
        if (!isFinished()) {
            throw new ChannelException("Channel has not been finalised.");
        }

        mFinished = false;
        mRetries++;
        connect();
    }

    /**
     * Returns the session state.
     *
     * @return
     */
    public RobustSessionState getState() {
        return mState;
    }

    /**
     * Returns whether the session is finished.
     *
     * @return
     */
    public boolean isFinished() {
        return mFinished;
    }

    /**
     * Asynchronous. Finishes the session and returns a future.
     *
     * @return
     */
    ChannelFuture finish() {
        return finish(true);
    }

    /**
     * Asynchronous. Finishes the session and returns a future.
     *
     * @return
     */
    ChannelFuture finish(boolean clearRetries) {
        final RobustSession session = this;
        reset(clearRetries);

        mFinished = true;

        mSSLSession = null;

        return mChannel.close().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                mListener.onSessionFinished(session);
            }
        });
    }

    /**
     * Asynchronous. Finishes the session and returns a future.
     *
     * Sets an error.
     *
     * @return
     */
    ChannelFuture finish(Throwable throwable) {
        mError = throwable;
        return finish(false);
    }

    /**
     * Returns whether the session closed with an error.
     *
     * @return
     */
    public boolean hasError() {
        return mError != null;
    }

    /**
     * Returns the session error.
     *
     * @return
     */
    public Throwable getError() {
        return mError;
    }

    /**
     * Returns the host for the session.
     *
     * @return
     */
    public String getHost() {
        return mHost;
    }

    /**
     * Returns the port for the session.
     *
     * @return
     */
    public int getPort() {
        return mPort;
    }

    /**
     * Sends a Robust message in the appropriate format.
     *
     * @param msg
     */
    public void sendMessage(RobustCommand msg) {
        Log.d(TAG, String.format("Sending: '%s'", msg.toJSON()));

        mChannel.writeAndFlush(msg.toJSON() + '\n');
    }

    /**
     * Sets the authenticator for the session.
     *
     * @param authenticator
     */
    void setAuthenticator(SessionAuthenticator authenticator) {
        if (authenticator == null) {
            Log.e(TAG, "authenticator cannot be null!");
            return;
        }

        mAuthenticator = authenticator;
    }

    /**
     * Returns whether an authenticator is present for the session.
     *
     * @return
     */
    boolean hasAuthenticator() {
        return mAuthenticator != null;
    }

    /**
     * Authenticates the session if an authenticator is provided.
     *
     */
    public void authenticate() {
        Log.d(TAG, "authenticate");
        int state = getState().getAuthenticationState();

        if (state == STATE_AUTHENTICATED) {
            return; // nothing to do;
        }

        if (mAuthenticator == null) {
            getState().setAuthenticationState(STATE_UNREGISTERED);
            mListener.onAuthenticatorMissing(this);
            return;
        }

        if (state != STATE_AUTHENTICATING) {
            getState().setAuthenticationState(STATE_AUTHENTICATING);

            RobustCommand msg = mAuthenticator.authenticate();
            if (msg != null) {
                sendMessage(msg);
            }
        }
    }

    /**
     * Returns whether the session is connected.
     *
     * @return
     */
    public boolean isConnected() {
        return getState().getConnectionState() == STATE_CONNECTED;
    }

    /**
     * Returns whether the session is authenticated.
     *
     * @return
     */
    public boolean isAuthenticated() {
        return getState().getAuthenticationState() == STATE_AUTHENTICATED;
    }

    /**
     * Returns whether the session is currently restarting.
     *
     * @return
     */
    public boolean isRestarting() {
        return mRestarting;
    }

    /**
     * Resets the session state back to defaults.
     *
     * @param clearRetries
     */
    private void reset(boolean clearRetries) {
        mFinished = false;

        if (clearRetries) {
            mRetries = 0;
        }

        RobustSessionState state = getState();

        state.setConnectionState(STATE_DISCONNECTED);
        state.setAuthenticationState(STATE_NOT_AUTHENTICATED);
    }

    /**
     * Connects the session to the Robust server.
     */
    private void connect() {
        Bootstrap b = new Bootstrap();

        b.group(mEventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new MessengerChannelInitializer(this, mHost, mPort));

        mRestarting = false;

        // Start the connection attempt.
        getState().setConnectionState(RobustSession.STATE_CONNECTING);
        mListener.onSessionStateChange(this, true, false);

        ChannelFuture f = b.connect(mHost, mPort);

        mChannel = f.channel();

        f.addListener(new MessengerChannelFutureListener(this, mListener));
    }

    /**
     * Sets the <code>SSLSession</code> object for this session.
     *
     * @param session
     */
    public void setSSLSession(SSLSession session) {
        try {
            for (int i = 0; i < session.getPeerCertificateChain().length; i++) {
                Log.d(TAG, String.format("%s\n%s",
                        session.getPeerCertificateChain()[i].getSubjectDN().toString(),
                        session.getPeerCertificateChain()[i].getIssuerDN().toString()));

            }

        } catch (SSLPeerUnverifiedException e) {
            e.printStackTrace();
        }

        mSSLSession = session;
    }

    /**
     * Returns the <code>SSLSession</code> object for this session.
     */
    public SSLSession getSSLSession() {
        return mSSLSession;
    }

    /**
     * Listener for raw messages received from the socket.
     *
     * @param message
     */
    public void onMessageReceived(String message) {
        mListener.onMessageReceived(this, message);
    }

    @Override
    public String toString() {
        return String.format("%s:%s", getHost(), getPort());
    }

    /**
     * Handles socket idle state. Will fire a ping after a specified amount of time, and will
     * attempt to reconnect if no reply received within specified period of time.
     *
     * @param state
     */
    @Override
    public void onIdleState(IdleState state) {
        if (state == IdleState.READER_IDLE) {
            finish(new Throwable("No response received from server."));
        } else if (state == IdleState.WRITER_IDLE) {
            sendMessage(RobustCommand.createPing());
        }
    }

    /**
     * Handles authentication changes.
     *
     * @param command
     */
    public void onAuth(AuthCommand command) {
        if (command.hasSuccess()) {
            Log.d(TAG, String.format("session authenticated: %s", hashCode()));

            getState().setAuthenticationState(STATE_AUTHENTICATED);
            getState().setUser(command.getUser());
        } else {
            Log.d(TAG, "session auth challenge");

            getState().setAuthenticationState(STATE_UNREGISTERED);
        }

        mListener.onSessionStateChange(this, false, true);
    }
}
