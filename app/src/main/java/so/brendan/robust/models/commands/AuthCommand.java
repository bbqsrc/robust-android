package so.brendan.robust.models.commands;

import com.google.gson.Gson;

import org.parceler.Parcel;

import so.brendan.robust.models.RobustUser;

@Parcel
public class AuthCommand extends RobustCommand {
    private String type = "auth";
    private String mode;
    private Boolean success;
    private Challenge challenge;
    private Data data;
    private RobustUser user;

    @Parcel
    public static class Challenge {
        private String url;
        private String key;
        private String secret;

        public Challenge() {}

        public Challenge(String key, String secret) {
            this.key = key;
            this.secret = secret;
        }
    }

    @Parcel
    public static class Data {
        private String key;
        private String secret;

        public Data() {}
    }

    public AuthCommand() {}

    public boolean hasSuccess() {
        return success;
    }

    public String getMode() {
        return mode;
    }

    public String getKey() {
        if (data != null) {
            return data.key;
        }

        return null;
    }

    public String getSecret() {
        if (data != null) {
            return data.secret;
        }

        return null;
    }

    public String getChallengeURL() {
        if (challenge != null) {
            return challenge.url;
        }
        return null;
    }

    public RobustUser getUser() {
        return user;
    }

    public static AuthCommand fromJSON(String json) {
        return new Gson().fromJson(json, AuthCommand.class);
    }

    public static AuthCommand createChallenge(String mode) {
        return new Builder().setMode(mode).build();
    }

    public static AuthCommand createChallenge(String mode, String key, String secret) {
        return new Builder().setMode(mode).setChallenge(key, secret).build();
    }

    public static class Builder {
        private AuthCommand mCommand;

        public Builder() {
            mCommand = new AuthCommand();
        }

        public Builder setMode(String mode) {
            mCommand.mode = mode;
            return this;
        }

        public Builder setChallenge(String key, String secret) {
            mCommand.challenge = new Challenge(key, secret);
            return this;
        }

        public AuthCommand build() {
            return mCommand;
        }
    }
}
