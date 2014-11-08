package so.brendan.robust.models;

import org.parceler.Parcel;

import java.util.List;

import so.brendan.robust.services.RobustSession;

/**
 * A parcelable model for holding Robust session state.
 */
@Parcel
public class RobustSessionState {
    private int mConnectionState;
    private int mAuthenticationState;
    private RobustUser mUser;

    public int getConnectionState() {
        return mConnectionState;
    }

    public void setConnectionState(int s) {
        mConnectionState = s;
    }

    public int getAuthenticationState() {
        return mAuthenticationState;
    }

    public void setAuthenticationState(int s) {
        mAuthenticationState = s;
    }

    public RobustUser getUser() {
        return mUser;
    }

    public void setUser(RobustUser u) {
        mUser = u;
    }

    public void addChannel(String channel) {
        RobustUser user = getUser();

        if (user != null) {
            List<String> channels = user.getChannels();
            if (channels != null && !channels.contains(channel)) {
                channels.add(channel);
            }
        }
    }

    public void removeChannel(String channel) {
        RobustUser user = getUser();

        if (user != null) {
            List<String> channels = user.getChannels();
            if (channels != null && channels.contains(channel)) {
                channels.remove(channel);
            }
        }
    }

    public boolean isConnected() {
        return getConnectionState() == RobustSession.STATE_CONNECTED;
    }

    public boolean isAuthenticated() {
        return getAuthenticationState() == RobustSession.STATE_AUTHENTICATED;
    }

    public boolean requiresLogin() {
        return getAuthenticationState() == RobustSession.STATE_UNREGISTERED;
    }

    @Override
    public String toString() {
        return String.format("%s { Conn: %s, Auth: %s, User: %s }",
                RobustSessionState.class.getSimpleName(),
                getConnectionState(),
                getAuthenticationState(),
                getUser() != null ? getUser().toJSON() : null);
    }
}
