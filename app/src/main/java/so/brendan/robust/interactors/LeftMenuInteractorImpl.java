package so.brendan.robust.interactors;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import so.brendan.robust.models.RobustSessionState;
import so.brendan.robust.models.RobustUser;
import so.brendan.robust.models.commands.JoinCommand;
import so.brendan.robust.models.commands.PartCommand;
import so.brendan.robust.presenters.LeftMenuPresenter;
import so.brendan.robust.services.MessengerService;
import so.brendan.robust.utils.BusProvider;
import so.brendan.robust.utils.Constants;

/**
 * Implementation of LeftMenuInteractor.
 */
public class LeftMenuInteractorImpl implements LeftMenuInteractor {
    private static final String TAG = Constants.createTag(LeftMenuInteractorImpl.class);

    private Context mCtx;
    private LeftMenuPresenter mPresenter;
    private ArrayList<String> mChannelCache;

    public LeftMenuInteractorImpl(Context context, LeftMenuPresenter presenter) {
        mPresenter = presenter;
        mCtx = context;
        mChannelCache = new ArrayList<String>();
    }

    @Override
    public void registerListeners() {
        BusProvider.getInstance().register(this);
    }

    public void requestState() {
        MessengerService.requestAction(mCtx, MessengerService.ACTION_SESSION_STATE);
    }

    @Override
    public void unregisterListeners() {
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void partChannel(String channel) {
        MessengerService.sendCommand(mCtx, new PartCommand(channel));
    }

    @Subscribe
    public void onSessionState(RobustSessionState state) {
        RobustUser user = state.getUser();
        if (user == null) {
            Log.e(TAG, "no user supplied!");
            return;
        }

        List<String> channels = state.getUser().getChannels();
        if (channels == null) {
            Log.e(TAG, "no channels in user!");
            return;
        }

        Log.d(TAG, TextUtils.join(", ", channels));

        if (!mChannelCache.containsAll(channels) || !channels.containsAll(mChannelCache)) {
            mChannelCache.clear();
            mChannelCache.addAll(channels);

            mPresenter.setAvailableChannels(mChannelCache);
        }
    }

    @Subscribe
    public void onJoin(JoinCommand command) {
        mChannelCache.add(command.getTarget());
        mPresenter.setAvailableChannels(mChannelCache);
        mPresenter.setTarget(command.getTarget());
    }

    @Subscribe
    public void onPart(PartCommand command) {
        mChannelCache.remove(command.getTarget());
        mPresenter.setAvailableChannels(mChannelCache);
    }
}
