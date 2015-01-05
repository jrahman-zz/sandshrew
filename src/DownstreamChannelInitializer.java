import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.spdy.*;

/**
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamChannelInitializer extends ChannelInitializer<SocketChannel> {

    public DownstreamChannelInitializer(ChannelHandler httpHandler, ChannelHandler spdyHandler) {
        // TODO (JR) stash configuration information here

        _httpHandler = httpHandler;
        _spdyHandler = spdyHandler;

        // TODO (JR) Make this configurable
        _version = SpdyVersion.SPDY_3_1; // Default to latest version
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {

        if (true) { // SPDY
            initSpdyChannel(ch);
        } else { // HTTP
            initHttpChannel(ch);
        }
    }

    /**
     * Configure a given channel for SPDY client use
     * @param ch
     */
    protected void initSpdyChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("spdyFrameCodec", new SpdyFrameCodec(_version));
        pipeline.addLast("spdySessionHandler", new SpdySessionHandler(_version, false)); // TODO (JR) What does false mean?
        pipeline.addLast("spdyHttpEncoder", new SpdyHttpEncoder(_version));
        pipeline.addLast("spdyHttpDecoder", new SpdyHttpDecoder(_version, MAX_SPDY_CONTENT_LENGTH));
        pipeline.addLast("spdyStreamIdHandler", new SpdyClientStreamIdHandler());
        pipeline.addLast("spdyClientHandler", _spdyHandler);
    }

    /**
     * Configure a given channel for HTTP client use
     * @param ch
     */
    protected void initHttpChannel(SocketChannel ch) {

        ChannelPipeline pipeline = ch.pipeline();

        // TODO (JR) Make the first two configurable
        pipeline.addLast("httpContentCompressor", new HttpContentCompressor());
        pipeline.addLast("httpContentDecompressor", new HttpContentDecompressor());

        pipeline.addLast("httpClientCodec", new HttpClientCodec());
        pipeline.addLast("httpClientHandler", _httpHandler);

    }

    /**
     *
     */
    private static final int MAX_SPDY_CONTENT_LENGTH = 1024 * 1024;

    /**
     * Store which version of SPDY we should use
     */
    private SpdyVersion _version;

    private ChannelHandler _httpHandler;

    private ChannelHandler _spdyHandler;
}
