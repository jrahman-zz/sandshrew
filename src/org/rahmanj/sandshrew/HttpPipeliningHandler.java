
package org.rahmanj.sandshrew;

import io.netty.channel.*;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

/**
 *
 * @author
 */
public class HttpPipeliningHandler extends SimpleChannelInboundHandler<HttpRequest> {

    // TODO (JR) Fold this functionality into the main ProxyHandler

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpRequest req) {

        // TODO (JR) The request needs to go outbound first, then do this
        ChannelFuture future = ctx.write(null);

        if (!HttpHeaders.isKeepAlive(req)) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}
