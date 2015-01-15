
package org.rahmanj.sandshrew;

import io.netty.channel.*;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.handler.codec.http.HttpObject;
import org.rahmanj.sandshrew.policy.DownstreamServer;
import org.rahmanj.sandshrew.routes.ProxyRoute;

/**
 * Primary handler for incoming HTTP and SPDY requests
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class UpstreamHandler extends ChannelInboundHandlerAdapter implements ProxyChannel {

    /**
     * Construct an instance of the UpstreamHandler using the given EventLoopGroup
     * @param workerGroup The shared EventLoopGroup to use for async IO
     */
    public UpstreamHandler(EventLoopGroup workerGroup) {
        _workerGroup = workerGroup;
        _writable = true; // Sane default
        _remoteIdentifier = null;

        _channel = null;
        _remoteAddress = null;

        _downstreamServer = null;
        _downstreamClient = null;
        _downstreamClientFuture = null;


        _throttled = false;
    }



    /**
     * Send a given {@link HttpObject} over the {@link ProxyChannel}. This method is asynchronous.
     *
     * @param msg The {@link HttpObject} to send over the {@link ProxyChannel}
     */
    public void send(final HttpObject msg) {

        /**
         * _channel should never be null or unconnected because the DownstreamHandler should not
         * even begin to start calling this method until after we have began reading from the upstream client
         * By then _channel is non-null and connected as expected
         *
         */

        _channel.eventLoop().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        _channel.write(msg);
                    }
                }
        );
    }

    /**
     * Send a given {@link HttpObject} over the {@link ProxyChannel}. This method is asynchronous
     *
     * @param msg The {@link HttpObject} to send over the {@link ProxyChannel}
     * @param promise A {@ChannelPromise} to be triggered when the {@link HttpObject} is sent
     */
    public void send(final HttpObject msg, final ChannelPromise promise) {
        _channel.eventLoop().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        _channel.write(msg, promise);
                    }
                }
        );
    }

    /**
     * Throttle AutoRead from the {@link Channel}
     */
    public void throttle() {
        _channel.eventLoop().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        _throttled = true;

                        if (_channel != null) {
                            _channel.config().setAutoRead(false);
                        }
                    }
                }
        );
    }



    /**
     * Unthrottle AutoRead from the {@link Channel}
     */
    public void unthrottle() {
        _channel.eventLoop().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        _throttled = false;

                        if (_channel != null) {
                            _channel.config().setAutoRead(true);
                        }
                    }
                }
        );
    }

    /**
     *
     * @return
     */
    public boolean isDraining() {
        return _draining;
    }

    /**
     * Checks if the {@link Channel} behind this {@link UpstreamHandler} is writable or not
     * @return Returns true if the {@link Channel} is writable, false otherwise
     */
    public boolean isWritable() {
        return _writable;
    }

    /**
     * Get the remote address of the remote upstream client
     *
     * @return Returns a {@link java.net.InetSocketAddress} if the connection has been established, null otherwise
     */
    public InetSocketAddress getRemoteAddress() {
        return _remoteAddress;
    }

    /**
     * Asynchronously start the given {@link ProxyChannel}
     */
    public ChannelFuture run() {

        // NOOP
        return null;
    }

    /**
     * Perform a graceful asynchronous shutdown of this client
     */
    public void shutdown() {

        _channel.eventLoop().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        _draining = true;
                        // TODO (JR) Implement this
                    }
                }
        );
    }

    /**
     * Performs appropriate setup
     * @param ctx The {@link ChannelHandlerContext} for this given channel
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

        // TODO (JR) Any setup operations
        _remoteAddress = (InetSocketAddress)ctx.channel().remoteAddress();

        // Use getHostString() to prevent a DNS lookup from happening
        _remoteIdentifier = _remoteAddress.getHostString();

        _logger.log(Level.FINE, "Connection opened with downstream: " + _remoteIdentifier);

        _channel = ctx.channel();

        // Use the established throttling settings
        _channel.config().setAutoRead(!_throttled);

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
            _downstreamClient = new DownstreamClient(this, _downstreamServer, _workerGroup);

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
     * Handle backpressure from the remote client so we can throttle reads from the {@link org.rahmanj.sandshrew.policy.DownstreamServer}
     *
     * @param ctx ChannelHandlerContext for this particular channel
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {

        // Check for toggle from previous state
        if (_writable != ctx.channel().isWritable()) {
            _writable = !_writable;

            if (_writable) {
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
     *
     * @param msg An {@link Object} to check
     * @return Returns true if the object is truly a {@link HttpMessage} object
     */
    protected boolean isHeader(Object msg) {
        return msg instanceof HttpMessage;
    }

    /**
     * Check if a given input is a {@link HttpContent} object
     *
     * @param msg {@link Object} to check
     * @return Returns true if the object is truly a {@link HttpContent} object
     */
    protected boolean isContent(Object msg) {
        return msg instanceof HttpContent;
    }

    /**
     * Shared {@link EventLoopGroup} used for all async background IO operations
     */
    private EventLoopGroup _workerGroup;

    /**
     * {@link Channel} this handler is managing
     */
    private Channel _channel;

    /**
     * {@link InetSocketAddress} for the remote upstream client
     */
    private InetSocketAddress _remoteAddress;

    /**
     * Track last writable state
     */
    private boolean _writable;

    /**
     * Track if we are draining down
     */
    private boolean _draining;

    /**
     * Information about the remote agent
     */
    private String _remoteIdentifier;

    /**
     * {@link org.rahmanj.sandshrew.policy.DownstreamServer} we are proxying for
     */
    private DownstreamServer _downstreamServer;

    /**
     * {@link ProxyChannel} to transmit data to the {@link DownstreamServer}
     */
    private ProxyChannel _downstreamClient;

    /**
     * {@link ChannelFuture} for the Close event on the downstream client
     */
    private ChannelFuture _downstreamClientFuture;

    /**
     * Tracks if the channel is read throttled or not
     */
    private boolean _throttled;



    private static final Logger _logger = Logger.getLogger(
            UpstreamHandler.class.getName()
    );
}
