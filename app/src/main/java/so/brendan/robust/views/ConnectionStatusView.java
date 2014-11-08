package so.brendan.robust.views;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * A view for showing the security of the connection.
 */
public interface ConnectionStatusView {
    /**
     * Sets the validity status.
     *
     * @param validity
     */
    public void setValidity(String validity);

    /**
     * Sets the current peer (host:port).
     *
     * @param peer
     */
    public void setPeer(String peer);

    /**
     * Sets the current cipher suite.
     *
     * @param cipher
     */
    public void setCipher(String cipher);

    /**
     * Sets the current SSL/TLS protocol.
     *
     * @param protocol
     */
    public void setProtocol(String protocol);

    /**
     * Sets the current certificate chain.
     *
     * @param chain
     */
    public void setCertificateChain(List<X509Certificate> chain);
}
