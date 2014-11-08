package so.brendan.robust.presenters;

import android.content.Context;

import so.brendan.robust.interactors.UserProfileInteractor;
import so.brendan.robust.interactors.UserProfileInteractorImpl;
import so.brendan.robust.models.RobustUser;
import so.brendan.robust.utils.Constants;
import so.brendan.robust.views.UserProfileView;

/**
 * An implementation of UserProfilePresenter.
 */
public class UserProfilePresenterImpl implements UserProfilePresenter {
    private static final String TAG = Constants.createTag(UserProfilePresenterImpl.class);

    private UserProfileView mView;
    private UserProfileInteractor mInteractor;
    private boolean mStarted = false;

    public UserProfilePresenterImpl(UserProfileView view) {
        mView = view;
        mInteractor = new UserProfileInteractorImpl((Context)mView, this);
    }

    @Override
    public void start(String userId) {
        mStarted = true;
        mInteractor.registerListeners();

        if (userId == null) {
            // Let's work out the current user!
            mInteractor.requestSessionState();
        } else {
            mInteractor.requestUser(userId);
        }
    }

    @Override
    public void finish() {
        if (mStarted) {
            mInteractor.unregisterListeners();
            mStarted = false;
        }
    }

    @Override
    public void setUser(RobustUser user) {
        mView.setCurrentUser(user);
    }
}
