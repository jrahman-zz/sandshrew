
import io.netty.buffer.ByteBuf;

import io.netty.channel.*;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Primary handler for incoming http and spdy requests
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * Construct an instance of the ProxyServerHandler using the given EventLoopGroup
     * @param workerGroup The shared EventLoopGroup to use for async IO
     */
    public ProxyServerHandler(EventLoopGroup workerGroup) {
        _workerGroup = workerGroup;
        _isWritable = true; // Sane default
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

        // TODO (JR) Any setup operations
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {

        // TODO (HR) Any teardown operations

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (isHeader(msg)) {
            HttpMessage req = (HttpMessage)msg;
            HttpHeaders header = req.headers();

            // TODO Determine the route
            ProxyRoute route;

            // TODO Update this??
            DownstreamServer _downstreamServer = route.getPolicy().selectDownstreamServer();
            DownstreamClient client = new DownstreamClient(ctx, _downstreamServer, _workerGroup);

            // TODO (JR) Find better way to run this
            try {
                client.run();
            } catch (Exception ex) {

            }
        }
    }

    /**
     * Handle backpressure from the client so we can throttle reads from the server
     * @param ctx ChannelHandlerContext for this particular channel
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {

        // Check for toggle from previous state
        if (_isWritable != ctx.channel().isWritable()) {
            _isWritable = !_isWritable;

            InetSocketAddress address = (InetSocketAddress)ctx.channel().remoteAddress();
            String remoteEntity = address.getHostString();

            if (_isWritable) {
                // TODO (JR) add identification information
                _logger.log(Level.FINE, "Backpressure from " + remoteEntity + " over, enabling reads from");
                 // TODO (JR) Reenable reads from the downstream server
            } else {
                // TODO (JR) add identification information
                _logger.log(Level.FINE, "Backpressure from " + remoteEntity + ", disabling reads from");
                // TODO (JR) Disable reads from the downsteam server
            }
        }

        // Forward on to the chain
        ctx.fireChannelWritabilityChanged();
    }

    /**
     * Handle the occurrence of an exception
     * @param ctx ChannelHandlerContext for this channel
     * @param cause Throwable raised by the error
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * Check if a given input is a HttpMessage
     * @param msg Object to check
     * @return Returns true if the object is truly an HttpMessage
     */
    protected boolean isHeader(Object msg) {
        return msg instanceof HttpMessage;
    }

    /**
     * Shared worker group used for all async background IO
     */
    private EventLoopGroup _workerGroup;

    /**
     * Track last writable state
     */
    private boolean _isWritable;

    /**
     * Downstream server we are proxying for
     */
    private DownstreamServer _downstreamServer;

    private static final Logger _logger = Logger.getLogger(
            ProxyServerHandler.class.getName()
    );
}
