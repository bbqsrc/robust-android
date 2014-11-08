package so.brendan.robust.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import so.brendan.robust.R;
import so.brendan.robust.components.MenuButton;
import so.brendan.robust.models.commands.JoinCommand;
import so.brendan.robust.presenters.LeftMenuPresenter;
import so.brendan.robust.presenters.LeftMenuPresenterImpl;
import so.brendan.robust.services.MessengerService;
import so.brendan.robust.utils.Constants;
import so.brendan.robust.views.LeftMenuView;

/**
 * Handles the left menu for the main activity.
 */
public class LeftMenuFragment extends Fragment implements LeftMenuView, View.OnClickListener {
    private static final String TAG = Constants.createTag(LeftMenuFragment.class);
    private static final String TAG_CHANNEL_BUTTON = Constants.createViewTag("CHANNEL_BUTTON");

    private View mContent;
    private OnTargetSelectedListener mListener;
    private LeftMenuPresenter mPresenter;

    private LayoutInflater mInflater;
    private List<String> mChannels;
    private LinearLayout mChannelsView;

    private View mLastContextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContent = inflater.inflate(R.layout.fragment_left_navigation, container);

        mChannelsView = (LinearLayout) mContent.findViewById(R.id.channelsList);
        mChannels = new ArrayList<String>();

        mContent.findViewById(R.id.joinChannelBtn).setOnClickListener(this);

        return mContent;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnTargetSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format("%s must implement %s.",
                    activity.toString(), OnTargetSelectedListener.class.getSimpleName()));
        }

        mPresenter = new LeftMenuPresenterImpl(activity, this);
        mPresenter.start();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mPresenter.finish();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.context_main_left, menu);
        menu.setHeaderTitle(((Button) v).getText());
        mLastContextView = v;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d(TAG, "onContextItemSelected");

        switch (item.getItemId()) {
            case R.id.action_leave_channel: {
                Log.d(TAG, "leave channel");
                mPresenter.partChannel(((Button) mLastContextView).getText().toString());
            }
        }

        mLastContextView = null;

        return super.onContextItemSelected(item);
    }

    @Override
    public void updateChannelList(List<String> channels) {
        mChannels.clear();
        mChannels.addAll(channels);
        Collections.sort(mChannels);

        mChannelsView.removeAllViews();

        for (String channel : mChannels) {
            MenuButton btn = inflateMenuButton(channel);
            // TODO handle specific channel states
            //btn.setBadge("" + 42);
            btn.setTag(TAG_CHANNEL_BUTTON);
            btn.setOnClickListener(this);
            btn.setOnCreateContextMenuListener(this);
            mChannelsView.addView(btn);
        }
    }

    private MenuButton inflateMenuButton(String text) {
        MenuButton btn = (MenuButton) mInflater.inflate(R.layout.button_menu, null);
        btn.setText(text);
        return btn;
    }

    @Override
    public void updateTarget(String target) {
        mListener.updateSelectedTarget(target);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.joinChannelBtn) {
            onClickJoinChannel(v);
            return;
        }

        if (TAG_CHANNEL_BUTTON.equals(v.getTag())) {
            String target = ((Button) v).getText().toString();
            updateTarget(target);
        }
    }

    public void onClickJoinChannel(View v) {
        buildJoinChannelDialog().show();
    }

    private AlertDialog buildJoinChannelDialog() {
        final EditText editText = new EditText(getActivity());

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: {
                        String channel = editText.getText().toString();

                        if (!channel.startsWith("#")) {
                            Toast.makeText(getActivity(), R.string.invalid_channel,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            MessengerService.sendCommand(getActivity(),
                                    new JoinCommand(channel));
                        }
                    }
                }
            }
        };

        return new AlertDialog.Builder(getActivity())
                .setView(editText)
                .setCancelable(true)
                .setTitle(R.string.join_channel)
                .setMessage(R.string.enter_channel_name)
                .setPositiveButton(R.string.join, listener)
                .setNegativeButton(R.string.cancel, listener)
                .create();
    }

    /**
     * A listener interface for events triggered by the LeftMenuFragment.
     */
    public interface OnTargetSelectedListener {
        public void updateSelectedTarget(String target);
    }
}
