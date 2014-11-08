package so.brendan.robust.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * A wrapper around the default SharedPreferences.
 */
public class RobustPreferences {
    private static final String TAG = Constants.createTag(RobustPreferences.class);

    public static final String PREF_NOTIFICATIONS = "enable_notifications";
    public static final String PREF_AUTH_KEY = "auth_key";
    public static final String PREF_AUTH_SECRET = "auth_secret";
    public static final String PREF_AUTH_MODE = "auth_mode";
    public static final String PREF_BOOT_TIME = "load_at_boot_time";
    public static final String PREF_SERVER_HOST = "server_host";
    public static final String PREF_SERVER_PORT = "server_port";
    public static final String PREF_LAST_USED_TARGET = "last_used_target";
    public static final String PREF_LOW_BANDWIDTH_IMAGES = "low_bandwidth_images";

    private SharedPreferences mPreferences;

    public static RobustPreferences getInstance(Context context) {
        return new RobustPreferences(PreferenceManager.getDefaultSharedPreferences(context));
    }

    private RobustPreferences(SharedPreferences preferences) {
        mPreferences = preferences;
    }

    public boolean hasAuthenticationMode() {
        return getAuthenticationMode() != null;
    }

    public String getAuthenticationMode() {
        return mPreferences.getString(PREF_AUTH_MODE, null);
    }

    public void setAuthenticationMode(String mode) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_AUTH_MODE, mode);
        editor.apply();
    }

    public String getAuthKey() {
        return mPreferences.getString(PREF_AUTH_KEY, null);
    }

    public String getAuthSecret() {
        return mPreferences.getString(PREF_AUTH_SECRET, null);
    }

    public void setAuthentication(String mode, String key, String secret) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_AUTH_MODE, mode);
        editor.putString(PREF_AUTH_KEY, key);
        editor.putString(PREF_AUTH_SECRET, secret);
        editor.apply();
    }

    public boolean hasLoadAtBootTime() {
        return mPreferences.getBoolean(PREF_BOOT_TIME, true);
    }

    public boolean hasNotificationsEnabled() {
        return mPreferences.getBoolean(PREF_NOTIFICATIONS, true);
    }

    public void enableNotifications() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PREF_NOTIFICATIONS, true);
        editor.apply();
    }

    public void disableNotifications() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PREF_NOTIFICATIONS, false);
        editor.apply();
    }

    public String getServerHost() {
        return mPreferences.getString(PREF_SERVER_HOST, null);
    }

    public int getServerPort() {
        // Workaround for EditTextPreference only supporting string values...
        return Integer.valueOf(mPreferences.getString(PREF_SERVER_PORT, "-1"));
    }

    public boolean hasValidServerSettings() {
        return getServerHost() != null && getServerPort() != -1;
    }

    public String getLastUsedTarget() {
        return mPreferences.getString(PREF_LAST_USED_TARGET, null);
    }

    public void setLastUsedTarget(String target) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_LAST_USED_TARGET, target);
        editor.apply();
    }

    public void setLowBandwidthImages(boolean v) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PREF_LOW_BANDWIDTH_IMAGES, v);
        editor.apply();
    }

    public boolean hasLowBandwidthImages() {
        return mPreferences.getBoolean(PREF_LOW_BANDWIDTH_IMAGES, false);
    }
}
