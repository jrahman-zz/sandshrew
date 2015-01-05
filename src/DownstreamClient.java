import io.netty.bootstrap.Bootstrap;

import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Handler for sending the request from the proxy to the downstream client
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamClient {

    public DownstreamClient(ChannelHandlerContext upstreamChannel, DownstreamServer server, EventLoopGroup workerGroup) {
        _upstreamChannel = upstreamChannel;
        _downstreamServer = server;
        _workerGroup = workerGroup;
    }

    public void run() throws Exception {

        String hostname = _downstreamServer.getHostname();
        int port = _downstreamServer.getPort();

        try {

            Class socketChannelClass = NioSocketChannel.class;

            // Configure the downstream client to our liking
            Bootstrap b = new Bootstrap();
            b.group(_workerGroup)
                    .channel(socketChannelClass)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    // TODO (JR) Add configuration information
                    .handler(new DownstreamChannelInitializer());

            // Initiate the downstream connection
            ChannelFuture f = b.connect(hostname, port).sync();

            // Wait until the connection is closed
            // TODO, make this more comprehensive and robust
            f.channel().closeFuture().sync();

        } finally {
            _workerGroup.shutdownGracefully();
        }
    }

    /**
     * Channel back to the upstream server making the request
     */
    private ChannelHandlerContext _upstreamChannel;

    /**
     * Information about the downstream server satisfying the request
     */
    private DownstreamServer _downstreamServer;

    /**
     * Set of worker threads to perform async IO for use, shared with the ProxyServer
     * to maximize reuse of threads and reduce latency
     */
    private EventLoopGroup _workerGroup;
}
