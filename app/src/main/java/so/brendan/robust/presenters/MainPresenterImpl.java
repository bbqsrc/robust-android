package so.brendan.robust.presenters;

import android.content.Context;
import android.content.res.Resources;

import so.brendan.robust.R;
import so.brendan.robust.interactors.MainInteractor;
import so.brendan.robust.interactors.MainInteractorImpl;
import so.brendan.robust.models.RobustSessionState;
import so.brendan.robust.models.commands.BacklogCommand;
import so.brendan.robust.models.commands.MessageCommand;
import so.brendan.robust.models.commands.PartCommand;
import so.brendan.robust.services.MessengerService;
import so.brendan.robust.services.RobustSession;
import so.brendan.robust.views.MainView;

/**
 * Implementation of the MainPresenter interface.
 */
public class MainPresenterImpl implements MainPresenter {
    private MainInteractor mInteractor;
    private MainView mView;
    private Context mCtx;
    private Resources mResources;

    public MainPresenterImpl(MainView view) {
        mCtx = (Context)view;
        mResources = mCtx.getResources();
        mView = view;
        mInteractor = new MainInteractorImpl(mCtx, this);
    }

    @Override
    public void start() {
        // Update status message if necessary.
        MessengerService.requestAction(mCtx, MessengerService.ACTION_SESSION_STATE);
    }

    @Override
    public void finish() {
        mInteractor.unregisterListeners();
    }

    @Override
    public void requestBacklog(String target, Long fromTs, Long toTs) {
        mInteractor.requestBacklog(target, fromTs, toTs);
    }

    @Override
    public void messageReceived(MessageCommand command) {
        mView.updateMessages(command);
    }

    @Override
    public void backlogReceived(BacklogCommand command) {
        mView.updateMessages(command);
    }

    @Override
    public void partReceived(PartCommand command) {
        mView.updateMessages(command);
    }

    @Override
    public void sendMessage(String target, String message) {
        MessageCommand msg = new MessageCommand.Builder()
                .setTarget(target)
                .setBody(message)
                .build();

        MessengerService.sendCommand(mCtx, msg);
    }

    @Override
    public void updateStatus(RobustSessionState state) {
        int conn = state.getConnectionState();
        int auth = state.getAuthenticationState();

        if (conn == RobustSession.STATE_CONNECTED && auth == RobustSession.STATE_AUTHENTICATED) {
            mView.hideStatusBar();
            return;
        }

        if (conn == RobustSession.STATE_CONNECTING) {
            mView.setStatusBarText(mResources.getString(R.string.conn_connecting));
        } else if (conn == RobustSession.STATE_DISCONNECTED) {
            mView.setStatusBarText(mResources.getString(R.string.conn_disconnected));
        } else if (auth == RobustSession.STATE_AUTHENTICATING) {
            mView.setStatusBarText(mResources.getString(R.string.conn_authenticating));
        } else if (auth == RobustSession.STATE_NOT_AUTHENTICATED) {
            mView.setStatusBarText(mResources.getString(R.string.conn_not_authenticated));
        } else if (auth == RobustSession.STATE_UNREGISTERED) {
            mView.setStatusBarText(mResources.getString(R.string.conn_no_authenticator));
        }

        mView.showStatusBar();
    }
}
