package so.brendan.robust.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import so.brendan.robust.R;

/**
 * Main preferences activity handles the primary preferences for the application.
 */
public class MainPreferencesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new BandwidthPrefs())
                .commit();
    }

    /**
     * Bandwidth preferences fragment.
     *
     * Allows the user to disable high resolution image downloading, starting at boot time, and
     * notifications.
     */
    public static class BandwidthPrefs extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_main_bandwidth);
        }
    }
}
