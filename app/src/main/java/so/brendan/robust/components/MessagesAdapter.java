package so.brendan.robust.components;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import so.brendan.robust.R;
import so.brendan.robust.models.RobustUser;
import so.brendan.robust.models.commands.MessageCommand;
import so.brendan.robust.models.commands.UserCommand;
import so.brendan.robust.services.MessengerService;
import so.brendan.robust.utils.BusProvider;
import so.brendan.robust.utils.Constants;

/**
 * An adapter for Robust messages, for use in the main activity view.
 *
 * Asychronously handles getting the display image for the given user.
 */
public class MessagesAdapter extends ArrayAdapter<MessageCommand> {
    private static final String TAG = Constants.createTag(MessagesAdapter.class);

    private static final double HUE_UNIT = 255d / 360d;

    private final LayoutInflater mInflater;

    public MessagesAdapter(Context context, List<MessageCommand> commands) {
        super(context, R.layout.message_item_layout, commands);
        mInflater = LayoutInflater.from(context);
    }

    /**
     * Takes a string, hashes it, takes the first byte and converts it into a fairly unique color
     * for the given string by using it as the hue value for a HSV colour.
     *
     * This should ensure a fairly even distribution of colours, making it easier to distinguish
     * names in the application.
     *
     * Falls back to black if for some reason SHA-1 is unavailable.
     *
     * @param handle
     * @return
     */
    private int getColor(String handle) {
        final int fallbackColor = 0;

        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "No SHA-1!", e);
            return fallbackColor;
        }

        byte[] data = digest.digest(handle.getBytes());

        int hue = (int) ((double) (data[0] & 0xFF) * HUE_UNIT);
        return Color.HSVToColor(new float[] { hue, 1f, 0.9f });
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view == null) {
            view = mInflater.inflate(R.layout.message_item_layout, parent, false);

            holder = new ViewHolder();
            holder.userImage = (ImageView) view.findViewById(R.id.userImage);
            holder.handle = (TextView) view.findViewById(R.id.handle);
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.timestamp = (TextView) view.findViewById(R.id.timestamp);
            holder.body = (TextView) view.findViewById(R.id.body);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        MessageCommand msg = getItem(position);

        if (holder.userImage != null) {
            String id = msg.getSenderId();

            RobustUser user;

            // ARE YOUR THREADS SAFE? MINE ARE.
            synchronized (MessengerService.getUserCache()) {
                user = MessengerService.getUserCache().get(id);
            }

            if (user != null) {
                if (user.getDisplayPictureURL() != null) {
                    Picasso.with(getContext()).load(
                            user.getDisplayPictureURL()).into(holder.userImage);
                }
            } else {
                UserCommandReceiver.add(new PendingUserRequest(
                        getContext(), holder, msg.getSenderId()));
            }
        }

        if (holder.handle != null) {
            String handle = msg.getSenderHandle();
            if (handle != null) {
                holder.handle.setText("@" + handle);
                int color = getColor(handle);

                Log.v(TAG, String.format("Color: [%s] #%06X", handle, color & 0x00FFFFFF));

                holder.handle.setTextColor(color);
            }
        }

        if (holder.name != null) holder.name.setText(msg.getSenderName());
        if (holder.timestamp != null) holder.timestamp.setText(msg.getPrettyTimestamp());
        if (holder.body != null) holder.body.setText(msg.getBody());

        return view;
    }

    private static class ViewHolder {
        ImageView userImage;
        TextView handle;
        TextView name;
        TextView timestamp;
        TextView body;
    }

    /**
     * Handles pending user requests asynchronously.
     */
    private static class PendingUserRequest {
        private Context mContext;
        private ViewHolder mHolder;
        private String mId;

        PendingUserRequest(Context context, ViewHolder holder, String id) {
            mContext = context;
            mHolder = holder;
            mId = id;
        }

        public void request() {
            MessengerService.sendCommand(mContext, new UserCommand(mId));
        }

        public void resolve(UserCommand command) {
            if (command.getUser().getDisplayPictureURL() != null) {
                Picasso.with(mContext).load(command.getUser()
                        .getDisplayPictureURL()).into(mHolder.userImage);
            }
        }

        public String getId() {
            return mId;
        }
    }

    /**
     * Ensures that only one request for a user object is sent to the server regardless of how many
     * PendingUserRequest objects need the result.
     */
    private static class UserCommandReceiver {
        private static final UserCommandReceiver sInstance = new UserCommandReceiver();

        private final HashMap<String, Stack<PendingUserRequest>> mRequests =
                new HashMap<String, Stack<PendingUserRequest>>();

        private UserCommandReceiver() {
            BusProvider.getInstance().register(this);
        }

        public static void add(PendingUserRequest req) {
            String id = req.getId();

            if (!sInstance.mRequests.containsKey(id)) {
                sInstance.mRequests.put(id, new Stack<PendingUserRequest>());
            }

            Stack<PendingUserRequest> stack = sInstance.mRequests.get(id);
            stack.add(req);

            if (stack.size() == 1) {
                Log.d(TAG, String.format("firing request for user id %s", id));
                req.request();
            } else {
                Log.v(TAG, String.format("already fired request for user id %s", id));
            }
        }

        @Subscribe
        public void onUserCommand(UserCommand command) {
            String userId = command.getUser().getId();

            Log.d(TAG, String.format("received command for user id %s", userId));

            if (mRequests.containsKey(userId)) {
                Stack<PendingUserRequest> stack = mRequests.get(userId);

                while (!stack.isEmpty()) {
                    stack.pop().resolve(command);
                }
            }
        }
    }
}
