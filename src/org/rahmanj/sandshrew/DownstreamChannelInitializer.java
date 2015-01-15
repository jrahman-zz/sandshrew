
package org.rahmanj.sandshrew;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.spdy.*;

/**
 * Initialzer class for the {@link org.rahmanj.sandshrew.policy.DownstreamServer} channel
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamChannelInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * Construct a {@link DownstreamChannelInitializer} instance
     * @param spdyHandler {@link ChannelHandler} for SPDY connections
     * @param version {@link SpdyVersion} to use
     */
    public DownstreamChannelInitializer(ChannelHandler spdyHandler, SpdyVersion version) {
        // TODO (JR) stash configuration information here

        _httpHandler = null;
        _spdyHandler = spdyHandler;

        // TODO (JR) Make this configurable
        _version = version;
    }

    /**
     * Construct a {@link DownstreamChannelInitializer} instance
     * @param httpHandler {@link ChannelHandler} for HTTP(S) connections
     */
    public DownstreamChannelInitializer(ChannelHandler httpHandler) {
        _httpHandler = httpHandler;
        _spdyHandler = null;
        _version = null;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {

        if (_spdyHandler != null) { // SPDY
            initSpdyChannel(ch);
        } else { // HTTP
            initHttpChannel(ch);
        }
    }

    /**
     * Configure a given channel for SPDY client use
     * @param ch The {@link SocketChannel} to initialze for use
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
     * Configure a given {@link SocketChannel} for HTTP client use
     * @param ch The {@link SocketChannel} to initialize for use
     */
    protected void initHttpChannel(SocketChannel ch) {

        ChannelPipeline pipeline = ch.pipeline();

        // TODO (JR) Make the first two configurable
        pipeline.addLast("httpContentCompressor", new HttpContentCompressor());
        pipeline.addLast("httpContentDecompressor", new HttpContentDecompressor());
        pipeline.addLast("httpClientCodec", new HttpClientCodec(MAX_HTTP_LINE_LENGTH, MAX_HTTP_HEADER_LENGTH, MAX_HTTP_CHUNK_LENGTH));
        pipeline.addLast("httpClientHandler", _httpHandler);
    }

    /**
     *
     */
    private static final int MAX_HTTP_LINE_LENGTH = 4 * 1024;

    /**
     *
     */
    private static final int MAX_SPDY_HEADER_LENGTH = 8 * 1024;

    /**
     *
     */
    private static final int MAX_HTTP_HEADER_LENGTH = 8 * 1024;

    /**
     *
     */
    private static final int MAX_SPDY_CONTENT_LENGTH = 1024 * 1024; // 1MB chunks

    /**
     *
     */
    private static final int MAX_HTTP_CHUNK_LENGTH = 1024 * 1024; // 1MB chunks




    /**
     * Store which version of SPDY we should use, if any
     */
    private SpdyVersion _version;

    private ChannelHandler _httpHandler;

    private ChannelHandler _spdyHandler;
}
