
package org.rahmanj.sandshrew;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;


/**
 * {@link ChannelInitializer} to initialize the {@link Channel} between the client and the proxy according to our configuration.
 * Uses {@link ProxySpdyOrHttpChooser} to handle protocol negotiation and final configuration details
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
class ProxyChannelInitializer<T extends Channel> extends ChannelInitializer<T> {

    /**
     * Construct a new {@link ProxyChannelInitializer} instance
     *
     * @param sslContext
     * @param workerGroup
     */
    public ProxyChannelInitializer(SslContext sslContext, EventLoopGroup workerGroup) {
        // TODO, later include some config stuff in here
        _sslContext = sslContext;
        _workerGroup = workerGroup;
    }

    /**
     * Initialize the {@link T} extends {@link Channel}
     *
     * @param ch
     * @throws Exception
     */
    @Override
    public void initChannel(T ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        // TODO (JR) make this configurable based on user desires
        p.addLast(_sslContext.newHandler(ch.alloc()));

        // Build pipeline between client and proxy
        // Note that ProxySpdyOrHttpChooser actually handles all the details
        // regarding how the pipeline is created
        UpstreamHandler handler = new UpstreamHandler(_workerGroup);
        p.addLast(new ProxySpdyOrHttpChooser(handler, handler));
    }

    /**
     * SSLContext for HTTPS and SPDY
     */
    private SslContext _sslContext;

    /**
     * Shared {@link EventLoopGroup} for all network IO. By sharing this worker group, we allow IO operations
     * to be cooperatively scheduled regardless of whether they are upstream or downstream operations
     */
    private EventLoopGroup _workerGroup;
}