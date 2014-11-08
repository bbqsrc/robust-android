package so.brendan.robust.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.squareup.otto.Bus;

/**
 * A singleton provider of an Otto bus. Much more convenient and static-friendly
 * than BroadcastReceiver madness.
 */
public class BusProvider extends Bus {
    private static final String TAG = Constants.createTag(BusProvider.class);

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private static final BusProvider BUS = new BusProvider();

    public static BusProvider getInstance() {
        return BUS;
    }

    /**
     * Broadcasts an object to all registered listeners.
     *
     * Provided workaround to handle posting objects from threads other than the main thread,
     * such as from <code>MessengerService</code>.
     *
     * @param event
     */
    @Override
    public void post(final Object event) {
        Log.d(TAG, "Post received.");

        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mHandler.post(new Runnable() {
                @Override public void run() {
                    BusProvider.super.post(event);
                }
            });
        }
    }

    /**
     * If unregister is attempted on an object that isn't registered, an exception is fired. We
     * swallow it and move on with our lives.
     *
     * @param object
     */
    @Override
    public void unregister(Object object) {
        try {
            super.unregister(object);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, String.format("Attempted to unregister '%s' when not registered.",
                    object.getClass().getName()));
        }
    }

    private BusProvider() {}
}
