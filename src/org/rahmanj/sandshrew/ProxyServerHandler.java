
package org.rahmanj.sandshrew;

import io.netty.channel.*;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Primary handler for incoming HTTP and SPDY requests
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * Construct an instance of the ProxyServerHandler using the given EventLoopGroup
     * @param workerGroup The shared EventLoopGroup to use for async IO
     */
    public ProxyServerHandler(EventLoopGroup workerGroup) {
        _workerGroup = workerGroup;
        _isWritable = true; // Sane default
        _remoteIdentifier = null;
        _downstreamServer = null;
        _downstreamClient = null;
        _downstreamClientFuture = null;
    }

    /**
     * Performs appropriate setup
     * @param ctx The {@link ChannelHandlerContext} for this given channel
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

        // TODO (JR) Any setup operations
        InetSocketAddress address = (InetSocketAddress)ctx.channel().remoteAddress();

        // Use getHostString() to prevent a DNS lookup from happening
        _remoteIdentifier = address.getHostString();

        _logger.log(Level.FINE, "Connection opened with downstream: " + _remoteIdentifier);

        // Forward if needed
        ctx.fireChannelActive();
    }

    /**
     * Perform appropriate teardown
     * @param ctx The {@link ChannelHandlerContext} for this given channel
     */
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {

        // TODO (JR) Any teardown operations

        if (_remoteIdentifier != null) {
            _logger.log(Level.FINE, "Connection closed with downstream: " + _remoteIdentifier);
            _remoteIdentifier = null;
        }

        // Forward if needed
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (isHeader(msg)) {
            HttpMessage req = (HttpMessage)msg;
            HttpHeaders header = req.headers();

            // TODO (JR) Determine the route
            ProxyRoute route = new ProxyRoute(null, null);

            // TODO (JR) Update this??
            _downstreamServer = route.getPolicy().selectDownstreamServer();
            _downstreamClient = new DownstreamClient(ctx.channel(), _downstreamServer, _workerGroup);

            try {
                // Start the downstream client connection
                // Note that this only starts the client connection process
                // The client isn't actually connected yet, but will be once
                // the future completes
                _downstreamClientFuture = _downstreamClient.run();
            } catch (Exception ex) {
                // TODO (JR) Blow things up as needed
            }

            // TODO (JR) Wait on the future before doing stuff


        } else if (isContent(msg)) {
            // TODO (JR) Send stuff to the downstream server
        } else {
            // Badness
            _logger.log(Level.WARNING, "Unknown message type read");
        }
    }

    /**
     * Handle backpressure from the remote client so we can throttle reads from the {@link DownstreamServer}
     * @param ctx ChannelHandlerContext for this particular channel
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {

        // Check for toggle from previous state
        if (_isWritable != ctx.channel().isWritable()) {
            _isWritable = !_isWritable;

            if (_isWritable) {
                enableDownstreamReads();
            } else {
                disableDownstreamReads();
            }
        }

        // Forward on to the chain
        ctx.fireChannelWritabilityChanged();
    }

    /**
     * Handle the occurrence of an exception
     * @param ctx ChannelHandlerContext for this channel
     * @param cause Throwable raised by the error
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised
        // TODO (JR) Whack the downstream client at this point if it has been established
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * Disable automatic reads from the downstream server, the remote client cannot keep up
     */
    protected void enableDownstreamReads() {
        // TODO (JR) add identification information
        _logger.log(Level.FINE, "Backpressure from " + _remoteIdentifier + " over, enabling reads from");
        // TODO (JR) Reenable reads from the downstream server
    }

    /**
     * Enable automatic reads from the downstream server, the remote client can keep up
     */
    protected void disableDownstreamReads() {
        // TODO (JR) add identification information
        _logger.log(Level.FINE, "Backpressure from " + _remoteIdentifier + ", disabling reads from");
        // TODO (JR) Disable reads from the downsteam server
    }

    /**
     * Check if a given input is a {@link HttpMessage} object
     * @param msg {@link Object} to check
     * @return Returns true if the object is truly a {@link HttpMessage} object
     */
    protected boolean isHeader(Object msg) {
        return msg instanceof HttpMessage;
    }

    /**
     * Check if a given input is a {@link HttpContent} object
     * @param msg {@link Object} to check
     * @return Returns true if the object is truly a {@link HttpContent} object
     */
    protected boolean isContent(Object msg) {
        return msg instanceof HttpContent;
    }

    /**
     * Shared {@link EventLoopGroup} used for all async background IO
     */
    private EventLoopGroup _workerGroup;

    /**
     * Track last writable state
     */
    private boolean _isWritable;

    /**
     * Information about the remote agent
     */
    private String _remoteIdentifier;

    /**
     * {@link DownstreamServer} we are proxying for
     */
    private DownstreamServer _downstreamServer;

    /**
     * {@link DownstreamClient} to transmit data to the {@link DownstreamServer}
     */
    private DownstreamClient _downstreamClient;

    /**
     * {@link ChannelFuture} for the Close event on the downstream client
     */
    private ChannelFuture _downstreamClientFuture;

    private static final Logger _logger = Logger.getLogger(
            ProxyServerHandler.class.getName()
    );
}
