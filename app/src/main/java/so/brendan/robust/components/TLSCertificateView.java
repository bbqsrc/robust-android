package so.brendan.robust.components;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.security.cert.X509Certificate;
import java.util.List;

import so.brendan.robust.R;
import so.brendan.robust.models.SimpleX500Name;
import so.brendan.robust.utils.TLSUtils;

/**
 * A view for TLS certificates so the user can ascertain how secure their connection is.
 */
public class TLSCertificateView extends LinearLayout {
    private Context mContext;
    private X509Certificate mCert;

    public TLSCertificateView(Context context, X509Certificate cert) {
        this(context);
        init(context);
        setCertificate(cert);
    }

    public TLSCertificateView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setOrientation(VERTICAL);
    }

    private View createView(String key, String value) {
        LinearLayout kv = (LinearLayout) inflate(mContext, R.layout.layout_key_value, null);

        TextView keyText = (TextView) kv.findViewById(R.id.key);
        TextView valueText = (TextView) kv.findViewById(R.id.value);

        keyText.setText(key);
        valueText.setText(value);

        return kv;
    }

    /**
     * Handles the key-value name pairs for a certificate.
     *
     * For example:
     * <code>"CN=Foo C=Sydney"</code> -> <code>{ Common Name: Foo, City: Sydney }</code>
     *
     * @param entries
     */
    private void insertEntries(List<SimpleX500Name.X500NameEntry> entries) {
        for (SimpleX500Name.X500NameEntry entry : entries) {
            String key = SimpleX500Name.translate(mContext, entry.getKey());
            String value = entry.getValue();

            addView(createView(key, value));
        }
    }

    private void updateSubjectInfo() {
        final String altNames = getResources().getString(R.string.alternative_names);

        SimpleX500Name subjectName = new SimpleX500Name(mCert.getSubjectX500Principal());

        TextView title = (TextView) inflate(mContext, R.layout.layout_header3_key_value, null);
        title.setText(getResources().getString(R.string.subject_name));
        addView(title);

        boolean first = true;
        for (String name : TLSUtils.getSubjectAlternativeNames(mCert))  {
            if (first) {
                addView(createView(altNames, name));
                first = false;
            } else {
                addView(createView("", name));
            }
        }

        insertEntries(subjectName.entries());
    }

    private void updateIssuerInfo() {
        final String altNames = getResources().getString(R.string.alternative_names);

        SimpleX500Name issuerName = new SimpleX500Name(mCert.getIssuerX500Principal());

        TextView title = (TextView) inflate(mContext, R.layout.layout_header3_key_value, null);
        title.setText(getResources().getString(R.string.issuer_name));
        addView(title);

        boolean first = true;
        for (String name : TLSUtils.getIssuerAlternativeNames(mCert))  {
            if (first) {
                addView(createView(altNames, name));
                first = false;
            } else {
                addView(createView("", name));
            }
        }

        insertEntries(issuerName.entries());
    }

    private void updateTitle() {
        SimpleX500Name subjectName = new SimpleX500Name(mCert.getSubjectX500Principal());

        TextView title = (TextView) inflate(mContext, R.layout.layout_header2_key_value, null);
        title.setText(subjectName.getCommonName());
        addView(title);
    }

    private void updateView() {
        removeAllViews();

        updateTitle();
        updateSubjectInfo();
        updateIssuerInfo();
    }

    /**
     * Sets the certificate for the view.
     *
     * @param cert
     */
    public void setCertificate(X509Certificate cert) {
        mCert = cert;
        updateView();
    }
}
