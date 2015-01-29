
package org.rahmanj.sandshrew;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.spdy.SpdyVersion;
import org.rahmanj.sandshrew.policy.ServerInfo;
import org.rahmanj.sandshrew.policy.ThrottleListener;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client for connections to a {@link org.rahmanj.sandshrew.policy.ServerInfo}.
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamClient extends ChannelInboundHandlerAdapter implements ProxyChannel, ThrottleListener {


    /**
     * Construct an {@link DownstreamClient} instance
     *
     * @param upstreamChannel The {@link ProxyChannel} for the upstream channel
     * @param server The {@link org.rahmanj.sandshrew.policy.ServerInfo} we are connecting to
     * @param workerGroup The shared {@link EventLoopGroup} that is backing our async IO operations
     */
    public DownstreamClient(ProxyChannel upstreamChannel, ServerInfo server, EventLoopGroup workerGroup) {
        _upstreamChannel = upstreamChannel;
        _downstreamServer = server;
        _workerGroup = workerGroup;
        _spdyVersion = null;

        commonInit();
    }

    /**
     * Construct an {@link DownstreamClient} instance
     *
     * @param upstreamChannel The {@link ProxyChannel} for the upstream channel
     * @param server The {@link org.rahmanj.sandshrew.policy.ServerInfo} we are connecting to
     * @param workerGroup The shared {@link EventLoopGroup} that is backing our async IO operations
     * @param spdyVersion The {@link SpdyVersion} to use if SPDY is requested
     */
    public DownstreamClient(ProxyChannel upstreamChannel, ServerInfo server, EventLoopGroup workerGroup, SpdyVersion spdyVersion) {
        _upstreamChannel = upstreamChannel;
        _downstreamServer = server;
        _workerGroup = workerGroup;
        _spdyVersion = spdyVersion;

        commonInit();
    }


    /**
     * Starts the {@link DownstreamClient} asynchronously
     *
     * @return Returns a {@link ChannelFuture} for the connection of the client to the {@link org.rahmanj.sandshrew.policy.ServerInfo}
     * @throws Exception
     */
    public ChannelFuture run() {

        String hostname = _downstreamServer.getHostname();
        int port = _downstreamServer.getPort();

        Class socketChannelClass = NioSocketChannel.class;

        // Configure the downstream client to our liking
        // TODO (JR) Are there any other options we would like to tune???
        _bootstrap = new Bootstrap();
        _bootstrap.group(_workerGroup)
                .channel(socketChannelClass)
                .option(ChannelOption.SO_KEEPALIVE, true);

        if (_spdyVersion == null) {
            // No SPDY
            _bootstrap.handler(new DownstreamChannelInitializer(this));
        } else {
            // Use SPDY
            _bootstrap.handler(new DownstreamChannelInitializer(this, _spdyVersion));
        }

        // Initiate the downstream connection
        return _bootstrap.connect(hostname, port);
    }

    /**
     * Send a given {@link HttpObject} over the {@link ProxyChannel}. This method is asynchronous.
     *
     * @param msg The {@link HttpObject} to send over the {@link ProxyChannel}
     */
    public void send(final HttpObject msg) {

        if (msg == null) {
            throw new NullPointerException("msg");
        }

        _channel.eventLoop().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        if (!_draindown) {
                            if (_connected && _writable && _messageQueue.size() == 0) {

                                // Immediately send the current message if possible
                                _channel.write(msg);
                            } else {

                                // Enqueue current message and send the next message as needed
                                _messageQueue.add(new Message(msg, null));
                                sendNextMessage();
                            }

                        } // TODO (JR) else discard next requests
                    }
                }
        );
    }

    /**
     * Send a given {@link HttpObject} over the {@link ProxyChannel}. This method is asynchronous.
     *
     * @param msg The {@link HttpObject} to send over the {@link ProxyChannel}
     * @param promise A {@ChannelPromise} to be triggered when the {@link HttpObject} is sent
     */
    public void send(final HttpObject msg, final ChannelPromise promise) {

        _channel.eventLoop().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        if (!_draindown) {
                            if (_connected && _writable && _messageQueue.size() == 0) {

                                // Immediately send the current message if possible
                                _channel.write(msg, promise);
                            } else {

                                // Enqueue current message and send the next message as needed
                                _messageQueue.add(new Message(msg, promise));
                                sendNextMessage();
                            }
                        }
                    }
                }
        );
    }

    /**
     * Throttles automatic reading from this channel. Delegate the counting of throttles to the shared
     * {@link org.rahmanj.sandshrew.policy.ServerStats} instance so we have globally shared throttling
     */
    public void throttle() {
        _downstreamServer.incrementThrottle();
    }

    /**
     * Unthrottles automatic reading from this channel. Delegate the counting of throttles to the shared
     * {@link org.rahmanj.sandshrew.policy.ServerStats} instance so we have globally shared throttling
     */
    public void unthrottle() {
        _downstreamServer.decrementThrottle();
    }

    /**
     * Triggered when the downstream server becomes throttled.
     * This callback will be invoked from a different threads (probably)
     * so we run a Runnable inside the channel event loop for concurrency control
     */
    public void onThrottle() {
        _channel.eventLoop().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        _throttleCount++;
                        if (_channel != null && _throttleCount > 0) {
                            _logger.fine("Throttling channel");
                            _channel.config().setAutoRead(false);
                        }
                    }
                }
        );
    }

    /**
     * Triggered when the downstream server becomes throttled
     * This callback will be invokved from a different threads (probably)
     * so we run a Runnable inside the channel event loop for concurrency control
     */
    public void onStopThrottle() {
        _channel.eventLoop().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        if (_throttleCount > 0) {
                            _throttleCount--;
                        }

                        if(_channel != null && _throttleCount == 0) {
                            _logger.fine("Unthrottling channel");
                            _channel.config().setAutoRead(true);
                        }
                    }
                }
        );
    }

    /**
     * Checks if the connection is currently writable
     *
     * @return Returns true if the connection is writable, false otherwise
     */
    public boolean isWritable() {
        return _writable;
    }

    /**
     * Checks if the {@link DownstreamClient} is draining
     * @return
     */
    public boolean isDraining() {
        return _draindown;
    }

    /**
     * Get the {@link InetSocketAddress} of the downstream server connected to the {@link Channel} controlled by this {@link DownstreamClient}
     *
     * @return Returns an {@link InetSocketAddress} is the connection is established, null otherwise
     */
    public InetSocketAddress getRemoteAddress() {
        return _remoteAddress;
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
        _channel = ctx.channel();

        _remoteAddress = (InetSocketAddress)_channel.remoteAddress();

        // Flush pending message
        sendNextMessage();

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
     * @param msg A {@link HttpResponse} or {@link HttpContent} from the {@link org.rahmanj.sandshrew.policy.ServerInfo}
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof HttpObject) {
            _upstreamChannel.send((HttpObject) msg);

            if (msg instanceof  HttpResponse) {

            }

        } else {
            _logger.log(Level.FINE, "Read non-HttpObject");
        }
    }

    /**
     * Record changes in channel writability from the proxy to the server so that we can properly handle backpressure
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
     */
    protected void sendNextMessage() {
        Message msg = null;
        HttpObject http = null;
        ChannelPromise promise = null;

        if (_writable) {
            msg = _messageQueue.remove();
            sendMessage(http, promise);

            /**
             * If more messages are queued, send them out ASAP
             * Note that we cooperatively use the eventLoop() event queue
             * instead of a loop here so we cooperate with other events that
             * may need to run on the loop, such as writablity changed events,
             * or other types of IO event.
             */
            if (_messageQueue.size() > 0) {
                _channel.eventLoop().schedule(
                        new Runnable() {
                            @Override
                            public void run() {
                                sendNextMessage();
                            }
                        },
                        1,
                        TimeUnit.MILLISECONDS
                );
            }
        }

        /**
         * Don't worry about the else part here
         * Once writability changes, the callback channelWritabilityChanged()
         * will call sendNextMessage() and this process will kick off normally
         */
    }

    /**
     * Send a message to the DownstreamServer through the pipeline. Here we assume we do not need to synchronize
     *
     * @param msg The {@link HttpObject} to send
     * @param promise The {@link ChannelPromise} to notify if non-null
     */
    protected void sendMessage(HttpObject msg, ChannelPromise promise) {
        if (promise != null) {
            _channel.write(msg, promise);
        } else {
            _channel.write(msg);
        }

        // TODO (JR) Handle failure to write because of _writable
        // Or perhaps, we shouldn't even check for _writable, just assume
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
            _upstreamChannel.unthrottle();
        }
    }

    /**
     * Start throttling the remote client if the DownstreamServer requires it
     */
    protected void throttleClient() {
        _downstreamServer.incrementThrottle();
        _logger.log(Level.FINE, "Disabling reads from upstream: " + _remoteIdentifier);
        _upstreamChannel.throttle();
    }

    /**
     * Default initializations for {@link DownstreamClient}
     */
    private void commonInit() {
        _bootstrap = null;
        _channel = null;

        _writable = true; // Sane default

        InetSocketAddress address = _upstreamChannel.getRemoteAddress();
        _remoteIdentifier = address.getHostString(); // Dodge the DNS call with getHostString()

        _draindown = false;
        _messageQueue = new ArrayDeque<Message>();
        _connected = false;
        _channel = null;
        _remoteAddress = null;
    }

    /**
     * Set of worker threads to perform async IO for use, shared with the {@link ProxyServer}
     * to maximize reuse of threads and reduce latency
     */
    private EventLoopGroup _workerGroup;

    /**
     * {@link io.netty.handler.codec.spdy.SpdyVersion} to use if SPDY is requested
     */
    private SpdyVersion _spdyVersion;

    /**
     * Netty {@link Bootstrap} to use for this {@link DownstreamClient}
     */
    private Bootstrap _bootstrap;

    /**
     * {@link ProxyChannel} for the upstream channel data should be sent to
     */
    private ProxyChannel _upstreamChannel;

    /**
     * {@link Channel} between the proxy and the downstream server
     */
    private Channel _channel;

    /**
     * {@link InetSocketAddress} for the remote {@link org.rahmanj.sandshrew.policy.ServerInfo}
     */
    private InetSocketAddress _remoteAddress;

    /**
     * {@link org.rahmanj.sandshrew.policy.ServerInfo} this client channel is proxying to
     */
    private ServerInfo _downstreamServer;

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
    private Queue<Message> _messageQueue;

    /**
     * Track current connection status
     */
    private boolean _connected;

    /**
     * Track the throttle requests on this given downstream connection.
     */
    private int _throttleCount;

    private static final Logger _logger = Logger.getLogger(
            DownstreamClient.class.getName()
    );
}
