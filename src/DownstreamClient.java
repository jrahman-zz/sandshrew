import io.netty.bootstrap.Bootstrap;

import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Handler for sending the request from the proxy to the downstream client
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
    public DownstreamClient(ChannelHandlerContext upstreamChannel, DownstreamServer server, EventLoopGroup workerGroup) {
        _upstreamChannel = upstreamChannel;
        _downstreamServer = server;
        _workerGroup = workerGroup;
        _handler = null;
    }

    /**
     * Start the asynchronous client
     * @return Returns a {@link ChannelFuture} for the closure of the client {@link Channel}
     * @throws Exception
     */
    public ChannelFuture run() throws Exception {

        String hostname = _downstreamServer.getHostname();
        int port = _downstreamServer.getPort();

        // TODO (JR) At some point update this with config information and a proper interface to the upstream channel
        _handler = new DownstreamHandler(_upstreamChannel, _downstreamServer);

        Class socketChannelClass = NioSocketChannel.class;

        // Configure the downstream client to our liking
        Bootstrap b = new Bootstrap();
        b.group(_workerGroup)
                .channel(socketChannelClass)
                .option(ChannelOption.SO_KEEPALIVE, true)
                // TODO (JR) Add configuration information
                .handler(new DownstreamChannelInitializer(_handler, _handler));

        // Initiate the downstream connection
        // TODO (JR) does this connection need to be asynchronous, looks synchronous right now??
        ChannelFuture f = b.connect(hostname, port).sync();

        // Wait until the connection is closed
        // TODO, make this more comprehensive and robust
        return f.channel().closeFuture();
    }

    /**
     * Channel back to the upstream server making the request
     */
    private ChannelHandlerContext _upstreamChannel;

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
}
