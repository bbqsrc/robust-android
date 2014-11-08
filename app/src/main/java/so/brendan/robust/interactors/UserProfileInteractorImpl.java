package so.brendan.robust.interactors;

import android.content.Context;
import android.util.Log;

import com.squareup.otto.Subscribe;

import so.brendan.robust.models.RobustSessionState;
import so.brendan.robust.models.RobustUser;
import so.brendan.robust.models.commands.UserCommand;
import so.brendan.robust.presenters.UserProfilePresenter;
import so.brendan.robust.services.MessengerService;
import so.brendan.robust.utils.BusProvider;
import so.brendan.robust.utils.Constants;

/**
 * An implementation of UserProfileInteractor.
 */
public class UserProfileInteractorImpl implements UserProfileInteractor {
    private static final String TAG = Constants.createTag(UserProfileInteractorImpl.class);

    private UserProfilePresenter mPresenter;
    private Context mCtx;

    public UserProfileInteractorImpl(Context context, UserProfilePresenter presenter) {
        mCtx = context;
        mPresenter = presenter;
    }

    public void registerListeners() {
        BusProvider.getInstance().register(this);
    }

    public void unregisterListeners() {
        BusProvider.getInstance().unregister(this);
    }

    public void requestSessionState() {
        MessengerService.requestAction(mCtx, MessengerService.ACTION_SESSION_STATE);
    }

    public void requestUser(String id) {
        MessengerService.sendCommand(mCtx, new UserCommand(id));
    }

    @Subscribe
    public void receiveCurrentUser(RobustSessionState state) {
        Log.d(TAG, "setCurrentUser event!");
        RobustUser user = state.getUser();
        mPresenter.setUser(user);
    }

    @Subscribe
    public void receiveOtherUser(UserCommand command) {
        Log.d(TAG, "setOtherUser event!");
        RobustUser user = command.getUser();
        mPresenter.setUser(user);
    }
}
