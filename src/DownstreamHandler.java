
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Logger;

/**
 * Handler for downstream connections to the server
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamHandler extends ChannelInboundHandlerAdapter {
    

    public DownstreamHandler(ChannelHandlerContext upstreamChannel) {
        _upstreamChannel = upstreamChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {


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


    private static final Logger _logger = Logger.getLogger(
            DownstreamHandler.class.getName()
    );
}
