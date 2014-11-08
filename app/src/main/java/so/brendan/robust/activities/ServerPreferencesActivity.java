package so.brendan.robust.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import so.brendan.robust.R;

/**
 * Server preferences activity handles server connection preferences.
 */
public class ServerPreferencesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ServerPrefs())
                .commit();
    }

    /**
     * Server preferences: host and port.
     */
    public static class ServerPrefs extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_server);
        }
    }
}
