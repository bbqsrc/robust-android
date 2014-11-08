package so.brendan.robust.presenters;

import android.content.Context;
import android.net.Uri;

import com.squareup.otto.Subscribe;

import so.brendan.robust.R;
import so.brendan.robust.models.commands.AuthCommand;
import so.brendan.robust.services.MessengerService;
import so.brendan.robust.utils.BusProvider;
import so.brendan.robust.utils.Constants;
import so.brendan.robust.utils.RobustPreferences;
import so.brendan.robust.views.TwitterLoginView;

/**
 * Implementation of the TwitterLoginPresenter interface.
 */
public class TwitterLoginPresenterImpl implements TwitterLoginPresenter {
    private static final String TAG = Constants.createTag(TwitterLoginPresenterImpl.class);

    private static String PARAM_OAUTH_VERIFIER = "oauth_verifier";
    private static String PARAM_MODE_TWITTER = "twitter";

    private TwitterLoginView mView;
    private RobustPreferences mPreferences;

    public TwitterLoginPresenterImpl(TwitterLoginView view) {
        mView = view;

        mPreferences = RobustPreferences.getInstance((Context)mView);
    }

    @Override
    public void start() {
        mView.showLoadingDialog(((Context)mView).getResources().
                getString(R.string.twitter_loading_intro));

        requestChallengeURL();
    }

    @Override
    public void finish() {
        BusProvider.getInstance().unregister(this);
    }

    private void requestChallengeURL() {
        BusProvider.getInstance().register(this);

        MessengerService.startSession((Context) mView, PARAM_MODE_TWITTER);
    }

    @Override
    public void validateURL(String url) {
        Uri uri = Uri.parse(url);

        if (uri.getQueryParameterNames().contains(PARAM_OAUTH_VERIFIER)) {
            mView.showLoadingDialog(((Context)mView).getResources().
                    getString(R.string.twitter_loading_outro));
        }
    }

    @Subscribe
    public void onAuth(AuthCommand command) {
        if (command.hasSuccess()) {
            mPreferences.setAuthentication(PARAM_MODE_TWITTER,
                    command.getKey(),
                    command.getSecret());
            mView.hideLoadingDialog();
            mView.navigateToMain();
        } else {
            mView.openWebView(command.getChallengeURL());
        }
    }
}
