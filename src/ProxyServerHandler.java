
import io.netty.buffer.ByteBuf;

import io.netty.channel.*;

import java.util.logging.Logger;

/**
 *
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {

    public ProxyServerHandler(EventLoopGroup workerGroup) {
        _workerGroup = workerGroup;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {

        // TODO (JR) this will likely be scrapped
        final ByteBuf time = ctx.alloc().buffer(4);
        time.writeInt((int) (System.currentTimeMillis()));

        final ChannelFuture f = ctx.writeAndFlush(time);

        // Wait for the operation to complete before closing the context
        f.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) {
                assert f == future;

                // TODO (JR) Should we be doing this?
                ctx.close();
            }

        });
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ((ByteBuf)msg).release(); // Discard data silently
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * Shared worker group used for all async background IO
     */
    private EventLoopGroup _workerGroup;


    private static final Logger _logger = Logger.getLogger(
            ProxyServerHandler.class.getName()
    );
}
