package so.brendan.robust.services;

import android.text.TextUtils;
import android.util.Log;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import so.brendan.robust.utils.Constants;

/**
 * Initialiser for the raw sockets of the Robust session.
 *
 * Ensures that the TLS connection will only be of TLSv1 or higher.
 *
 * No POODLE (CVE-2014-3566) vulnerability for us!
 */
public class MessengerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final String TAG = Constants.createTag(MessengerChannelInitializer.class);

    private RobustSession mSession;
    private String mHost;
    private int mPort;

    protected MessengerChannelInitializer(RobustSession session, String host, int port) {
        mSession = session;
        mHost = host;
        mPort = port;
    }

    private String[] stripOldProtocols(SSLEngine engine) {
        ArrayList<String> protocols = new ArrayList<String>(
                Arrays.asList(engine.getEnabledProtocols()));

        protocols.remove("SSLv2");
        protocols.remove("SSLv3");

        String[] newProtocols = new String[protocols.size()];

        for (int i = 0; i < protocols.size(); ++i) {
            newProtocols[i] = protocols.get(i);
        }

        return newProtocols;
    }

    private SslHandler makeSslHandler(SocketChannel ch) {
        final TrustManagerFactory tmf;
        SslContext sslContext;

        try {
            // Default Android keystore
            KeyStore ks = KeyStore.getInstance("AndroidCAStore");
            ks.load(null, null);

            // Default trust manager.
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(ks);

            sslContext = SslContext.newClientContext(tmf);
        } catch (Exception e) {
            Log.e(TAG, "Error while creating SSL context", e);
            return null;
        }

        SslHandler handler = sslContext.newHandler(ch.alloc(), mHost, mPort);

        handler.engine().setEnabledProtocols(stripOldProtocols(handler.engine()));

        Log.d(TAG, String.format("Supported protocols: %s",
                TextUtils.join(", ", handler.engine().getSupportedProtocols())));
        Log.d(TAG, String.format("Enabled protocols: %s",
                TextUtils.join(", ", handler.engine().getEnabledProtocols())));
        Log.d(TAG, String.format("Supported ciphers: %s",
                TextUtils.join(", ", handler.engine().getSupportedCipherSuites())));
        Log.d(TAG, String.format("Enabled ciphers: %s",
                TextUtils.join(", ", handler.engine().getEnabledCipherSuites())));

        return handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("ssl", makeSslHandler(ch));

        // BUG: the server refuses to enforce a fixed frame size at the moment.
        // TODO fix the server so there's a reasonable max frame size.
        pipeline.addLast(new DelimiterBasedFrameDecoder(10000000, Delimiters.lineDelimiter()));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());

        pipeline.addLast("idle", new IdleStateHandler(240, 180, 0));
        pipeline.addLast("handler", new MessengerChannelHandler(mSession));
    }
}
