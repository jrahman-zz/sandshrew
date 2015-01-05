
package org.rahmanj.sandshrew;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.spdy.SpdyVersion;

/**
 * Handler for sending the request from the proxy to the downstream client. Most of the public methods should
 * only be called from the ProxyServerHandler which owns this client and the thread in which thathandler
 * is currently running
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamClient {

    /**
     * Construct an {@link DownstreamClient} instance
     * @param upstreamChannel {@link ChannelHandlerContext} for the upstream channel
     * @param server {@link DownstreamServer} we are connecting to
     * @param workerGroup The shared {@link EventLoopGroup} that is backing our async IO
     */
    public DownstreamClient(Channel upstreamChannel, DownstreamServer server, EventLoopGroup workerGroup) {
        _upstreamChannel = upstreamChannel;
        _downstreamServer = server;
        _workerGroup = workerGroup;
        _spdyVersion = null;

        commonInit();
    }

    /**
     * Construct an {@link DownstreamClient} instance
     * @param upstreamChannel {@link ChannelHandlerContext} for the upstream channel
     * @param server {@link DownstreamServer} we are connecting to
     * @param workerGroup The shared {@link EventLoopGroup} that is backing our async IO
     * @param spdyVersion The {@link SpdyVersion} to use if SPDY is requested
     */
    public DownstreamClient(Channel upstreamChannel, DownstreamServer server, EventLoopGroup workerGroup, SpdyVersion spdyVersion) {
        _upstreamChannel = upstreamChannel;
        _downstreamServer = server;
        _workerGroup = workerGroup;
        _spdyVersion = spdyVersion;

        commonInit();
    }

    /**
     * Start the asynchronous client
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

    public void throttleReads(boolean read) {
        // This gets nasty because we have accesses to this from the DownstreamHandler thread
        // when the channel finally gets initialzed and we need to set it
        // But we also have access from the ProxyServerHandler thread when it tries to set
        // the AutoRead value to throttle reads from the downstream server
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
     * Channel back to the upstream client making the request
     */
    private Channel _upstreamChannel;

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
