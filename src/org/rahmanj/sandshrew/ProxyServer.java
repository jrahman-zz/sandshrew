
package org.rahmanj.sandshrew;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.handler.ssl.SslContext;

import org.eclipse.jetty.npn.NextProtoNego;
import org.rahmanj.sandshrew.config.RouteConfig;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.logging.Logger;


/**
 * Top level class for the proxy server
 * Creates the appropriate Netty {@link EventLoopGroup}s and other data structures, configures them, and starts listening
 * for incoming connections to proxy
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class ProxyServer {

    /**
     * Create a proxy server listening on a given port
     *
     * @param address The {@link SocketAddress} to bind this {@link SocketAddress} to listen for connections on
     * @param config The {@link RouteConfig} for the server
     * @param bossGroup The shared {@link EventLoopGroup} to use listen for incoming connections with
     * @param workerGroup The shared {@link EventLoopGroup} to use for handling connections
     * @param serverSocketChannelClass The {@link Class} to use for the server {@link SocketChannel}
     */
    public ProxyServer(SocketAddress address, RouteConfig config, EventLoopGroup bossGroup, EventLoopGroup workerGroup, Class serverSocketChannelClass) {
        _address = _address;
        _bossGroup = bossGroup;
        _workerGroup = workerGroup;
        _config = config;

        // TODO (JR) Init the SSLContext
        _sslContext = null;

        _bootstrap = new ServerBootstrap();
        _serverSocketChannelClass = serverSocketChannelClass;

        _initializer = new ProxyChannelInitializer(_sslContext, _workerGroup, _config);
    }

    /**
     * Run the given {@link ProxyServer}, blocks until the server finishes running
     * 
     * @return A {@link ChannelFuture} that can be waited upon to indicate closure of the listening socket
     * @throws {@link Exception}
     */
    public ChannelFuture run() throws Exception {

       _bootstrap.group(_bossGroup, _workerGroup)
                .channel(_serverSocketChannelClass)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // This is where we need to handle configuration changes, we reset this
                .childHandler(_initializer);

        // Bind the accepting socket and start running
        // TODO (JR) This looks like it's blocking, does it need to be made asynchronous??;
        ChannelFuture f = _bootstrap.bind(_address);
        f.sync();

        // Wait until the server socket is closed
        // In this example this will not happen, but
        // it can be done to gracefully shut the server down
        return f.channel().closeFuture();
    }

    /**
     *
     * @param config
     */
    public void updateConfig(RouteConfig config) {
        _config = config;
        _initializer.updateConfig(config);
    }

    /**
     * Port this given proxy should listen on
     */
    private SocketAddress _address;

    /**
     * Globally shared {@link EventLoopGroup} for the listening socket
     */
    private EventLoopGroup _bossGroup;

    /**
     * Gloablly shared {@link EventLoopGroup} for the client sockets
     */
    private EventLoopGroup _workerGroup;

    /**
     * SSL Context for HTTPS and SPDY use
     */
    private final SslContext _sslContext;

    /**
     * Main {@link ServerBootstrap} for the proxy
     */
    private ServerBootstrap _bootstrap;

    /**
     * {@link ProxyChannelInitializer} for the proxy channel
     */
    private ProxyChannelInitializer _initializer;

    /**
     * {@link RouteConfig} for the server
     */
    private RouteConfig _config;

    /**
     * Implementation {@link Class} for the {@link ServerSocketChannel}
     */
    private Class _serverSocketChannelClass;

    private static final Logger _logger = Logger.getLogger(
            ProxyServer.class.getName()
    );
}
