
package org.rahmanj.sandshrew;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for client connections to the downstream server.
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamHandler extends ChannelInboundHandlerAdapter {


    /**
     * Construct an instance of the {@link DownstreamHandler}
     *
     * @param upstreamChannel The {@link UpstreamHandler} for the upsteam channel
     * @param downstreamServer The {@link DownstreamServer} we are sending data to
     */
    public DownstreamHandler(UpstreamHandler upstreamChannel, DownstreamServer downstreamServer) {
        _upstreamChannelHandler = upstreamChannel;
        _downstreamServer = downstreamServer;
        _writable = true; // Sane default

        InetSocketAddress address = (InetSocketAddress)_upstreamChannelHandler.remoteAddress();
        _remoteIdentifier = address.getHostString(); // Dodge the DNS call with getHostString()

        _draindown = false;
        _messageQueue = new ArrayDeque<HttpObject>();
        _connected = false;
        _downstreamChannel = null;
    }

    public void send(HttpObject msg) {

        if (msg == null) {
            throw new NullPointerException("msg");
        }

        synchronized (_messageQueue) {
            if (!_draindown) {
                if (_connected && _writable && _messageQueue.size() == 0) {

                    // Immediately send the current message if possible
                    _downstreamChannel.write(msg);
                } else {

                    // Enqueue current message and send the next message as needed
                    _messageQueue.add(msg);
                    sendNextMessage();
                }

            } // else discard next requests
        }
    }

    /**
     *
     */
    public void shutdown() {

        // TODO (JR) Complete this
       _draindown = true;
    }

    /**
     * Start sending data onward since the connection has been established
     *
     * @param ctx The {@link ChannelHandlerContext} for this channel
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

        _connected = true;

        _downstreamChannel = ctx.channel();

        // Flush pending message
        sendNextMessage(ctx);

        // Forward if needed
        ctx.fireChannelActive();
    }

    /**
     * Perform appropriate teardown
     *
     * @param ctx The {@link ChannelHandlerContext} for this channel
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        _connected = false;

        // Forward if needed
        ctx.fireChannelInactive();
    }


    /**
     * Handle read events from the downstream server
     *
     * @param ctx The {@link ChannelHandlerContext} for this channel
     * @param msg A {@link HttpResponse} or {@link HttpContent} from the {@link DownstreamServer}
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        // TODO (JR) Forward onward to the other channel
    }

    /**
     * Record changes in channel writeability from the proxy to the server so that we can properly handle backpressure
     * from the server to the client.
     *
     * @param ctx {@link ChannelHandlerContext} for the current {@link Channel} and pipeline
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {

        // Check for toggle from previous state
        if (_writable != ctx.channel().isWritable()) {
            _writable = !_writable;

            if (_writable) {
                unthrottleClient();
            } else {
                throttleClient();
            }
        }

        // Send queued messages
        if (_writable) {
            sendNextMessage();
        }

        // Forward onward
        ctx.fireChannelWritabilityChanged();
    }

    /**
     *
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        // TODO (JR) Convert this to logging framework
        // TODO (JR) Have this do something to robustly respond
        cause.printStackTrace();

        // Have a better closure mechanism
        ctx.close();

        // TODO (JR) Is this safe?
        // _upstreamChannelHandler.close();
    }

    /**
     * Transfer the next message from the queue
     * @param ctx
     */
    protected void sendNextMessage() {
        HttpObject obj = null;
        synchronized (_messageQueue) {
            obj = _messageQueue.remove();
        }

        // TODO (JR) Check _writeable first

        if (obj != null) {
            sendMessage(obj);
        }
    }

    /**
     * Send a message to the DownstreamServer through the pipeline
     *
     * @param obj
     */
    protected void sendMessage(HttpObject obj) {
        if (_writable) {
            _downstreamChannel.write(obj);
        }

        // TODO (JR) Handle failure to write because of _writable
        // Or perhaps, we shouldn't even check for _writeable, just assume
        // That the rest of the class knows what it is doing
    }

    /**
     * Stop throttling the remote client if the DownstreamServer allows it
     */
    protected void unthrottleClient() {

        /** TODO (JR) Reconsider the global throttle count because
         * the problem is that only the last DownstreamClient
         * to unthrottle the server will see that the server
         * is no longer experiencing backpressure, so
         * a notification mechanism would be needed to inform all
         * DownstreamClients connected to the DownstreamServer
         * that the server was free again, which would be nasty
         */

        if (_downstreamServer.decrementThrottle() == 0) {
            _logger.log(Level.FINE, "Renabling reads from upstream: " + _remoteIdentifier);
            _upstreamChannelHandler.throttleClientReads(false);
        }
    }

    /**
     * Start throttling the remote client if the DownstreamServer requires it
     */
    protected void throttleClient() {
        _downstreamServer.incrementThrottle();
        _logger.log(Level.FINE, "Disabling reads from upstream: " + _remoteIdentifier);
        _upstreamChannelHandler.throttleClientReads(true);
    }

    /**
     * {@link UpstreamHandler} for the upstream channel data should be sent to
     */
    private UpstreamHandler _upstreamChannelHandler;

    /**
     * {@link Channel} between the proxy and the downstream server
     */
    private Channel _downstreamChannel;

    /**
     * {@link DownstreamServer} this client channel is proxying to
     */
    private DownstreamServer _downstreamServer;

    /**
     * Track if the connection is writable
     */
    private boolean _writable;

    /**
     * Store the address of the remote client
     */
    private String _remoteIdentifier;

    /**
     * Current draindown state, set to true when client should stop accepting new data, and should drain
     * it's current queue
     */
    private boolean _draindown;

    /**
     * Queue of objects to send
     */
    private Queue<HttpObject> _messageQueue;

    /**
     * Track current connection status
     */
    private boolean _connected;

    private static final Logger _logger = Logger.getLogger(
            DownstreamHandler.class.getName()
    );
}
