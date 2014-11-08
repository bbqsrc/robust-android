package so.brendan.robust.interactors;

import android.content.Context;
import android.util.Log;

import com.squareup.otto.Subscribe;

import so.brendan.robust.models.RobustSessionState;
import so.brendan.robust.presenters.SplashPresenter;
import so.brendan.robust.services.MessengerService;
import so.brendan.robust.utils.BusProvider;
import so.brendan.robust.utils.Constants;

/**
 * An implementation of SplashInteractor.
 */
public class SplashInteractorImpl implements SplashInteractor {
    private static final String TAG = Constants.createTag(SplashInteractorImpl.class);

    private boolean mRegistered;
    private Context mCtx;
    private SplashPresenter mPresenter;

    private boolean mFirstCheck = true;

    public SplashInteractorImpl(Context ctx, SplashPresenter presenter) {
        mCtx = ctx;
        mPresenter = presenter;
    }

    public void attemptToInitSession() {
        Log.d(TAG, "attemptToInitSession");

        mPresenter.connecting();

        registerListeners();

        MessengerService.startDefaultSession(mCtx);
    }

    @Override
    public void registerListeners() {
        if (!mRegistered) {
            mRegistered = true;
            BusProvider.getInstance().register(this);
        }
    }

    @Override
    public void unregisterListeners() {
        if (mRegistered) {
            mRegistered = false;
            BusProvider.getInstance().unregister(this);
        }
    }

    @Subscribe
    public void onSessionState(RobustSessionState state) {
        Log.d(TAG, state.toString());

        if (mFirstCheck) {
            mFirstCheck = false;

            if (state.isConnected() && state.isAuthenticated()) {
                unregisterListeners();
                mPresenter.alreadyAuthenticated();
                return;
            }
        }

        if (state.isAuthenticated()) {
            unregisterListeners();
            mPresenter.loginSuccessful();
        }

        if (state.requiresLogin()) {
            unregisterListeners();
            mPresenter.loginRequired();
        }
    }
}
