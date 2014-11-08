package so.brendan.robust;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import so.brendan.robust.models.commands.ErrorCommand;
import so.brendan.robust.utils.BusProvider;

/**
 * A convenience subclass of Application for handling application-specific global state.
 *
 * Handles global error messaging and running runnables on the main thread.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        BusProvider.getInstance().unregister(this);
    }

    /**
     * Handles server error commands.
     *
     * @param error
     */
    @Subscribe
    public void onError(ErrorCommand error) {
        Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
    }

    /**
     * Runs the provided <code>Runnable</code> on the main thread.
     *
     * @param runnable
     */
    public static void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
