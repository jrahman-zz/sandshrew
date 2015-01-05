
package org.rahmanj.sandshrew;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;


/**
 * Create the channel between the client and the proxy according to our configuration.
 * Uses {@link org.rahmanj.sandshrew.ProxySpdyOrHttpChooser} to handle protocol negotiation
 * and final configuration details
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
class ProxyChannelInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * Construct a new {@link ProxyChannelInitializer} instance
     * @param sslContext
     * @param workerGroup
     */
    public ProxyChannelInitializer(SslContext sslContext, EventLoopGroup workerGroup) {
        // TODO, later include some config stuff in here
        _sslContext = sslContext;
        _workerGroup = workerGroup;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        // TODO (JR) make this configurable based on user desires
        p.addLast(_sslContext.newHandler(ch.alloc()));

        // Build pipeline between client and proxy
        // Note that ProxySpdyOrHttpChooser actually handles all the details
        // regarding how the pipeline is created
        // TODO (JR) Get the appropriate handler in here to inject into the constructor
        p.addLast(new ProxySpdyOrHttpChooser());
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