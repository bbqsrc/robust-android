package so.brendan.robust.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.LruCache;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSession;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import so.brendan.robust.R;
import so.brendan.robust.activities.MainActivity;
import so.brendan.robust.db.DatabaseHelper;
import so.brendan.robust.interactors.SessionAuthenticator;
import so.brendan.robust.interactors.TwitterSessionAuthenticator;
import so.brendan.robust.listeners.OnSessionEventListener;
import so.brendan.robust.models.RobustSessionState;
import so.brendan.robust.models.RobustUser;
import so.brendan.robust.models.TLSSessionData;
import so.brendan.robust.models.commands.AuthCommand;
import so.brendan.robust.models.commands.BacklogCommand;
import so.brendan.robust.models.commands.ErrorCommand;
import so.brendan.robust.models.commands.JoinCommand;
import so.brendan.robust.models.commands.MessageCommand;
import so.brendan.robust.models.commands.PartCommand;
import so.brendan.robust.models.commands.RobustCommand;
import so.brendan.robust.models.commands.UserCommand;
import so.brendan.robust.utils.BackgroundIntentService;
import so.brendan.robust.utils.BusProvider;
import so.brendan.robust.utils.Constants;
import so.brendan.robust.utils.PicassoNotificationHelper;
import so.brendan.robust.utils.RobustPreferences;

/**
 * The messenger service.
 *
 * Handles the majority of the communication between the Robust server and the frontend.
 */
public class MessengerService extends BackgroundIntentService implements OnSessionEventListener {
    private static final String TAG = Constants.createTag(MessengerService.class);

    /** Holds the host name or IP. */
    public static final String EXTRA_HOST = Constants.createExtra("HOST");

    /** Holds the port. */
    public static final String EXTRA_PORT = Constants.createExtra("PORT");

    /** Holds the Robust command */
    public static final String EXTRA_COMMAND = Constants.createExtra("COMMAND");

    /** Holds the authentication mode. */
    public static final String EXTRA_AUTH_MODE = Constants.createExtra("AUTH_MODE");

    /** Holds the authentication key. */
    public static final String EXTRA_KEY = Constants.createExtra("KEY");

    /** Holds the authentication secret. */
    public static final String EXTRA_SECRET = Constants.createExtra("SECRET");

    /** Size of the user cache (in users) */
    private static final int MAX_USER_CACHE_SIZE = 100;

    /** The ID for notifications */
    private static final int ID_NOTIFICATION = 1;

    /** Dismisses any notifications that are present. */
    public static final String ACTION_DISMISS_NOTIFICATION =
            Constants.createAction("DISMISS_NOTIFICATION");

    /** Initalises a session if it isn't available yet. */
    public static final String ACTION_SESSION_INITIALISE =
            Constants.createAction("SESSION_INITIALISE");

    /** Requests the state of the current session. */
    public static final String ACTION_SESSION_STATE =
            Constants.createAction("SESSION_STATE");

    /** Requests that the current session reconnects. */
    public static final String ACTION_SESSION_RECONNECT =
            Constants.createAction("SESSION_RECONNECT");

    /** Requests the TLS state of the current session. */
    public static final String ACTION_SESSION_TLS =
            Constants.createAction("SESSION_TLS");

    /** Sends a command to the Robust service through the current session. */
    public static final String ACTION_SEND_COMMAND = Constants.createAction("SEND_COMMAND");

    /** Whether notifications are currently allowed. */
    private static boolean sAllowNotifications = true;

    /** User cache. Handy for stopping constant calls to the server. */
    private static final LruCache<String, RobustUser> sUserCache =
            new LruCache<String, RobustUser>(MAX_USER_CACHE_SIZE);

    /** Holds the current sessions */
    private HashMap<String, RobustSession> mSessions =
            new HashMap<String, RobustSession>();

    /** Holds state for when the service is purposely killing sessions. */
    private boolean mIsClosing;

    /** Holds the notification messages state */
    private ArrayList<MessageCommand> mNotificationMessages;

    private RobustPreferences mPreferences;
    private EventLoopGroup mEventLoopGroup;
    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;

    /**
     * Returns the user cache.
     *
     * @return
     */
    public static LruCache<String, RobustUser> getUserCache() {
        return sUserCache;
    }

