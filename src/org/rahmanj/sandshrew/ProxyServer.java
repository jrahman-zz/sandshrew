
package org.rahmanj.sandshrew;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.handler.ssl.SslContext;

import org.eclipse.jetty.npn.NextProtoNego;

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
     * @param port The port on which to listen for incoming connections
     * @param bossGroup The shared {@link EventLoopGroup} to use listen for incoming connections with
     * @param workerGroup The shared {@link EventLoopGroup} to use for handling connections
     * @param serverSocketChannelClass The {@link Class} to use for the server {@link SocketChannel}
     */
    public ProxyServer(int port, EventLoopGroup bossGroup, EventLoopGroup workerGroup, Class serverSocketChannelClass) {
        _port = port;
        _bossGroup = bossGroup;
        _workerGroup = workerGroup;

        // TODO (JR) Init the SSLContext
        _sslContext = null;

        _bootstrap = new ServerBootstrap();
        _serverSocketChannelClass = serverSocketChannelClass;
    }

    /**
     * Run the given proxy server, blocks until the server finishes running
     * @return A {@link ChannelFuture} that can be waited upon to indicate closure of the listening socket
     * @throws Exception
     */
    public ChannelFuture run() throws Exception {

       _bootstrap.group(_bossGroup, _workerGroup)
                .channel(_serverSocketChannelClass)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // This is where we need to handle configuration changes, we reset this
                .childHandler(new ProxyChannelInitializer(_sslContext, _workerGroup));

        // Bind the accepting socket and start running
        // TODO (JR) This looks like it's blocking, does it need to be made asynchronous??
        ChannelFuture f = _bootstrap.bind(_port).sync();

        // Wait until the server socket is closed
        // In this example this will not happen, but
        // it can be done to gracefully shut the server down
        return f.channel().closeFuture();
    }

    /**
     * Port this given proxy should listen on
     */
    int _port;

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
     * Implementation {@link Class} for the {@link ServerSocketChannel}
     */
    private Class _serverSocketChannelClass;

    private static final Logger _logger = Logger.getLogger(
            ProxyServer.class.getName()
    );
}
