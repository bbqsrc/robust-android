package so.brendan.robust.models.commands;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class BacklogCommand extends RobustCommand {
    private String type = "backlog";
    private String target;
    @SerializedName("from_date") private Long fromDate;
    @SerializedName("to_date") private Long toDate;
    private Integer count;
    private List<MessageCommand> messages;

    public static BacklogCommand fromJSON(String json) {
        return new Gson().fromJson(json, BacklogCommand.class);
    }

    public static BacklogCommand sinceTimestamp(long ts) {
        BacklogCommand command = new BacklogCommand();
        command.fromDate = ts;
        return command;
    }

    public static BacklogCommand beforeTimestamp(long ts) {
        BacklogCommand command = new BacklogCommand();
        command.toDate = ts;
        return command;
    }

    public static BacklogCommand betweenTimestamp(long fromTs, long toTs) {
        BacklogCommand command = new BacklogCommand();
        command.fromDate = fromTs;
        command.toDate = toTs;
        return command;
    }

    BacklogCommand() {}

    public Long getFromDate() {
        return fromDate;
    }

    public Long getToDate() {
        return toDate;
    }

    public Integer getCount() {
        return count;
    }

    public String getTarget() {
        return target;
    }

    public List<MessageCommand> getMessages() {
        return messages;
    }

    public static class Builder {
        private BacklogCommand mCommand;

        public Builder() {
            mCommand = new BacklogCommand();
        }

        public Builder setFromDate(long ts) {
            mCommand.fromDate = ts;
            return this;
        }

        public Builder setToDate(long ts) {
            mCommand.toDate = ts;
            return this;
        }

        public Builder setCount(int c) {
            mCommand.count = c;
            return this;
        }

        public Builder setTarget(String target) {
            mCommand.target = target;
            return this;
        }

        public BacklogCommand build() {
            return mCommand;
        }
    }
}
