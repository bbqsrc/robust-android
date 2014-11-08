package so.brendan.robust.presenters;

import android.content.Context;
import android.content.res.Resources;

import so.brendan.robust.R;
import so.brendan.robust.interactors.ConnectionStatusInteractor;
import so.brendan.robust.interactors.ConnectionStatusInteractorImpl;
import so.brendan.robust.models.TLSSessionData;
import so.brendan.robust.views.ConnectionStatusView;

/**
 * Implementation of the ConnectionStatusPresenter interface.
 */
public class ConnectionStatusPresenterImpl implements ConnectionStatusPresenter {
    private Context mCtx;
    private ConnectionStatusView mView;
    private ConnectionStatusInteractor mInteractor;

    public ConnectionStatusPresenterImpl(ConnectionStatusView view) {
        mView = view;
        mCtx = (Context) view;
        mInteractor = new ConnectionStatusInteractorImpl((Context) mView, this);
    }

    @Override
    public void start() {
        mInteractor.registerListeners();
        mInteractor.requestSSLSession();
    }

    @Override
    public void finish() {
        mInteractor.unregisterListeners();
    }

    public void updateTLSSession(TLSSessionData session) {
        Resources res = mCtx.getResources();

        String valid;

        switch (session.checkValidity()) {
            case TLSSessionData.NO_MATCH: {
                valid = res.getString(R.string.tls_no_match);
                break;
            }
            case TLSSessionData.MATCHES_ALT_NAME: {
                valid = res.getString(R.string.tls_matches_alt_name);
                break;
            }
            case TLSSessionData.MATCHES_CN: {
                valid = res.getString(R.string.tls_matches_cn);
                break;
            }
            default: {
                valid = res.getString(R.string.unknown).toUpperCase();
            }
        }

        mView.setValidity(valid);
        mView.setPeer(session.getPeerHost() + ":" + session.getPeerPort());
        mView.setCipher(session.getCipherSuite());
        mView.setProtocol(session.getProtocol());
        mView.setCertificateChain(session.getCertificateChain());
    }
}
