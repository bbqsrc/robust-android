package so.brendan.robust.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import so.brendan.robust.R;
import so.brendan.robust.views.LoginView;

/**
 * An activity for logging into Robust using various authentication mechanisms.
 */
public class LoginActivity extends Activity implements LoginView, View.OnClickListener {
    private Button mTwitterBtn;
    private Button mPlainBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mTwitterBtn = (Button) findViewById(R.id.twitterBtn);
        mPlainBtn = (Button) findViewById(R.id.plainBtn);

        mTwitterBtn.setOnClickListener(this);
        mPlainBtn.setOnClickListener(this);
    }

    /**
     * Handle clicks for the authenticator buttons.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.equals(mTwitterBtn)) {
            navigateToTwitterLogin();
        } else if (v.equals(mPlainBtn)) {
            navigateToPlainLogin();
        }
    }

    /**
     * Navigates the activity to the Twitter Login Activity.
     */
    @Override
    public void navigateToTwitterLogin() {
        startActivity(new Intent(this, TwitterLoginActivity.class));
    }

    /**
     * Navigates the activity to the Plain Login Activity.
     */
    @Override
    public void navigateToPlainLogin() {
        // XXX: Not implemented on server yet!
        Toast.makeText(this, "Plain login unavailable.", Toast.LENGTH_LONG).show();
    }
}
