package so.brendan.robust.services;

import android.util.Log;

import java.net.InetSocketAddress;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import so.brendan.robust.utils.Constants;

/**
 * The handler for the raw socket of the Robust session.
 *
 * Mostly just passes through messages to the session to be handled, including idle state events,
 * connection events and <code>SSLSession</code> instantiation.
 */
public class MessengerChannelHandler extends SimpleChannelInboundHandler<String> {
    private String TAG = Constants.createTag(MessengerChannelHandler.class);

    private RobustSession mSession;

    protected MessengerChannelHandler(RobustSession session) {
        mSession = session;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);

        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent)evt).state();
            mSession.onIdleState(state);
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        Log.i(TAG, String.format("Channel opened. (%s)",
                ctx.channel().remoteAddress().toString()));

        final SslHandler handler = (SslHandler) ctx.pipeline().get("ssl");

        // Add the SSLSession to the session as soon as it's available.
        handler.handshakeFuture().addListener(new GenericFutureListener<Future<? super Channel>>() {
            @Override
            public void operationComplete(Future<? super Channel> future) throws Exception {
                mSession.setSSLSession(handler.engine().getSession());
            }
        });

        mSession.getState().setConnectionState(RobustSession.STATE_CONNECTED);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();

        Log.i(TAG, String.format("Channel connection lost. (%s)", addr.toString()));

        if (!mSession.isFinished()) {
            // A random throwable in order to trigger the reconnect on error behaviour.
            mSession.finish(new Throwable("Reconnect me, Charlemagne!"));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof ConnectTimeoutException) {
            Log.d(TAG, "connection timeout exception");
            return; // no finish, fall through to listener
        }

        mSession.finish(cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Log.d(TAG, String.format("<- %s", msg));

        mSession.onMessageReceived(msg);
    }
}
