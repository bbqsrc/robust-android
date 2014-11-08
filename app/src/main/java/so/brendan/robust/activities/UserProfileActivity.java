package so.brendan.robust.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import so.brendan.robust.R;
import so.brendan.robust.fragments.RetainFragment;
import so.brendan.robust.models.RobustUser;
import so.brendan.robust.presenters.UserProfilePresenter;
import so.brendan.robust.presenters.UserProfilePresenterImpl;
import so.brendan.robust.utils.Constants;
import so.brendan.robust.utils.RobustPreferences;
import so.brendan.robust.views.UserProfileView;

/**
 * Implementation of the UserProfileView.
 */
public class UserProfileActivity extends Activity implements UserProfileView {
    private static final String TAG = Constants.createTag(UserProfileActivity.class);

    private static final String RETAIN_TAG = TAG + ".retain";
    private static final String PARAM_USER_ID = "user_id";

    private ImageView mDisplayPicture;
    private TextView mNameText;
    private TextView mHandleText;
    private TextView mBioText;
    private TextView mLocationText;

    private UserProfilePresenter mPresenter;
    private RetainFragment<RobustUser> mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mDisplayPicture = (ImageView) findViewById(R.id.displayPicture);
        mNameText = (TextView) findViewById(R.id.fullName);
        mHandleText = (TextView) findViewById(R.id.handle);
        mBioText = (TextView) findViewById(R.id.bio);
        mLocationText = (TextView) findViewById(R.id.location);

        mFragment = RetainFragment.findOrCreate(getFragmentManager(), RETAIN_TAG);
        mPresenter = new UserProfilePresenterImpl(this);

        RobustUser user = mFragment.getValue();
        Log.d(TAG, "User is null? " + (user == null));

        if (user != null) {
            setCurrentUser(user);
        } else {
            mPresenter.start(getUserId());
        }
    }

    private String getUserId() {
        return getIntent().getStringExtra(PARAM_USER_ID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPresenter.finish();
    }

    public void setHandle(String handle) {
        mHandleText.setText("@" + handle);
    }

    public void setName(String name) {
        mNameText.setText(name);
    }

    public void setBio(String bio) {
        mBioText.setText(bio);
    }

    public void setDisplayPicture(Bitmap picture) {
        mDisplayPicture.setImageBitmap(picture);
    }

    public void setLocation(String location) {
        mLocationText.setText(location);
    }

    @Override
    public void setCurrentUser(RobustUser user) {
        mFragment.setValue(user);

        setName(user.getName());
        setHandle(user.getHandle());
        setBio(user.getBio());
        setLocation(user.getLocation());

        if (RobustPreferences.getInstance(this).hasLowBandwidthImages()) {
            Picasso.with(this).load(user.getDisplayPictureURL()).into(mDisplayPicture);
        } else {
            Picasso.with(this).load(user.getLargeDisplayPictureURL()).into(mDisplayPicture);
        }
    }
}
