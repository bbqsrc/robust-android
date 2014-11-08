package so.brendan.robust.presenters;

import android.content.Context;

import java.util.List;

import so.brendan.robust.interactors.LeftMenuInteractor;
import so.brendan.robust.interactors.LeftMenuInteractorImpl;
import so.brendan.robust.views.LeftMenuView;

/**
 * Implementation of the LeftMenuPresenter interface.
 */
public class LeftMenuPresenterImpl implements LeftMenuPresenter {
    private LeftMenuView mView;
    private LeftMenuInteractor mInteractor;

    public LeftMenuPresenterImpl(Context context, LeftMenuView view) {
        mView = view;
        mInteractor = new LeftMenuInteractorImpl(context, this);
    }

    @Override
    public void start() {
        mInteractor.registerListeners();
        mInteractor.requestState();
    }

    @Override
    public void finish() {
        mInteractor.unregisterListeners();
    }

    @Override
    public void setTarget(String target) {
        mView.updateTarget(target);
    }

    @Override
    public void partChannel(String channel) {
        mInteractor.partChannel(channel);
    }

    @Override
    public void setAvailableChannels(List<String> channels) {
        mView.updateChannelList(channels);
    }
}
