package so.brendan.robust.interactors;

import android.content.Context;
import android.util.Log;

import com.squareup.otto.Subscribe;

import so.brendan.robust.models.RobustSessionState;
import so.brendan.robust.models.commands.BacklogCommand;
import so.brendan.robust.models.commands.MessageCommand;
import so.brendan.robust.models.commands.PartCommand;
import so.brendan.robust.presenters.MainPresenter;
import so.brendan.robust.services.MessengerService;
import so.brendan.robust.utils.BusProvider;
import so.brendan.robust.utils.Constants;

/**
 * Implementation of MainInteractor.
 */
public class MainInteractorImpl implements MainInteractor {
    private static final String TAG = Constants.createTag(MainInteractorImpl.class);

    private MainPresenter mPresenter;
    private Context mCtx;

    public MainInteractorImpl(Context context, MainPresenter presenter) {
        mCtx = context;
        mPresenter = presenter;

        registerListeners();
    }
    public void registerListeners() {
        BusProvider.getInstance().register(this);
    }

    public void unregisterListeners() {
        BusProvider.getInstance().unregister(this);
    }

    private void sendBacklogCommand(String target, Long fromTs, Long toTs) {
        BacklogCommand.Builder builder = new BacklogCommand.Builder()
                .setTarget(target);

        if (fromTs != null) {
            builder.setFromDate(fromTs);
        }

        if (toTs != null) {
            builder.setToDate(toTs);
        }

        MessengerService.sendCommand(mCtx, builder.build());
    }
    
    public void requestBacklog(String target, Long fromTs, Long toTs) {
        sendBacklogCommand(target, fromTs, toTs);
    }

    @Subscribe
    public void onMessageReceived(MessageCommand command) {
        Log.d(TAG, "onMessageReceived");

        mPresenter.messageReceived(command);
    }

    @Subscribe
    public void onBacklogReceived(BacklogCommand command) {
        Log.d(TAG, "onBacklogReceived");

        mPresenter.backlogReceived(command);
    }

    @Subscribe
    public void onPartReceive(PartCommand command) {
        mPresenter.partReceived(command);
    }

    @Subscribe
    public void onSessionStateChange(RobustSessionState state) {
        mPresenter.updateStatus(state);
    }
}