    /**
     * Allows notifications to be displayed, nominally when no views are active.
     */
    public static void enableNotifications() {
        sAllowNotifications = true;
    }

    /**
     * Disallows notifications to be displayed, nominally when views are active.
     */
    public static void disableNotifications() {
        sAllowNotifications = false;
    }

    /**
     * Clears the current list of notifications, and removes the notification.
     *
     * @param context
     */
    public static void clearNotifications(Context context) {
        Intent intent = new Intent(context, MessengerService.class);
        intent.setAction(ACTION_DISMISS_NOTIFICATION);
        context.startService(intent);
    }

    /**
     * Convenience method for creating intents used within this service.
     *
     * @param context
     * @param action
     * @param host
     * @param port
     * @return
     */
    private static Intent createIntent(Context context, String action, String host, int port) {
        Intent intent = new Intent(context, MessengerService.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_HOST, host);
        intent.putExtra(EXTRA_PORT, port);
        return intent;
    }

    /**
     * Initialises a session with the specified authentication mode.
     *
     * @param context
     * @param authMode
     */
    public static void startSession(Context context, String authMode) {
        Intent intent = createDefaultIntent(context, ACTION_SESSION_INITIALISE);

        intent.putExtra(EXTRA_AUTH_MODE, authMode);

        context.startService(intent);
    }

    /**
     * Convenience method for creating an intent with relevant connection defaults from
     * application preferences.
     *
     * @param context
     * @param action
     * @return
     */
    private static Intent createDefaultIntent(Context context, String action) {
        RobustPreferences prefs = RobustPreferences.getInstance(context);
        return createIntent(context, action,
                prefs.getServerHost(), prefs.getServerPort());
    }

    /**
     * Attempts to start a session using defaults stored in application preferences.
     *
     * @param context
     */
    public static void startDefaultSession(Context context) {
        RobustPreferences prefs = RobustPreferences.getInstance(context);
        Intent intent = createIntent(context, ACTION_SESSION_INITIALISE,
                prefs.getServerHost(), prefs.getServerPort());

        intent.putExtra(EXTRA_AUTH_MODE, prefs.getAuthenticationMode());
        intent.putExtra(EXTRA_KEY, prefs.getAuthKey());
        intent.putExtra(EXTRA_SECRET, prefs.getAuthSecret());

        context.startService(intent);
    }

    /**
     * Attempts to restart a session using defaults stored in application preferences.
     *
     * @param context
     */
    public static void restartDefaultSession(Context context) {
        RobustPreferences prefs = RobustPreferences.getInstance(context);
        Intent intent = createIntent(context, ACTION_SESSION_RECONNECT,
                prefs.getServerHost(), prefs.getServerPort());

        intent.putExtra(EXTRA_AUTH_MODE, prefs.getAuthenticationMode());
        intent.putExtra(EXTRA_KEY, prefs.getAuthKey());
        intent.putExtra(EXTRA_SECRET, prefs.getAuthSecret());

        context.startService(intent);
    }

    /**
     * Sends a Robust command to the specified session.
     *
     * @param context
     * @param host
     * @param port
     * @param message
     */
    public static void sendCommand(Context context, String host, int port, RobustCommand message) {
        Intent intent = createIntent(context, ACTION_SEND_COMMAND, host, port);
        intent.putExtra(EXTRA_COMMAND, Parcels.wrap(message));
        context.startService(intent);
    }

    /**
     * Sends a Robust command to the default session.
     *
     * @param context
     * @param message
     */
    public static void sendCommand(Context context, RobustCommand message) {
        RobustPreferences prefs = RobustPreferences.getInstance(context);
        sendCommand(context, prefs.getServerHost(), prefs.getServerPort(), message);
    }

    /**
     * A convenience method for creating an intent of one of the specified constant actions and
     * running <code>startService(Intent intent)</code>.
     *
     * @param context
     * @param action
     */
    public static void requestAction(Context context, String action) {
        RobustPreferences prefs = RobustPreferences.getInstance(context);
        context.startService(createIntent(context, action,
                prefs.getServerHost(), prefs.getServerPort()));
    }

    public MessengerService() {
        super(MessengerService.class.getSimpleName());
        mIsClosing = false;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "created");
        super.onCreate();

        mPreferences = RobustPreferences.getInstance(this);

