package so.brendan.robust.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.security.cert.X509Certificate;
import java.util.List;

import so.brendan.robust.R;
import so.brendan.robust.components.TLSCertificateView;
import so.brendan.robust.presenters.ConnectionStatusPresenter;
import so.brendan.robust.presenters.ConnectionStatusPresenterImpl;
import so.brendan.robust.views.ConnectionStatusView;

/**
 * An activity for displaying the TLS connection status, such as cipher suite, peering, and
 * certificate chain.
 */
public class ConnectionStatusActivity extends Activity implements ConnectionStatusView {
    private TextView mValidity;
    private TextView mPeerText;
    private TextView mCipherText;
    private TextView mProtocolText;
    private LinearLayout mCertChainView;
    private ConnectionStatusPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_status);

        mValidity = (TextView) findViewById(R.id.verified);
        mPeerText = (TextView) findViewById(R.id.peer);
        mCipherText = (TextView) findViewById(R.id.cipher);
        mProtocolText = (TextView) findViewById(R.id.protocol);
        mCertChainView = (LinearLayout) findViewById(R.id.certificateChain);

        mPresenter = new ConnectionStatusPresenterImpl(this);
        mPresenter.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.finish();
    }

    /**
     * Sets the validity message in the view.
     *
     * @param validity
     */
    public void setValidity(String validity) {
        mValidity.setText(validity);
    }

    /**
     * Sets the peer text in the view.
     *
     * @param peer
     */
    public void setPeer(String peer) {
        mPeerText.setText(peer);
    }

    /**
     * Sets the cipher suite text in the view.
     *
     * @param cipher
     */
    @Override
    public void setCipher(String cipher) {
        mCipherText.setText(cipher);
    }

    /**
     * Sets the protocol text in the view.
     *
     * @param protocol
     */
    @Override
    public void setProtocol(String protocol) {
        mProtocolText.setText(protocol);
    }

    /**
     * Sets the certificate chain in the view.
     *
     * @param chain
     */
    @Override
    public void setCertificateChain(List<X509Certificate> chain) {
        mCertChainView.removeAllViews();

        boolean first = true;
        for (X509Certificate cert : chain) {
            if (first) {
                first = false;
            } else {
                View v = getLayoutInflater().inflate(R.layout.divider, null);
                mCertChainView.addView(v);
            }
            mCertChainView.addView(new TLSCertificateView(this, cert));
        }
    }
}
