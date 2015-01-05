
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for downstream connections to the server
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamHandler extends ChannelInboundHandlerAdapter {
    

    public DownstreamHandler(ChannelHandlerContext upstreamChannel, DownstreamServer downstreamServer) {
        _upstreamChannel = upstreamChannel;
        _downstreamServer = downstreamServer;
        _isWritable = true; // Sane default
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {


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
     * ChannelHandlerContext for the upstream channel data should be sent to
     */
    private ChannelHandlerContext _upstreamChannel;

    /**
     * Downstream server this client channel is proxying to
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
