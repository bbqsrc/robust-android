package so.brendan.robust.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import so.brendan.robust.R;
import so.brendan.robust.presenters.TwitterLoginPresenter;
import so.brendan.robust.presenters.TwitterLoginPresenterImpl;
import so.brendan.robust.views.TwitterLoginView;

public class TwitterLoginActivity extends Activity implements TwitterLoginView {
    private TwitterLoginPresenter mPresenter;
    private WebView mWebView;
    private ProgressDialog mProgress;

    private class TwitterWebViewClient extends WebViewClient {
        private boolean mFirstPage = true;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mPresenter.validateURL(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (mFirstPage) {
                hideLoadingDialog();
                mFirstPage = false;
            }
        }
    };

    /**
     * JavaScript is enabled, otherwise entering a password incorrectly in the webview
     * causes a popup to appear that can only be closed with JavaScript, blocking further input.
     *
     * @param savedInstanceState
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_login);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new TwitterWebViewClient());

        mPresenter = new TwitterLoginPresenterImpl(this);
        mPresenter.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
        mPresenter.finish();
    }

    @Override
    public void openWebView(String url) {
        mWebView.loadUrl(url);
    }

    @Override
    public void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void showLoadingDialog(String text) {
        if (mProgress == null) {
            mProgress = new ProgressDialog(this);
        }

        mProgress.setTitle(R.string.title_activity_twitter_login);

        mProgress.setMessage(text);
        mProgress.show();
    }

    @Override
    public void hideLoadingDialog() {
        if (mProgress == null) {
            return;
        }

        mProgress.dismiss();
        mProgress = null;
    }
}
