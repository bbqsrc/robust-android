package so.brendan.robust.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import so.brendan.robust.R;
import so.brendan.robust.presenters.SplashPresenter;
import so.brendan.robust.presenters.SplashPresenterImpl;
import so.brendan.robust.utils.Constants;
import so.brendan.robust.views.SplashView;

/**
 * Implementation of the SplashView.
 */
public class SplashActivity extends Activity implements SplashView {
    private static String TAG = Constants.createTag(SplashActivity.class);

    private SplashPresenter mPresenter;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d(TAG, "created");

        mTextView = (TextView) findViewById(R.id.splashText);
        mPresenter = new SplashPresenterImpl(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.finish();
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void navigateToMain(boolean noTransition) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(noTransition ? 0 : android.R.anim.fade_in, 0);
    }

    @Override
    public void navigateToServerPreferences() {
        Intent intent = new Intent(this, ServerPreferencesActivity.class);
        startActivity(intent);
    }

    @Override
    public void showError(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showConnecting() {
        mTextView.setText("Connecting...");
    }

    @Override
    public void showAuthenticating() {
        mTextView.setText("Authenticating...");
    }
}
