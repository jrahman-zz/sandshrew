
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for client connections to the downstream server
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamHandler extends ChannelInboundHandlerAdapter {


    /**
     * Construct an instance of the {@link DownstreamHandler}
     * @param upstreamChannel The {@link ChannelHandlerContext} to send data upstream through
     * @param downstreamServer The {@link DownstreamServer} we are sending data to
     */
    public DownstreamHandler(ChannelHandlerContext upstreamChannel, DownstreamServer downstreamServer) {
        _upstreamChannel = upstreamChannel;
        _downstreamServer = downstreamServer;
        _isWritable = true; // Sane default
    }

    /**
     * Perform appropriate initialization
     * @param ctx The ChannelHandlerContext for this channel
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

        // Forward if needed
        ctx.fireChannelActive();
    }

    /**
     * Perform appropriate teardown
     * @param ctx The {@link ChannelHandlerContext} for this channel
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        // Forward if needed
        ctx.fireChannelInactive();
    }

    /**
     * Handle read events from the downstream server
     * @param ctx The {@link ChannelHandlerContext} for this channel
     * @param msg A {@link HttpResponse} or {@link HttpContent} from the {@link DownstreamServer}
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        // TODO (JR) Forward onward to the other channel

    }

    /**
     * Record changes in channel writability from the proxy to the server so that we can properly handle backpressure
     * from the server to the client
     * @param ctx
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {

        // Check for toggle from previous state
        if (_isWritable != ctx.channel().isWritable()) {
            _isWritable = !_isWritable;

            if (_isWritable) {
                if (_downstreamServer.decrementThrottle() == 0) {
                    // TODO (JR) add identification information
                    _logger.log(Level.FINE, "Renabling reads");
                    // TODO (JR) Reenable reads from the client
                }
            } else {
                _downstreamServer.incrementThrottle();
                // TODO (JR) add identification information
                _logger.log(Level.FINE, "Disabling reads");
                // TODO (JR) Disable reads from the client here
            }
        }

        // Forward onward
        ctx.fireChannelWritabilityChanged();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        // TODO (JR) Convert this to logging framework
        // TODO (JR) Have this do something to robustly respond
        cause.printStackTrace();
        ctx.close();

        // TODO (JR) Is this safe?
        _upstreamChannel.close();
    }

    /**
     * {@link ChannelHandlerContext} for the upstream channel data should be sent to
     */
    private ChannelHandlerContext _upstreamChannel;

    /**
     * {@link DownstreamServer} this client channel is proxying to
     */
    private DownstreamServer _downstreamServer;

    /**
     * Track if the connection is writable
     */
    private boolean _isWritable;

    private static final Logger _logger = Logger.getLogger(
            DownstreamHandler.class.getName()
    );
}
