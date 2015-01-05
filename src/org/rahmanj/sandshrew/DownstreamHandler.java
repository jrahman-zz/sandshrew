
package org.rahmanj.sandshrew;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for client connections to the downstream server
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamHandler extends ChannelInboundHandlerAdapter {


    /**
     * Construct an instance of the {@link DownstreamHandler}
     * @param upstreamChannel The {@link Channel} to send data upstream through
     * @param downstreamServer The {@link DownstreamServer} we are sending data to
     */
    public DownstreamHandler(Channel upstreamChannel, DownstreamServer downstreamServer) {
        _upstreamChannel = upstreamChannel;
        _downstreamServer = downstreamServer;
        _isWritable = true; // Sane default

        InetSocketAddress address = (InetSocketAddress)_upstreamChannel.remoteAddress();
        _remoteIdentifier = address.getHostString(); // Dodge the DNS call with getHostString()
    }

    /**
     * Perform appropriate initialization
     * @param ctx The ChannelHandlerContext for this channel
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

        // Forward if needed
        ctx.fireChannelActive();
    }

    /**
     * Perform appropriate teardown
     * @param ctx The {@link ChannelHandlerContext} for this channel
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        // Forward if needed
        ctx.fireChannelInactive();
    }

    /**
     * Handle read events from the downstream server
     * @param ctx The {@link ChannelHandlerContext} for this channel
     * @param msg A {@link HttpResponse} or {@link HttpContent} from the {@link DownstreamServer}
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        // TODO (JR) Forward onward to the other channel

    }

    /**
     * Record changes in channel writability from the proxy to the server so that we can properly handle backpressure
     * from the server to the client
     * @param ctx
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {

        // Check for toggle from previous state
        if (_isWritable != ctx.channel().isWritable()) {
            _isWritable = !_isWritable;

            if (_isWritable) {
                unthrottleClient();
            } else {
                throttleClient();
            }
        }

        // Forward onward
        ctx.fireChannelWritabilityChanged();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        // TODO (JR) Convert this to logging framework
        // TODO (JR) Have this do something to robustly respond
        cause.printStackTrace();
        ctx.close();

        // TODO (JR) Is this safe?
        _upstreamChannel.close();
    }

    /**
     * Stop throttling the remote client if the DownstreamServer allows it
     */
    protected void unthrottleClient() {

        // TODO (JR) Reconsider the global throttle count because
        // the problem is that only the last DownstreamClient
        // to unthrottle the server will see that the server
        // is no longer experiencing backpressure, so
        // a notification mechanism would be needed to inform all
        // DownstreamClients connected to the DownstreamServer
        // that the server was free again, which would be nasty

        if (_downstreamServer.decrementThrottle() == 0) {
            _logger.log(Level.FINE, "Renabling reads from upstream: " + _remoteIdentifier);
            _upstreamChannel.config().setAutoRead(true);
        }
    }

    /**
     * Start throttling the remote client if the DownstreamServer requires it
     */
    protected void throttleClient() {
        _downstreamServer.incrementThrottle();
        _logger.log(Level.FINE, "Disabling reads from upstream: " + _remoteIdentifier);
        _upstreamChannel.config().setAutoRead(false);
    }

    /**
     * {@link Channel} for the upstream channel data should be sent to
     */
    private Channel _upstreamChannel;

    /**
     * {@link DownstreamServer} this client channel is proxying to
     */
    private DownstreamServer _downstreamServer;

    /**
     * Track if the connection is writable
     */
    private boolean _isWritable;

    /**
     * Store the address of the remote client
     */
    private String _remoteIdentifier;

    private static final Logger _logger = Logger.getLogger(
            DownstreamHandler.class.getName()
    );
}
