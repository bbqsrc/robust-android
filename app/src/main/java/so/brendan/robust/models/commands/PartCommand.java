package so.brendan.robust.models.commands;

import com.google.gson.Gson;

import org.parceler.Parcel;

/**
 * File is included as part of the Robust project.
 *
 * Copyright (c) 2014 Brendan Molloy
 */
@Parcel
public class PartCommand extends RobustCommand {
    private String type = "part";
    private String target;

    PartCommand() {}

    public PartCommand(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public static PartCommand fromJSON(String json) {
        return new Gson().fromJson(json, PartCommand.class);
    }
}