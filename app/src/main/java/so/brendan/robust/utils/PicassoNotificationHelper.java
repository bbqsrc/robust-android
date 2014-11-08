package so.brendan.robust.utils;

import android.app.Notification;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import so.brendan.robust.App;
import so.brendan.robust.models.RobustUser;
import so.brendan.robust.models.commands.UserCommand;
import so.brendan.robust.services.MessengerService;

/**
 * A helper for getting the display picture for a Robust user for use in notifications.
 *
 * Where the user is not already cached, we hve to ask the server for the relevant URL to
 * get the images. Once URL is received, we request the image from Picasso and insert it directly
 * into the RemoveView.
 */
public class PicassoNotificationHelper {
    private static final String TAG = Constants.createTag(PicassoNotificationHelper.class);

    private Context mContext;
    private String mUserId;
    private RemoteViews mRemoteViews;
    private int mResId;
    private int mNotificationId;
    private Notification mNotification;

    public PicassoNotificationHelper(Context context, String userId,
                                     RemoteViews remoteViews,
                                     int resId, int notificationId,
                                     Notification notification) {
        mContext = context;
        mUserId = userId;
        mRemoteViews = remoteViews;
        mResId = resId;
        mNotificationId = notificationId;
        mNotification = notification;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            RobustUser user;

            synchronized (MessengerService.getUserCache()) {
                user = MessengerService.getUserCache().get(mUserId);
            }

            if (user != null) {
                setImage(user);
            } else {
                requestUser();
            }
        }
    };

    /**
     * Runs the process on the main thread so it can receive messages from the bus.
     */
    public void start() {
        App.runOnMainThread(mRunnable);
    }

    /**
     * Requests the user data from the server.
     */
    private void requestUser() {
        BusProvider.getInstance().register(this);
        MessengerService.sendCommand(mContext, new UserCommand(mUserId));
    }

    /**
     * Stops listening for a <code>UserCommand</code>.
     */
    public void cancel() {
        BusProvider.getInstance().unregister(this);
    }

    /**
     * Sets the image based on the received RobustUser information.
     *
     * @param user
     */
    protected void setImage(RobustUser user) {
        Log.d(TAG, "image set");
        Picasso.with(mContext)
                .load(user.getLargeDisplayPictureURL())
                .into(
                        mRemoteViews,
                        mResId,
                        mNotificationId,
                        mNotification);
    }

    /**
     * Listens for a <code>UserCommand</code> message from the service.
     * @param command
     */
    @Subscribe
    public void onUserCommand(final UserCommand command) {
        if (command.getUser().getId().equals(mUserId)) {
            cancel();
            setImage(command.getUser());
        }
    }
}
