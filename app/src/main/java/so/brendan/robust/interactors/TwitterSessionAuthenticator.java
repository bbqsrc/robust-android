package so.brendan.robust.interactors;

import so.brendan.robust.models.commands.AuthCommand;
import so.brendan.robust.models.commands.RobustCommand;
import so.brendan.robust.utils.Constants;

/**
 * An implementation of SessionAuthenticator for Twitter authentication.
 */
public class TwitterSessionAuthenticator implements SessionAuthenticator {
    private static final String TAG = Constants.createTag(TwitterSessionAuthenticator.class);

    private String mKey;
    private String mSecret;

    public TwitterSessionAuthenticator(String key, String secret) {
        mKey = key;
        mSecret = secret;
    }

    @Override
    public RobustCommand authenticate() {
        return new AuthCommand.Builder()
                .setMode("twitter")
                .setChallenge(mKey, mSecret)
                .build();
    }
}
