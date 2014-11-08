package so.brendan.robust.models.commands;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A base model for holding commands received from the Robust server.
 *
 * It is expected that subclasses will implement a static <code>fromJSON</code>
 * method for conveniently creating a command from a JSON string.
 */
public abstract class RobustCommand {
    public static transient final String PARAM_TYPE = "type";

    public static transient final String TYPE_AUTH = "auth";
    public static transient final String TYPE_MESSAGE = "message";
    public static transient final String TYPE_BACKLOG = "backlog";
    public static transient final String TYPE_PING = "ping";
    public static transient final String TYPE_PONG = "pong";
    public static transient final String TYPE_ERROR = "error";
    public static transient final String TYPE_JOIN = "join";
    public static transient final String TYPE_PART = "part";
    public static transient final String TYPE_USER = "user";

    /**
     * Creates an empty command with a type field.
     *
     * @param type
     * @return
     */
    private static RobustCommand createEmptyWithType(final String type) {
        return new RobustCommand() {
            @Override
            public String toJSON() {
                JSONObject o = new JSONObject();
                try {
                    o.put(PARAM_TYPE, type);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return o.toString();
            }
        };
    }

    /**
     * Creates a "ping" command.
     *
     * @return
     */
    public static RobustCommand createPing() {
        return createEmptyWithType(TYPE_PING);
    }

    /**
     * Creates a "pong" command.
     *
     * @return
     */
    public static RobustCommand createPong() {
        return createEmptyWithType(TYPE_PONG);
    }

    /**
     * Serialises the object to JSON.
     *
     * @return
     */
    public String toJSON() {
        return new Gson().toJson(this);
    }

    public String toString() {
        return toJSON();
    }
}
