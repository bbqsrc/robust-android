package so.brendan.robust.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import so.brendan.robust.utils.TLSUtils;

/**
 * A parcelable, immutable TLS session data object for parceling and feeding to views.
 */
public class TLSSessionData implements Parcelable {
    public static final int NO_MATCH = 0;
    public static final int MATCHES_ALT_NAME = 1;
    public static final int MATCHES_CN = 2;

    private final String mCipherSuite;
    private final String mProtocol;
    private final String mPeerHost;
    private final int mPeerPort;
    private final boolean mIsValid;
    private final List<X509Certificate> mPeerCertChain;

    public TLSSessionData(SSLSession session) {
        mCipherSuite = session.getCipherSuite();
        mProtocol = session.getProtocol();
        mPeerHost = session.getPeerHost();
        mPeerPort = session.getPeerPort();
        mIsValid = session.isValid();

        List<X509Certificate> certs;
        try {
            certs = TLSUtils.convertCertificates(session.getPeerCertificateChain());
        } catch (SSLPeerUnverifiedException e) {
            certs = null;
            // Do nothing.
        }

        mPeerCertChain = certs;
    }

    private TLSSessionData(Parcel in) {
        mCipherSuite = in.readString();
        mProtocol = in.readString();
        mPeerHost = in.readString();
        mPeerPort = in.readInt();
        mIsValid = in.readByte() != 0;

        ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>();
        int c = in.readInt();

        for (int i = 0; i < c; ++i) {
            byte[] data = new byte[in.readInt()];
            in.readByteArray(data);
            try {
                certs.add(TLSUtils.createX509Certificate(data));
            } catch (CertificateException e) {
                e.printStackTrace();
            }
        }

        mPeerCertChain = certs;
    }

    public String getCipherSuite() {
        return mCipherSuite;
    }

    public String getProtocol() {
        return mProtocol;
    }

    public String getPeerHost() {
        return mPeerHost;
    }

    public int getPeerPort() {
        return mPeerPort;
    }

    public X509Certificate getPeerCertificate() {
        if (mPeerCertChain.isEmpty()) {
            return null;
        }

        return mPeerCertChain.get(0);
    }

    public int checkValidity() {
        String peerName = getPeerHost();
        X509Certificate cert = getPeerCertificate();

        List<String> altNames = TLSUtils.getSubjectAlternativeNames(cert);
        String commonName = new SimpleX500Name(cert.getSubjectX500Principal()).getCommonName();

        if (altNames.contains(peerName)) {
            return MATCHES_ALT_NAME;
        } else if (peerName.equals(commonName)) {
            return MATCHES_CN;
        } else {
            return NO_MATCH;
        }
    }

    public List<X509Certificate> getCertificateChain() {
        return mPeerCertChain;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCipherSuite);
        dest.writeString(mProtocol);
        dest.writeString(mPeerHost);
        dest.writeInt(mPeerPort);
        dest.writeByte((byte) (mIsValid ? 1 : 0));

        dest.writeInt(mPeerCertChain.size());

        for (X509Certificate cert : mPeerCertChain) {
            byte[] data;

            try {
                data = cert.getEncoded();
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
                continue;
            }

            dest.writeInt(data.length);
            dest.writeByteArray(data);
        }
    }

    public static final Creator<TLSSessionData> CREATOR = new Creator<TLSSessionData>() {
        @Override
        public TLSSessionData createFromParcel(Parcel source) {
            return new TLSSessionData(source);
        }

        @Override
        public TLSSessionData[] newArray(int size) {
            return new TLSSessionData[size];
        }
    };
}
