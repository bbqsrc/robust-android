package so.brendan.robust.interactors;

import android.content.Context;

import com.squareup.otto.Subscribe;

import so.brendan.robust.models.TLSSessionData;
import so.brendan.robust.presenters.ConnectionStatusPresenter;
import so.brendan.robust.services.MessengerService;
import so.brendan.robust.utils.BusProvider;
import so.brendan.robust.utils.Constants;

/**
 * An implementation of ConnectionStatusInteractor.
 */
public class ConnectionStatusInteractorImpl implements ConnectionStatusInteractor {
    private static final String TAG = Constants.createTag(ConnectionStatusInteractorImpl.class);

    private ConnectionStatusPresenter mPresenter;
    private Context mCtx;

    public ConnectionStatusInteractorImpl(Context context, ConnectionStatusPresenter presenter) {
        mPresenter = presenter;
        mCtx = context;
    }

    public void registerListeners() {
        BusProvider.getInstance().register(this);
    }

    public void unregisterListeners() {
        BusProvider.getInstance().unregister(this);
    }

    public void requestSSLSession() {
        MessengerService.requestAction(mCtx, MessengerService.ACTION_SESSION_TLS);
    }

    @Subscribe
    public void onTLSSession(TLSSessionData session) {
        mPresenter.updateTLSSession(session);
    }
}
