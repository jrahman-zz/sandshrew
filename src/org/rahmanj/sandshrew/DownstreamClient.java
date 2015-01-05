
package org.rahmanj.sandshrew;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.spdy.SpdyVersion;

/**
 * Client for sending the request from the proxy to the downstream client. Most of the public methods should
 * only be called from the UpstreamHandler which owns this client and the thread in which that handler
 * is currently running
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamClient {

    /**
     * Construct an {@link DownstreamClient} instance
     *
     * @param upstreamChannel {@link UpstreamHandler} for the upstream channel
     * @param server {@link DownstreamServer} we are connecting to
     * @param workerGroup The shared {@link EventLoopGroup} that is backing our async IO
     */
    public DownstreamClient(UpstreamHandler upstreamChannel, DownstreamServer server, EventLoopGroup workerGroup) {
        _upstreamChannel = upstreamChannel;
        _downstreamServer = server;
        _workerGroup = workerGroup;
        _spdyVersion = null;

        commonInit();
    }

    /**
     * Construct an {@link DownstreamClient} instance
     * @param upstreamChannel {@link UpstreamHandler} for the upstream channel
     * @param server {@link DownstreamServer} we are connecting to
     * @param workerGroup The shared {@link EventLoopGroup} that is backing our async IO
     * @param spdyVersion The {@link SpdyVersion} to use if SPDY is requested
     */
    public DownstreamClient(UpstreamHandler upstreamChannel, DownstreamServer server, EventLoopGroup workerGroup, SpdyVersion spdyVersion) {
        _upstreamChannel = upstreamChannel;
        _downstreamServer = server;
        _workerGroup = workerGroup;
        _spdyVersion = spdyVersion;

        commonInit();
    }

    /**
     * Starts the {@link DownstreamClient} asynchronously
     *
     * @return Returns a {@link ChannelFuture} for the connection of the client and {@link DownstreamServer}
     * @throws Exception
     */
    public ChannelFuture run() throws Exception {

        String hostname = _downstreamServer.getHostname();
        int port = _downstreamServer.getPort();

        // TODO (JR) At some point update this with config information and a proper interface to the upstream channel
        _handler = new DownstreamHandler(_upstreamChannel, _downstreamServer);

        Class socketChannelClass = NioSocketChannel.class;

        // Configure the downstream client to our liking
        // TODO (JR) Are there any other options we would like to tune???
        _bootstrap = new Bootstrap();
        _bootstrap.group(_workerGroup)
                .channel(socketChannelClass)
                .option(ChannelOption.SO_KEEPALIVE, true);

        if (_spdyVersion == null) {
            // No SPDY
            _bootstrap.handler(new DownstreamChannelInitializer(_handler));
        } else {
            // Use SPDY
            _bootstrap.handler(new DownstreamChannelInitializer(_handler, _spdyVersion));
        }

        // Initiate the downstream connection
        return _bootstrap.connect(hostname, port);
    }

    /**
     * Asynchronously shutdown the client. Allows pending messages to be sent and any pending responses to
     * be sent to the upstream client
     * @return
     */
    public ChannelFuture shutdown() {
        _handler.shutdown();
    }


    public void send(HttpObject msg) {
        /** TODO (JR) Improve this by better tracking the state of the client
         * even in the face of concurrent operations and potential race conditions
         */

        // Forward onto the handler
        _handler.send(msg);
    }

    public void throttleReads(boolean read) {
        // This gets nasty because we have accesses to this from the DownstreamHandler thread
        // when the channel finally gets initialzed and we need to set it
        // But we also have access from the UpstreamHandler thread when it tries to set
        // the AutoRead value to throttle reads from the downstream server

        // TODO (JR) Implement this
    }

    /**
     * Default initializations
     */
    private void commonInit() {
        _handler = null;
        _bootstrap = null;
        _channel = null;
    }

    /**
     * Current auto read status
     */
    private boolean _autoRead;

    /**
     * {@link Channel} for this client
     */
    private Channel _channel;

    /**
     * {@link UpstreamHandler} for the upstream channel
     */
    private UpstreamHandler _upstreamChannel;

    /**
     * Information about the {@link DownstreamServer} satisfying the request
     */
    private DownstreamServer _downstreamServer;

    /**
     * {@link DownstreamHandler} for this {@link Channel}
     */
    private DownstreamHandler _handler;

    /**
     * Set of worker threads to perform async IO for use, shared with the {@link ProxyServer}
     * to maximize reuse of threads and reduce latency
     */
    private EventLoopGroup _workerGroup;

    /**
     * {@link SpdyVersion} to use if SPDY is requested
     */
    private SpdyVersion _spdyVersion;

    /**
     * Netty {@link Bootstrap} to use for this {@link DownstreamClient}
     */
    private Bootstrap _bootstrap;
}
