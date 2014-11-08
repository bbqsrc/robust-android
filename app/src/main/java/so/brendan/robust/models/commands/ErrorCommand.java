package so.brendan.robust.models.commands;

import com.google.gson.Gson;

import org.parceler.Parcel;

@Parcel
public class ErrorCommand extends RobustCommand {
    private String type = "error";
    private String subtype;
    private String message;

    public String getSubtype() {
        return subtype;
    }

    public String getMessage() {
        return message;
    }

    ErrorCommand() {}

    public static ErrorCommand fromJSON(String json) {
        return new Gson().fromJson(json, ErrorCommand.class);
    }
}
