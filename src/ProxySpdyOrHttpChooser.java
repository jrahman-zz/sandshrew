import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.spdy.*;

import java.util.logging.Logger;

/**
 * Configure the pipeline for either SPDY or HTTP based on the client
 *
 * @author Jason P. Rahman
 */
class ProxySpdyOrHttpChooser extends SpdyOrHttpChooser {

    /**
     *
     * @param httpHandler
     * @param spdyHandler
     */
    public ProxySpdyOrHttpChooser(ChannelHandler httpHandler, ChannelHandler spdyHandler) {
        super(MAX_CONTENT_LENGTH, MAX_CONTENT_LENGTH);

        _httpHandler = httpHandler;
        _spdyHandler = spdyHandler;
    }

    /**
     *
     * @param httpHandler
     * @param spdyHandler
     * @param maxSpdyContentLength
     * @param maxHttpContentLength
     */
    public ProxySpdyOrHttpChooser(ChannelHandler httpHandler, ChannelHandler spdyHandler, int maxSpdyContentLength, int maxHttpContentLength) {
        super(maxSpdyContentLength, maxHttpContentLength);

        _httpHandler = httpHandler;
        _spdyHandler = spdyHandler;
    }


    /**
     * Build HTTP response handling pipeline based on configuration
     * @param ctx {@link ChannelHandlerContext} for the current channel
     */
    @Override
    protected void addHttpHandlers(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        p.addLast("httpContentCompressor", new HttpContentCompressor()); // TODO (JR) make configurable
        p.addLast("httpRequestDecoder", new HttpRequestDecoder());
        p.addLast("httpRequestHandler", null);

        /**
         * NOTE: We need to further consider the excessive overhead here in the different pipeline stages
         * On the client <-> proxy connection:
         *  For incoming messages, we do need a full HTTP pipeline, feeding HttpMessage and HttpContent objects
         *  For outgoing messages, we only need to feed raw bytes back across the wire
         * On the proxy <-> server connection:
         *  For outgoing messages, we simple need to feed raw bytes across the wire
         *  For incoming messages, we simply need to feed raw bytes across the wire
         *
         *  Additional considerations need to be made for cases when each connection has compression
         *  where we may be able to skip compression on one of the two connections to avoid decompressing
         *  and then subsequently recompressing the same data
         *  Mixed cases also need to be considered
         */
    }

    /**
     * Build the SPDY response handling pipeline based on configuration
     * @param ctx {@link ChannelHandlerContext} for the current channel
     */
    @Override
    protected void addSpdyHandlers(ChannelHandlerContext ctx, SpdyVersion version) {

        ChannelPipeline p = ctx.pipeline();
        p.addLast("spdyFrameCodec", new SpdyFrameCodec(version));
        p.addLast("spdySessionHandler", new SpdySessionHandler(version, true));
        p.addLast("spdyHttpObjectDecoder", new SpdyHeaderBlockRawDecoder(version, maxHttpHeaderLength));
        p.addLast("spdyStreamIdHandler", new SpdyHttpResponseStreamIdHandler());
        // TODO (JR) finish this p.addLast("httpRequestHandler", null);
    }

    /**
     *
     * @return
     */
    @Override
    protected ChannelInboundHandler createHttpRequestHandlerForHttp() {
        return _httpHandler;
    }

    /**
     *
     * @return
     */
    @Override
    protected ChannelInboundHandler createHttpRequestHandlerForSpdy() {
        return _spdyHandler;
    }

    private static final int MAX_CONTENT_LENGTH = 1024 * 100;

    private static final Logger _logger = Logger.getLogger(
            ProxySpdyOrHttpChooser.class.getName()
    );

    /**
     * {@link ChannelHandler} for HTTP and HTTPS connections
     */
    private ChannelHandler _httpHandler;

    /**
     * {@link ChannelHandler} for SPDY connections
     */
    private ChannelHandler _spdyHandler;
}