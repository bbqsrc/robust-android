package so.brendan.robust.models.commands;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.parceler.Parcel;

@Parcel
public class MessageCommand extends RobustCommand implements Comparable<MessageCommand> {
    private transient static final DateTimeFormatter FORMATTER =
            DateTimeFormat.forPattern("HH:mm:ss (d MMM)");

    private String type = "message";
    private String subtype;
    private String id;
    private String body;
    private String target;
    private Long ts;
    private MessageSender from;

    public static MessageCommand fromJSON(String json) {
        return new Gson().fromJson(json, MessageCommand.class);
    }

    MessageCommand() {}

    public int compareTo(@NonNull MessageCommand another) {
        long ts = getTimestamp();
        long otherTs = another.getTimestamp();

        if (ts < otherTs) {
            return -1;
        } else if (ts == otherTs) {
            return 0;
        } else {
            return 1;
        }
    }

    @Parcel
    public static class MessageSender {
        private String id;
        private String handle;
        private String name;

        MessageSender() {}

        public MessageSender(String id, String handle, String name) {
            this.id = id;
            this.handle = handle;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getHandle() {
            return handle;
        }

        public String getName() {
            return name;
        }
    }

    public String getId() { return id; }

    public String getSubtype() { return subtype; }

    public String getBody() {
        return body;
    }

    public String getTarget() {
        return target;
    }

    public String getSenderId() {
        if (from == null) {
            return null;
        }
        return from.id;
    }

    public String getSenderHandle() {
        if (from == null) {
            return null;
        }
        return from.handle;
    }

    public String getSenderName() {
        if (from == null) {
            return null;
        }
        return from.name;
    }

    public long getTimestamp() {
        return ts;
    }

    public String getPrettyTimestamp() {
        long localTs = DateTimeZone.getDefault().convertUTCToLocal(ts);
        return new DateTime(localTs).toString(FORMATTER);
    }

    public static class Builder {
        private MessageCommand mCommand;

        public Builder() {
            mCommand = new MessageCommand();
        }

        public MessageCommand build() {
            return mCommand;
        }

        public Builder setBody(String body) {
            mCommand.body = body;
            return this;
        }

        public Builder setTarget(String target) {
            mCommand.target = target;
            return this;
        }

        public Builder setId(String id) {
            mCommand.id = id;
            return this;
        }

        public Builder setTimestamp(long ts) {
            mCommand.ts = ts;
            return this;
        }

        public Builder setSender(String id, String handle, String name) {
            mCommand.from = new MessageSender(id, handle, name);
            return this;
        }
    }
}
