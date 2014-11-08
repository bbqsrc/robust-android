package so.brendan.robust.utils;

import org.bouncycastle.x509.extension.X509ExtensionUtil;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.security.cert.CertificateEncodingException;

/**
 * Let me tell you a story of object-oriented programming.
 *
 * There once was a young, fun package called <code>javax.security.cert</code>.
 *
 * Later, <code>javax.security.cert</code> got popular, and some aspects of its API were promoted
 * to boring old <code>java.security.cert</code>.
 *
 * However, <code>javax.security.cert</code> still had to maintain compatibility with old codebases,
 * and <code>javax.security.cert</code> was frozen and forgotten to the ages.
 *
 * Life went on, new packages built upon <code>java.security.cert</code>, but some did not.
 *
 * This is where our current nightmare begins.
 */
final public class TLSUtils {
    private static CertificateFactory sCertFactory;

    /**
     * I only need one factory, surely. Let's do this once and let's do this well.
     */
    static {
        try {
            sCertFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a <code>java.security.cert.X509Certificate</code> from raw certificate byte data.
     *
     * Don't you just love the factory pattern? I don't, so this method now exists instead.
     *
     * @param certData
     * @return
     * @throws CertificateException
     */
    public static X509Certificate createX509Certificate(byte[] certData) throws CertificateException {
        return (X509Certificate) sCertFactory.generateCertificate(
                new ByteArrayInputStream(certData));
    }

    /**
     * Converts the old <code>javax.security.cert.X509Certificate</code> to
     * <code>java.security.cert.X509Certificate</code>.
     *
     * This beautiful method signature brought to you by Java's strict import rules.
     *
     * @param oldCert
     * @return
     */
    public static X509Certificate convertCertificate(javax.security.cert.X509Certificate oldCert) {
        try {
            return createX509Certificate(oldCert.getEncoded());
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Converts an old <code>javax.security.cert.X509Certificate[]</code> to
     * a <code>List<java.security.cert.X509Certificate></java.security.cert.X509Certificate></code>.
     *
     * This beautiful method signature brought to you by Java's strict import rules.
     *
     * @param oldCerts
     * @return
     */
    public static List<X509Certificate> convertCertificates(javax.security.cert.X509Certificate[] oldCerts) {
        ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>(oldCerts.length);

        for (javax.security.cert.X509Certificate oldCert : oldCerts) {
            certs.add(convertCertificate(oldCert));
        }

        return certs;
    }

    public static List<String> getSubjectAlternativeNames(X509Certificate cert) {
        Collection altNames;
        try {
            altNames = X509ExtensionUtil.getSubjectAlternativeNames(cert);
        } catch (CertificateParsingException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }

        return getAlternativeNames(altNames);
    }

    public static List<String> getIssuerAlternativeNames(X509Certificate cert) {
        Collection altNames;
        try {
            altNames = X509ExtensionUtil.getIssuerAlternativeNames(cert);
        } catch (CertificateParsingException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }

        return getAlternativeNames(altNames);
    }

    private static List<String> getAlternativeNames(Collection altNames) {
        ArrayList<String> out = new ArrayList<String>();

        // X509ExtensionUtil does this bizarre thing where it returns an untyped Collection
        // of untyped ArrayLists. This is nasty.
        for (Object listObj : altNames) {
            if (listObj instanceof List) {
                List list = (List) listObj;

                if (list.size() < 2) {
                    continue;
                }

                // Index 0 is a tag; 1 is what we want.
                Object name = list.get(1);
                if (name instanceof String) {
                    out.add((String) name);
                } else {
                    out.add(name.toString());
                }
            }
        }

        return out;
    }

    private TLSUtils() {}
}
