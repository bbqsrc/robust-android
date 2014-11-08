package so.brendan.robust.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.parceler.Parcels;

import so.brendan.robust.R;
import so.brendan.robust.fragments.ChatMessagesFragment;
import so.brendan.robust.fragments.LeftMenuFragment;
import so.brendan.robust.models.commands.BacklogCommand;
import so.brendan.robust.models.commands.MessageCommand;
import so.brendan.robust.models.commands.PartCommand;
import so.brendan.robust.presenters.MainPresenter;
import so.brendan.robust.presenters.MainPresenterImpl;
import so.brendan.robust.services.MessengerService;
import so.brendan.robust.utils.Constants;
import so.brendan.robust.utils.RobustPreferences;
import so.brendan.robust.views.ChatMessagesView;
import so.brendan.robust.views.MainView;

/**
 * The main activity. Handles the interaction between the relevant drawer fragments, the chat box,
 * the messages list view, and other components for the purpose of handling live messaging.
 */
public class MainActivity extends Activity implements MainView, View.OnClickListener,
        LeftMenuFragment.OnTargetSelectedListener, ChatMessagesFragment.OnChatMessagesEvent {
    private static final String TAG = Constants.createTag(MainActivity.class);

    private static final String PARAM_USER_ID = "user_id";
    private static final String PARAM_TARGET = "target";
    private static final String PARAM_CHAR_SEQ_LABEL = TAG;

    private MainPresenter mPresenter;

    private ImageButton mSendBtn;
    private EditText mMessageField;
    private DrawerLayout mDrawerLayout;
    private View mLeftDrawer;
    private View mStatusBar;
    private TextView mStatusBarText;
    private ChatMessagesView mChatMessages;

    private String mSelectedTarget;
    private RobustPreferences mPreferences;

    private ActionBar mActionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private ProgressDialog mProgress;

    /**
     * A convenience method for handling instance state regardless of which event it is received
     * from.
     *
     * @param savedInstanceState
     */
    private void handleSavedInstanceState(Bundle savedInstanceState) {
        Intent intent = getIntent();

        // Check for intent trigger from notification.
        if (intent.hasExtra(MessengerService.EXTRA_COMMAND)) {
            MessageCommand command = Parcels.unwrap(
                    intent.getParcelableExtra(MessengerService.EXTRA_COMMAND));
            mSelectedTarget = command.getTarget();

            return;
        }

        if (savedInstanceState != null) {
            mSelectedTarget = savedInstanceState.getString(PARAM_TARGET,
                    mPreferences.getLastUsedTarget());
        } else {
            mSelectedTarget = mPreferences.getLastUsedTarget();
        }
    }

    /**
     * Initialises the menu drawer.
     */
    private void initMenuDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (mDrawerLayout == null) {
            return;
        }

        mLeftDrawer = findViewById(R.id.left_drawer);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, 0, 0);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
    }

    /**
     * Disables notifications and clears them if they exist while application is focused.
     */
    @Override
    protected void onResume() {
        super.onResume();

        MessengerService.disableNotifications();
        MessengerService.clearNotifications(this);
    }

    /**
     * Initialise the activity, its views, the drawer, assigns handlers and registers context menus.
     *
     * Most of the business logic is handled in the presenter.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = RobustPreferences.getInstance(this);
        handleSavedInstanceState(savedInstanceState);

        setContentView(R.layout.activity_main);

        mActionBar = getActionBar();
        mMessageField = (EditText) findViewById(R.id.messageFld);
        mStatusBar = findViewById(R.id.statusBar);
        mStatusBarText = (TextView) findViewById(R.id.statusText);
        mSendBtn = (ImageButton) findViewById(R.id.sendBtn);
        mChatMessages = (ChatMessagesView) getFragmentManager()
                .findFragmentById(R.id.fragment_chat_messages);

        mSendBtn.setOnClickListener(this);
        initMenuDrawer();

        mPresenter = new MainPresenterImpl(this);
        mPresenter.start();

        // Opens the drawer if no default target or no target available.
        if (mSelectedTarget != null) {
            updateSelectedTarget(mSelectedTarget);
        } else {
            mMessageField.setEnabled(false);
            mDrawerLayout.openDrawer(mLeftDrawer);
        }
    }

    /**
     * Creates the options menu.
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handles item selection.
     *
     * @param featureId
     * @param item
     * @return
     */
    @Override
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connection_status: {
                startActivity(new Intent(this, ConnectionStatusActivity.class));
                return true;
            }
            case R.id.action_user_profile: {
                startActivity(new Intent(this, UserProfileActivity.class));
                return true;
            }
            case R.id.action_reconnect: {
                MessengerService.restartDefaultSession(this);
                return true;
            }
            case R.id.action_settings: {
                startActivity(new Intent(this, MainPreferencesActivity.class));
                return true;
            }
        }

        return super.onMenuItemSelected(featureId, item);
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
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    /**
     * Handles a context menu item being selected.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (menuInfo == null) {
            Log.d(TAG, "menuInfo is null!");
            return super.onContextItemSelected(item);
        }

        MessageCommand msg = mChatMessages.getItem(menuInfo.position);
        Log.d(TAG, String.format("Selected item: %s %s", msg.getSenderId(), msg.getSenderHandle()));

        switch (item.getItemId()) {
            case R.id.action_copy_message: {
                ClipboardManager clipboardManager =
                        (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                ClipData clip = ClipData.newPlainText(PARAM_CHAR_SEQ_LABEL, msg.getBody());
                clipboardManager.setPrimaryClip(clip);
                break;
            }
            case R.id.action_view_user_profile: {
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra(PARAM_USER_ID, msg.getSenderId());
                startActivity(intent);
                break;
            }
        }

        return super.onContextItemSelected(item);
    }

    /**
     * Synchronises the drawer toggle states after activity creation events.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerLayout != null) {
            mDrawerToggle.syncState();
        }
    }

    /**
     * Passes through the configuration change events to the toggle drawer.
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerLayout != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    /**
     * Pass the event to ActionBarDrawerToggle, if it returns
     * true, then it has handled the app icon touch event.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle returning to the last selected channel.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        handleSavedInstanceState(savedInstanceState);

        if (mSelectedTarget != null) {
            updateSelectedTarget(mSelectedTarget);
        }
    }

    /**
     * Disable notifications when leaving main activity for any reason.
     */
    @Override
    protected void onPause() {
        super.onPause();
        MessengerService.enableNotifications();
    }

    /**
     * Handle destroy.
     */
    @Override
    protected void onDestroy() {
        if (mProgress != null) {
            mProgress.dismiss();
        }
        mPresenter.finish();
        super.onDestroy();
    }

    /**
     * Save last target.
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");

        outState.putString(PARAM_TARGET, mSelectedTarget);

        super.onSaveInstanceState(outState);
    }

    /**
     * Handle on click events, particularly the send button event.
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendBtn: {
                String msg = mMessageField.getText().toString().trim();

                if (msg.length() == 0) {
                    return;
                }

                mPresenter.sendMessage(mSelectedTarget, msg);

                mChatMessages.scrollToBottom();
                mMessageField.setText("");

                return;
            }
        }
    }

    /**
     * Update the view when a new target is selected.
     *
     * @param target
     */
    @Override
    public void updateSelectedTarget(String target) {
        mActionBar.setTitle(target);
        mSelectedTarget = target;
        mPreferences.setLastUsedTarget(target);

        Log.d(TAG, String.format("updateSelectedTarget: %s %s", target, mChatMessages.getTarget()));

        if (!target.equals(mChatMessages.getTarget())) {
            mChatMessages.setTarget(target);
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mLeftDrawer);
        }
    }

    /**
     * Clears the screen and related state for when no channel is selected.
     */
    public void clearScreen() {
        mActionBar.setTitle("");
        mSelectedTarget = null;
        mMessageField.setEnabled(false);
        mPreferences.setLastUsedTarget(null);
        mChatMessages.clear();
    }

    /**
     * Shows the loading dialog.
     */
    @Override
    public void showLoadingDialog() {
        if (mProgress == null) {
            mProgress = new ProgressDialog(this);
        }

        mProgress.setTitle("Loading " + mSelectedTarget);
        mProgress.setMessage("Please wait...");
        mProgress.show();
    }

    /**
     * Hides the loading dialog.
     */
    @Override
    public void hideLoadingDialog() {
        if (mProgress == null) {
            return;
        }

        mProgress.dismiss();
        mProgress = null;
    }

    /**
     * Shows the status bar.
     */
    @Override
    public void showStatusBar() {
        mStatusBar.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the status bar.
     */
    @Override
    public void hideStatusBar() {
        mStatusBar.setVisibility(View.GONE);
    }

    /**
     * Sets the status bar text.
     *
     * @param text
     */
    @Override
    public void setStatusBarText(String text) {
        mStatusBarText.setText(text);
    }

    @Override
    public void updateMessages(MessageCommand command) {
        if (command.getTarget().equals(mSelectedTarget)) {
            mChatMessages.insertMessage(command);
        }
    }

    @Override
    public void updateMessages(BacklogCommand command) {
        if (command.getTarget().equals(mSelectedTarget)) {
            mChatMessages.insertMessages(command);
        }
        onBacklogReceived();
    }

    @Override
    public void updateMessages(PartCommand command) {
        if (command.getTarget().equals(mSelectedTarget)) {
            mChatMessages.clear();
        }
    }

    @Override
    public void onSetTargetStarted() {
        showLoadingDialog();
        mMessageField.setEnabled(false);
    }

    @Override
    public void onSetTargetFinished() {
        hideLoadingDialog();
        mMessageField.setEnabled(true);
    }

    @Override
    public void onMessageSelected(MessageCommand command) {
        Editable text = mMessageField.getText();
        int len = text.length();

        text.append("@")
                .append(command.getSenderHandle())
                .append(len == 0 ? ": " : " ");
    }

    @Override
    public void requestBacklogTo(long ts) {
        setStatusBarText(getString(R.string.message_backlog));
        showStatusBar();
        mPresenter.requestBacklog(mSelectedTarget, null, ts);
    }

    @Override
    public void requestBacklogFrom(long ts) {
        setStatusBarText(getString(R.string.message_backlog));
        showStatusBar();
        mPresenter.requestBacklog(mSelectedTarget, ts, null);
    }

    @Override
    public void onBacklogReceived() {
        hideStatusBar();
    }
}
