

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;

import org.eclipse.jetty.npn.NextProtoNego;

import java.util.logging.Logger;


/**
 * Top level class for the proxy server
 * Creates the appropriate Netty EventLoopGroups and other data structures, configures them, and starts listening
 * for incoming connections to proxy
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class ProxyServer {

    private int port;

    public ProxyServer(int port) {
        _port = port;
        _bossGroup = new NioEventLoopGroup();
        _workerGroup = new NioEventLoopGroup();

        // TODO (JR) Init the SSLContext
        _sslContext = null;

        _bootstrap = new ServerBootstrap();
    }

    public void run() throws Exception {

        try {

            Class serverSocketChannelClass = NioServerSocketChannel.class;

            // TODO refactor dis mess
            _bootstrap.group(_bossGroup, _workerGroup)
                    .channel(serverSocketChannelClass)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // This is where we need to handle configuration changes, we reset this
                    .childHandler(new ChannelInitializer(_sslContext, _workerGroup));

            // Bind the accepting socket and start running
            ChannelFuture f = _bootstrap.bind(port).sync();

            // Wait until the server socket is closed
            // In this example this will not happen, but
            // it can be done to gracefully shut the server down
            f.channel().closeFuture().sync();

        } finally {
            _workerGroup.shutdownGracefully();
            _bossGroup.shutdownGracefully();
        }

    }


    // TODO (JR) move this out to a different file
    public static void main(String[] args) throws Exception {
        int port;

        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }

        new ProxyServer(port).run();

    }

    /**
     * Port this given proxy should listen on
     */
    int _port;

    /**
     * EventLoopGroup for the listening socket
     */
    private EventLoopGroup _bossGroup;

    /**
     * EventLoopGroup for the client sockets
     */
    private EventLoopGroup _workerGroup;

    /**
     * SSL Context for HTTPS and SPDY use
     */
    private final SslContext _sslContext;

    /**
     * Main server bootstrap for the proxy
     */
    private ServerBootstrap _bootstrap;

    private static final Logger _logger = Logger.getLogger(
            ProxyServer.class.getName()
    );
}
