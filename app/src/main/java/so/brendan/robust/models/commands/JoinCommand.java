package so.brendan.robust.models.commands;

import com.google.gson.Gson;

import org.parceler.Parcel;

@Parcel
public class JoinCommand extends RobustCommand {
    private String type = "join";
    private String target;

    JoinCommand() {}

    public JoinCommand(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public static JoinCommand fromJSON(String json) {
        return new Gson().fromJson(json, JoinCommand.class);
    }
}
