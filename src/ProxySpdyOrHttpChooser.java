import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.spdy.*;

import java.util.logging.Logger;

/**
 * Configure the pipeline for either SPDY or HTTP based on the client
 *
 * @author Jason P. Rahman
 */
class ProxySpdyOrHttpChooser extends SpdyOrHttpChooser {

    public ProxySpdyOrHttpChooser() {
        this(MAX_CONTENT_LENGTH, MAX_CONTENT_LENGTH);
    }

    public ProxySpdyOrHttpChooser(int maxSpdyContentLength, int maxHttpContentLength) {
        super(maxSpdyContentLength, maxHttpContentLength);
    }


    /**
     * Build HTTP response handling pipeline based on configuration
     * @param ctx
     */
    @Override
    protected void addHttpHandlers(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        p.addLast("httpContentCompressor", new HttpContentCompressor()); // TODO (JR) configurable
        p.addLast("httpRequestDecoder", new HttpRequestDecoder());
        p.addLast("httpResponseEncoder", new HttpResponseEncoder());
        p.addLast("httpChunkAggregator", new HttpObjectAggregator(maxHttpContentLength));
        // TODO (JR) finish this p.addLast("httpRequestHandler", null);
    }

    /**
     * Build the SPDY response handling pipeline based on configuration
     * @param ctx
     */
    @Override
    protected void addSpdyHandlers(ChannelHandlerContext ctx, SpdyVersion version) {

        ChannelPipeline p = ctx.pipeline();
        p.addLast("spdyFrameCodec", new SpdyFrameCodec(version));
        p.addLast("spdySessionHandler", new SpdySessionHandler(version, true));
        p.addLast("spdyHttpEncoder", new SpdyHttpEncoder(version));
        p.addLast("spdyHttpDecoder", new SpdyHttpDecoder(version, maxSpdyContentLength));
        p.addLast("spdyStreamIdHandler", new SpdyHttpResponseStreamIdHandler());
        // TODO (JR) finish this p.addLast("httpRequestHandler", null);
    }

    @Override
    protected ChannelInboundHandler createHttpRequestHandlerForHttp() {
        return null; // TODO (JR) Implement this
    }

    @Override
    protected ChannelInboundHandler createHttpRequestHandlerForSpdy() {
        return createHttpRequestHandlerForHttp();
    }

    private static final int MAX_CONTENT_LENGTH = 1024 * 100;

    private static final Logger _logger = Logger.getLogger(
            ProxySpdyOrHttpChooser.class.getName()
    );
}