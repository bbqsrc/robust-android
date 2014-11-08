package so.brendan.robust.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import so.brendan.robust.services.MessengerService;
import so.brendan.robust.utils.Constants;

/**
 * A simple receiver for initialising a session as soon as the device boots up.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = Constants.createTag(BootReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "attempting to connect and authenticate on boot.");
        MessengerService.startDefaultSession(context);
    }
}
