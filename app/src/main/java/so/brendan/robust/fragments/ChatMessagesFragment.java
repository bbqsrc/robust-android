package so.brendan.robust.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import so.brendan.robust.R;
import so.brendan.robust.components.MessagesAdapter;
import so.brendan.robust.components.RefreshableListView;
import so.brendan.robust.db.DatabaseHelper;
import so.brendan.robust.listeners.OnOverScrolledListener;
import so.brendan.robust.models.commands.BacklogCommand;
import so.brendan.robust.models.commands.MessageCommand;
import so.brendan.robust.utils.Constants;
import so.brendan.robust.views.ChatMessagesView;

public class ChatMessagesFragment extends Fragment implements ChatMessagesView, OnOverScrolledListener {
    private static final String TAG = Constants.createTag(ChatMessagesFragment.class);

    private View mContent;
    private RefreshableListView mListView;
    private MessagesAdapter mAdapter;
    private ArrayList<MessageCommand> mMessages;

    private OnChatMessagesEvent mListener;
    private String mTarget;

    private boolean mPendingOverscrollRequest = false;
    private boolean mBlockOverscrollRequests = false;

    public ChatMessagesFragment() {}

    @Nullable
    @Override
    public View getView() {
        return mContent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMessages = new ArrayList<MessageCommand>();
        mAdapter = new MessagesAdapter(getActivity(), mMessages);

        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContent = inflater.inflate(R.layout.fragment_chat_messages, container);

        mListView = (RefreshableListView) mContent.findViewById(R.id.listView);
        initListView();

        return mContent;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnChatMessagesEvent) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format("%s must implement %s.",
                    activity.toString(), OnChatMessagesEvent.class.getSimpleName()));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView.setOnCreateContextMenuListener(this);
    }

    /**
     * Creates the context menu for messages.
     *
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d(TAG, "onCreateContextMenu");

        if (v.getId() == mListView.getId()) {
            getActivity().getMenuInflater().inflate(R.menu.context_main, menu);

            int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
            MessageCommand command = getItem(position);

            menu.setHeaderTitle(String.format("@%s: %s",
                    command.getSenderHandle(), command.getBody()));
        }
    }

    public MessageCommand getItem(int position) {
        return (MessageCommand) mListView.getItemAtPosition(position);
    }

    public void scrollToBottom() {
        mListView.setSelection(mAdapter.getCount() - 1);
    }

    public void setTarget(String target) {
        mTarget = target;

        long fromTs = DatabaseHelper.Messages.getNewestTimestamp(getActivity(), mTarget);
        mListener.requestBacklogFrom(fromTs + 1);

        new AsyncUpdateAdapter().execute();
        mListener.onSetTargetStarted();
    }

    public String getTarget() {
        return mTarget;
    }

    public void insertMessages(BacklogCommand command) {
        List<MessageCommand> messages = command.getMessages();

        if (!messages.isEmpty()) {
            mMessages.addAll(messages);
            Collections.sort(mMessages);
            mAdapter.notifyDataSetChanged();
        }

        if (mPendingOverscrollRequest) {
            mPendingOverscrollRequest = false;

            if (messages.isEmpty()) {
                mBlockOverscrollRequests = true;
            } else {
                // Scroll up just one to show there's results.
                mListView.setSelection(command.getMessages().size());
                mListView.smoothScrollToPosition(Math.max(0, command.getMessages().size() - 1));
            }

            mListener.onBacklogReceived();
        }
    }

    public void insertMessage(MessageCommand command) {
        mMessages.add(command);
        Collections.sort(mMessages);
        mAdapter.notifyDataSetChanged();
    }

    public void clear() {
        mMessages.clear();
        mAdapter.notifyDataSetChanged();
        mBlockOverscrollRequests = false;
        mTarget = null;
    }

    /**
     * Initialises the list view.
     */
    private void initListView() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MessageCommand msg = (MessageCommand) mListView.getItemAtPosition(position);
                mListener.onMessageSelected(msg);
            }
        });

        mListView.setOnOverScrolledListener(this);

        mListView.setAdapter(mAdapter);
    }

    /**
     * Listener for overscroll events in the message list.
     *
     * @param scrollX
     * @param scrollY
     * @param clampedX
     * @param clampedY
     */
    @Override
    public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (mBlockOverscrollRequests || mPendingOverscrollRequest) {
            return;
        }

        // Check for refresh trigger
        if (scrollY < -5) {
            Log.d(TAG, "onOverScrolled");
            if (!mMessages.isEmpty()) {
                mListener.requestBacklogTo(mMessages.get(0).getTimestamp() - 1);
                mPendingOverscrollRequest = true;
            }
        }
    }

    /**
     * Asynchronously grabs the backlog for the given target and displays it in the list.
     */
    private class AsyncUpdateAdapter extends AsyncTask<Void, Void, List<MessageCommand>> {
        @Override
        protected List<MessageCommand> doInBackground(Void... params) {
            return DatabaseHelper.Messages.getTargetAsList(getActivity(), mTarget);
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, String.format("Cancelled: %s",
                    AsyncUpdateAdapter.class.getSimpleName()));
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(List<MessageCommand> messageCommands) {
            mMessages.clear();
            mMessages.addAll(messageCommands);
            mAdapter.notifyDataSetChanged();
            mListener.onSetTargetFinished();
        }
    }

    /**
     * Interface for passing messages to the parent view.
     */
    public interface OnChatMessagesEvent {
        public void onSetTargetStarted();
        public void onSetTargetFinished();
        public void onMessageSelected(MessageCommand command);
        public void requestBacklogTo(long ts);
        public void requestBacklogFrom(long ts);
        public void onBacklogReceived();
    }
}
