package so.brendan.robust.models.commands;

import com.google.gson.Gson;

import org.parceler.Parcel;

import so.brendan.robust.models.RobustUser;

@Parcel
public class UserCommand extends RobustCommand {
    private String type = "user";
    private String id;
    private RobustUser user;

    public UserCommand() {}

    public UserCommand(String id) {
        this.id = id;
    }

    public static UserCommand fromJSON(String json) {
        return new Gson().fromJson(json, UserCommand.class);
    }

    public RobustUser getUser() {
        return user;
    }
}
