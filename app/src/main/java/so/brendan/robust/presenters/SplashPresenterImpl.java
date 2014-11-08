package so.brendan.robust.presenters;

import android.content.Context;

import so.brendan.robust.R;
import so.brendan.robust.interactors.SplashInteractor;
import so.brendan.robust.interactors.SplashInteractorImpl;
import so.brendan.robust.utils.RobustPreferences;
import so.brendan.robust.views.SplashView;

/**
 * Implementation of the SplashPresenter interface.
 */
public class SplashPresenterImpl implements SplashPresenter {
    private SplashView mView;
    private Context mCtx;
    private SplashInteractor mInteractor;

    public SplashPresenterImpl(SplashView view) {
        mView = view;
        mCtx = (Context) view;
        mInteractor = new SplashInteractorImpl(mCtx, this);
    }

    @Override
    public void start() {
        if (!RobustPreferences.getInstance(mCtx).hasValidServerSettings()) {
            mView.navigateToServerPreferences();
            mView.showError(mCtx.getResources()
                    .getString(R.string.message_no_server_settings));
            return;
        }

        mInteractor.attemptToInitSession();
    }

    @Override
    public void finish() {
        mInteractor.unregisterListeners();
    }

    @Override
    public void loginRequired() {
        mView.navigateToLogin();
    }

    @Override
    public void loginSuccessful() {
        mView.navigateToMain(false);
    }

    @Override
    public void connecting() {
        mView.showConnecting();
    }

    @Override
    public void authenticating() {
        mView.showAuthenticating();
    }

    @Override
    public void connectionFailed(String reason) {
        mView.showError(reason);
    }

    @Override
    public void alreadyAuthenticated() {
        mView.navigateToMain(true);
    }
}
