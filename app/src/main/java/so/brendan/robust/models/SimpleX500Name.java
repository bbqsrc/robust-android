package so.brendan.robust.models;

import android.content.Context;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import so.brendan.robust.R;

/**
 * A wrapper around X500Name instances to make dealing with them significantly easier for
 * common use cases, like getting human-parseable values for the names.
 */
public class SimpleX500Name {
    private static final ASN1ObjectIdentifier[] OID_ARRAY = new ASN1ObjectIdentifier[] {
        BCStyle.CN, BCStyle.C, BCStyle.ST, BCStyle.L, BCStyle.O, BCStyle.OU
    };

    private static final HashMap<ASN1ObjectIdentifier, Integer> ASN1_TRANSLATION =
            new HashMap<ASN1ObjectIdentifier, Integer>() {{
                put(BCStyle.CN, R.string.asn1_common_name);
                put(BCStyle.C, R.string.asn1_country);
                put(BCStyle.ST, R.string.asn1_state);
                put(BCStyle.L, R.string.asn1_locality);
                put(BCStyle.O, R.string.asn1_organization);
                put(BCStyle.OU, R.string.asn1_organizational_unit);
            }};

    /**
     * Translate the provided ASN.1 object to its localised string.
     *
     * @param context
     * @param oid
     * @return
     */
    public static String translate(Context context, ASN1ObjectIdentifier oid) {
        Integer oidInt = ASN1_TRANSLATION.get(oid);
        String v = null;

        if (oidInt != null) {
            v = context.getString(oidInt);
        }

        if (v == null) {
            return oid.toString();
        }

        return v;
    }

    private final X500Name mName;

    public SimpleX500Name(X500Principal principal) {
        this(new X500Name(principal.getName(X500Principal.RFC2253)));
    }

    public SimpleX500Name(X500Name name) {
        mName = name;
    }

    private String getSingleRDN(ASN1ObjectIdentifier oid) {
        RDN[] rdn = mName.getRDNs(oid);

        if (rdn.length > 0) {
            return IETFUtils.valueToString(rdn[0].getFirst().getValue());
        }

        return null;
    }

    public String getCommonName() {
        return getSingleRDN(BCStyle.CN);
    }

    public String getCountry() {
        return getSingleRDN(BCStyle.C);
    }

    public String getState() {
        return getSingleRDN(BCStyle.ST);
    }

    public String getLocality() {
        return getSingleRDN(BCStyle.L);
    }

    public String getOrganization() {
        return getSingleRDN(BCStyle.O);
    }

    public String getOrganizationalUnit() {
        return getSingleRDN(BCStyle.OU);
    }

    public X500Name getX500Name() {
        return mName;
    }

    public List<X500NameEntry> entries() {
        ArrayList<X500NameEntry> list =
                new ArrayList<X500NameEntry>();

        for (ASN1ObjectIdentifier oid : OID_ARRAY) {
            String value = getSingleRDN(oid);

            if (value != null) {
                list.add(new X500NameEntry(oid, value));
            }
        }

        return list;
    }

    /**
     * A simple implementation of <code>Map.Entry</code> for holding the key-value pairs relevant
     * to an X500Name.
     */
    public static class X500NameEntry implements Map.Entry<ASN1ObjectIdentifier, String> {
        private final ASN1ObjectIdentifier mOID;
        private final String mValue;

        private X500NameEntry(ASN1ObjectIdentifier oid, String value) {
            mOID = oid;
            mValue = value;
        }

        @Override
        public ASN1ObjectIdentifier getKey() {
            return mOID;
        }

        @Override
        public String getValue() {
            return mValue;
        }

        @Override
        public String setValue(String object) {
            throw new UnsupportedOperationException();
        }
    }
}