        mEventLoopGroup = new NioEventLoopGroup();

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(this)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_stat_message)
                .setAutoCancel(true);
        mNotificationMessages = new ArrayList<MessageCommand>();

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "destroyed service");
        mIsClosing = true;
        mEventLoopGroup.shutdownGracefully();
        stopSelf();
        super.onDestroy();
    }

    private static String formatHostString(String host, int port) {
        return String.format("%s:%s", host, port);
    }

    /**
     * Convenience method for getting a session for the given host and port.
     *
     * @param host
     * @param port
     * @return
     */
    private RobustSession getSession(String host, int port) {
        String hostString = formatHostString(host, port);
        return mSessions.get(hostString);
    }

    /**
     * Convenience method for getting a session for the provided parameters,
     * or creating one and returning it.
     *
     * @param host
     * @param port
     * @param authenticator
     * @param listener
     * @return
     */
    private RobustSession getOrCreateSession(String host, int port,
                                             SessionAuthenticator authenticator,
                                             OnSessionEventListener listener) {
        String hostString = formatHostString(host, port);

        RobustSession session = mSessions.get(hostString);
        if (session == null) {
            Log.d(TAG, "Creating new session.");
            session = new RobustSession(mEventLoopGroup, host, port, authenticator, listener);
            mSessions.put(hostString, session);
        } else if (!session.isAuthenticated() && authenticator != null) {
            Log.d(TAG, "Setting authenticator and authenticating.");
            Log.d(TAG, String.format("%s %s", session.isAuthenticated(), session.getState().toString()));
            session.setAuthenticator(authenticator);
            session.authenticate();
        }

        return session;
    }

    /**
     * Converts relevant parameters from an intent to an authenticator where possible.
     *
     * @param intent
     * @return
     */
    private SessionAuthenticator getAuthenticatorFromIntent(Intent intent) {
        String mode = intent.getStringExtra(EXTRA_AUTH_MODE);
        String key = intent.getStringExtra(EXTRA_KEY);
        String secret = intent.getStringExtra(EXTRA_SECRET);

        // TODO not just twitter!
        SessionAuthenticator authenticator = null;
        if (mode != null) {
            if (mode.equals("twitter")) {
                authenticator = new TwitterSessionAuthenticator(key, secret);
            }
        }

        return authenticator;
    }

    /**
     * Handler for <code>ACTION_SESSION_INITIALISE</code>.
     *
     * @param intent
     */
    private void handleSessionInit(Intent intent) {
        String host = intent.getStringExtra(EXTRA_HOST);
        int port = intent.getIntExtra(EXTRA_PORT, -1);
        SessionAuthenticator authenticator = getAuthenticatorFromIntent(intent);

        RobustSession session = getOrCreateSession(host, port, authenticator, this);

        // Handle case where it's already connected and no event will fire
        if (session.isConnected() && session.isAuthenticated()) {
            BusProvider.getInstance().post(session.getState());
        }
    }
    /**
     * Handler for <code>ACTION_SESSION_RECONNECT</code>.
     *
     * @param intent
     */
    private void handleSessionReconnect(Intent intent) {
        String host = intent.getStringExtra(EXTRA_HOST);
        int port = intent.getIntExtra(EXTRA_PORT, -1);

        RobustSession session = getSession(host, port);

        if (session != null) {
            session.restart();
        } else {
            handleSessionInit(intent);
        }
    }
    /**
     * Handler for <code>ACTION_SESSION_STATE</code>.
     *
     * @param intent
     */
    private void handleSessionState(Intent intent) {
        String host = intent.getStringExtra(EXTRA_HOST);
        int port = intent.getIntExtra(EXTRA_PORT, -1);

        RobustSession session = getSession(host, port);

        if (session == null) {
            BusProvider.getInstance().post(new RobustSessionState());
        } else {
            BusProvider.getInstance().post(session.getState());
        }
    }
    /**
     * Handler for <code>ACTION_SEND_COMMAND</code>.
     *
     * @param intent
     */
    private void handleSendCommand(Intent intent) {
        String host = intent.getStringExtra(EXTRA_HOST);
        int port = intent.getIntExtra(EXTRA_PORT, -1);

        RobustCommand message = Parcels.unwrap(
                intent.getParcelableExtra(EXTRA_COMMAND));

        RobustSession session = getSession(host, port);

        if (session != null) {
            session.sendMessage(message);
        } else {
            Log.w(TAG, String.format("Attempted to send command to null session %s:%s", host, port));
        }
    }

    /**
     * Handler for <code>ACTION_DISMISS_NOTIFICATION</code>.
     *
     * @param intent
     */
    private void handleDismissNotification(Intent intent) {
        mNotificationManager.cancel(ID_NOTIFICATION);
        mNotificationMessages.clear();
    }

    /**
     * Handler for <code>ACTION_SESSION_TLS</code>.
     *
     * @param intent
     */
    private void handleSessionTLS(Intent intent) {
        String host = intent.getStringExtra(EXTRA_HOST);
        int port = intent.getIntExtra(EXTRA_PORT, -1);

        RobustSession session = getSession(host, port);

        SSLSession sslSession = null;
        if (session != null && session.getSSLSession() != null) {
            sslSession = session.getSSLSession();
        }

        BusProvider.getInstance().post(new TLSSessionData(sslSession));
    }

    /**
     * Handles incoming intents from <code>startService(Intent intent)</code> calls.
     *
     * Passes through the intents to relevant handler methods, and logs errors.
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, String.format("Received intent: %s", intent));

        if (intent == null) {
            Log.d(TAG, "Received null intent; doing nothing.");
            return;
        }

        String action = intent.getAction();

        if (action == null) {
            Log.e(TAG, "received null action!");
            return;
        }

        if (action.equals(ACTION_SESSION_STATE)) {
            handleSessionState(intent);
        } else if (action.equals(ACTION_SESSION_TLS)) {
            handleSessionTLS(intent);
        } else if (action.equals(ACTION_SESSION_INITIALISE)) {
            handleSessionInit(intent);
        } else if (action.equals(ACTION_SEND_COMMAND)) {
            handleSendCommand(intent);
        } else if (action.equals(ACTION_DISMISS_NOTIFICATION)) {
            handleDismissNotification(intent);
        } else if (action.equals(ACTION_SESSION_RECONNECT)) {
            handleSessionReconnect(intent);
        } else {
            Log.w(TAG, "Unhandled intent: " + action);
        }
    }

    /**
     * Gets the <code>type</code> parameter from a raw JSON message from the Robust server.
     *
     * @param json
     * @return
     * @throws JSONException
     */
    private String getTypeFromJSON(String json) throws JSONException {
        JSONObject o = new JSONObject(json);
        return o.optString(RobustCommand.PARAM_TYPE, null);
    }

    /**
     * Handles raw JSON messages received from the Robust session.
     *
     * Generally passes through the command and the session to relevant handlers.
     *
     * @param session
     * @param message
     */
    @Override
    public void onMessageReceived(RobustSession session, String message) {
        String type;
        RobustCommand command;

        Log.d(TAG, String.format("<- '%s'", message));

        try {
            type = getTypeFromJSON(message);
        } catch (JSONException e) {
            type = null;
        }

        if (type == null) {
            Log.w(TAG, "received garbage message.");
            Log.d(TAG, message);
            return;
        }

        if (type.equals(RobustCommand.TYPE_AUTH)) {
            command = AuthCommand.fromJSON(message);
            onReceiveAuthCommand((AuthCommand) command, session);
        } else if (type.equals(RobustCommand.TYPE_MESSAGE)) {
            command = MessageCommand.fromJSON(message);
            onReceiveMessageCommand((MessageCommand) command, session);
        } else if (type.equals(RobustCommand.TYPE_BACKLOG)) {
            command = BacklogCommand.fromJSON(message);
            onReceiveBacklogCommand((BacklogCommand) command);
        } else if (type.equals(RobustCommand.TYPE_USER)) {
            command = UserCommand.fromJSON(message);
            onReceiveUserCommand((UserCommand) command);
        } else if (type.equals(RobustCommand.TYPE_ERROR)) {
            command = ErrorCommand.fromJSON(message);
            BusProvider.getInstance().post((ErrorCommand) command);
        } else if (type.equals(RobustCommand.TYPE_JOIN)) {
            command = JoinCommand.fromJSON(message);
            onReceiveJoinCommand((JoinCommand) command, session);
        } else if (type.equals(RobustCommand.TYPE_PART)) {
            command = PartCommand.fromJSON(message);
            onReceivePartCommand((PartCommand) command, session);
        } else if (type.equals(RobustCommand.TYPE_PING)) {
            Log.d(TAG, "Ping received; responding.");
            session.sendMessage(RobustCommand.createPong());
        } else if (type.equals(RobustCommand.TYPE_PONG)) {
            ; // No response required.
        } else {
            Log.w(TAG, String.format("Received unknown message type: %s", type));
            Log.d(TAG, message);
        }
    }

    /**
     * Handles <code>BacklogCommand</code>.
     *
     * @param backlog
     */
    private void onReceiveBacklogCommand(BacklogCommand backlog) {
        DatabaseHelper.Messages.upsert(this, backlog);

        BusProvider.getInstance().post(backlog);
    }

    private void onReceiveAuthCommand(AuthCommand command, RobustSession session) {
        session.onAuth(command);

        BusProvider.getInstance().post(command);
    }

    /**
     * Handles <code>MessageCommand</code>.
     *
     * @param message
     * @param session
     */
    private void onReceiveMessageCommand(MessageCommand message, RobustSession session) {
        boolean isNew = DatabaseHelper.Messages.upsert(this, message);

        RobustUser user = session.getState().getUser();

        if (isNew &&
                sAllowNotifications &&
                mPreferences.hasNotificationsEnabled() &&
                message.getBody().contains(user.getHandle())) {

            generateMessageNotification(message);
        }

        BusProvider.getInstance().post(message);
    }

    /**
     * Handles <code>UserCommand</code>.
     *
     * @param command
     */
    private void onReceiveUserCommand(UserCommand command) {
        RobustUser user = command.getUser();

        if (user.getId() != null) {
            synchronized (sUserCache) {
                sUserCache.put(user.getId(), user);
            }
        }

        BusProvider.getInstance().post(command);
    }

    /**
     * Handles <code>JoinCommand</code>.
     *
     * @param command
     * @param session
     */
    private void onReceiveJoinCommand(JoinCommand command, RobustSession session) {
        String target = command.getTarget();
        session.getState().addChannel(target);

        BusProvider.getInstance().post(command);
    }

    /**
     * Handles <code>PartCommand</code>.
     *
     * @param command
     * @param session
     */
    private void onReceivePartCommand(PartCommand command, RobustSession session) {
        String target = command.getTarget();
        session.getState().removeChannel(target);

        BusProvider.getInstance().post(command);
    }

    /**
     * Convenience method for updating the "Inbox Style" view of the messages notification.
     *
     * @return
     */
    private NotificationCompat.InboxStyle updateNotificationMessageList() {
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        RobustSession session = getSession(mPreferences.getServerHost(),
                mPreferences.getServerPort());

        // I don't really want to null check all of these properties for no gain.
        try {
            style.setSummaryText("@" + session.getState().getUser().getHandle());
        } catch (Exception e) {
            style.setSummaryText("Not logged in.");
        }

        style.setBigContentTitle(String.format("%s unread highlights",
                mNotificationMessages.size()));

        for (MessageCommand message : mNotificationMessages) {
            style.addLine(String.format("[%s] %s: %s",
                    message.getTarget(), message.getSenderHandle(), message.getBody()));
        }

        return style;
    }

    /**
     * Generates notifications for relevant messages.
     *
     * Shows a list of recent highlights (user's handle being said in a channel).
     *
     * @param message
     */
    private void generateMessageNotification(final MessageCommand message) {
        mNotificationMessages.add(message);

        mNotificationBuilder
            .setStyle(updateNotificationMessageList())
            .setContentTitle("Last highlight in " + message.getTarget())
            .setContentText(message.getBody())
            .setSubText("@" + message.getSenderHandle())
            .setTicker(String.format("[%s] %s: %s",
                    message.getTarget(), message.getSenderHandle(), message.getBody()))
            .setSmallIcon(R.drawable.ic_stat_message)
            .setNumber(mNotificationMessages.size());

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_COMMAND, Parcels.wrap(message));

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final Notification notification = mNotificationBuilder.build();

        notificationManager.notify(ID_NOTIFICATION, notification);

        if (!mPreferences.hasLowBandwidthImages()) {
            Log.d(TAG, "attempting to load image for @" + message.getSenderHandle());

            PicassoNotificationHelper contentViewResolver = new PicassoNotificationHelper(
                    this, message.getSenderId(), notification.contentView,
                    android.R.id.icon, ID_NOTIFICATION, notification);

            PicassoNotificationHelper bigContentViewResolver = new PicassoNotificationHelper(
                    this, message.getSenderId(), notification.bigContentView,
                    android.R.id.icon, ID_NOTIFICATION, notification);

            contentViewResolver.start();
            bigContentViewResolver.start();
        }
    }

    /**
     * Convenience method for broadcasting session state for registered listeners.
     *
     * @param session
     */
    private void broadcastSessionState(RobustSession session) {
        BusProvider.getInstance().post(session.getState());
    }

    /**
     * Listener for changes to session state.
     *
     * @param session
     * @param connectionChanged
     * @param authChanged
     */
    @Override
    public void onSessionStateChange(RobustSession session, boolean connectionChanged, boolean authChanged) {
        if (connectionChanged) {
            onConnectionStateChange(session);
        }

        if (authChanged) {
            onAuthenticationStateChange(session);
        }

        broadcastSessionState(session);
    }

    /**
     * Convenience method for logging connection state.
     *
     * @param session
     */
    private void onConnectionStateChange(RobustSession session) {
        switch (session.getState().getConnectionState()) {
            case RobustSession.STATE_CONNECTED:
                Log.i(TAG, String.format("Connected. (%s)", session.toString()));
                break;
            case RobustSession.STATE_CONNECTING:
                Log.i(TAG, String.format("Connecting. (%s)", session.toString()));
                break;
            case RobustSession.STATE_DISCONNECTED:
                Log.i(TAG, String.format("Disconnected. (%s)", session.toString()));
                break;
            default:
                Log.e(TAG, String.format("Unknown connection state (%s). (%s)",
                        session.toString(), session.getState().getConnectionState()));
                break;
        }
    }
    /**
     * Convenience method for logging authentication state.
     *
     * @param session
     */
    private void onAuthenticationStateChange(RobustSession session) {
        switch (session.getState().getAuthenticationState()) {
            case RobustSession.STATE_NOT_AUTHENTICATED:
                Log.i(TAG, String.format("Not authenticated. (%s)", session.toString()));
                break;
            case RobustSession.STATE_AUTHENTICATING:
                Log.i(TAG, String.format("Authenticating. (%s)", session.toString()));
                break;
            case RobustSession.STATE_AUTHENTICATED:
                Log.i(TAG, String.format("Authenticated. (%s)", session.toString()));
                break;
            case RobustSession.STATE_UNREGISTERED:
                Log.i(TAG, String.format("Authentication challenge received. (%s)", session.toString()));
                break;
            default:
                Log.e(TAG, String.format("Unknown auth state (%s). (%s)",
                        session.toString(), session.getState().getAuthenticationState()));
                break;
        }
    }

    /**
     * Listener for session completion.
     *
     * @param session
     */
    @Override
    public void onSessionFinished(RobustSession session) {
        final String host = session.getHost();
        final int port = session.getPort();

        final String hostString = formatHostString(host, port);

        broadcastSessionState(session);

        if (!mIsClosing && session.hasError()) {
            Log.e(TAG, "Session has error.", session.getError());

            int retries = session.getRetries();

            if (retries >= 3) {
                Log.i(TAG, String.format("Failed to reconnect. (%s)", hostString));
                mSessions.remove(hostString);
                return;
            }

            int timeout = (retries + 1) * 3;

            Log.i(TAG, String.format("Reconnecting in %s seconds. (%s)", timeout, hostString));

            mEventLoopGroup.schedule(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, String.format("Attempting to reconnect. (%s)", hostString));
                    RobustSession s = getSession(host, port);
                    if (s != null) {
                        s.reconnect();
                    }
                }
            }, timeout, TimeUnit.SECONDS);
        } else if (session.isRestarting()) {
            // Do nothing.
        }
    }

    /**
     * Listener for missing authenticators.
     *
     * This occurs when the session has been initialised with no user preferences and allows the
     * view to take the user to a login screen.
     *
     * @param session
     */
    @Override
    public void onAuthenticatorMissing(RobustSession session) {
        Log.d(TAG, String.format("Authenticator missing for session '%s'", session));
        broadcastSessionState(session);
    }
}
