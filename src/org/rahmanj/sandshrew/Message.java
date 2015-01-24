package org.rahmanj.sandshrew;

import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpObject;

/**
 * Wrapper object to store {@link io.netty.handler.codec.http.HttpObject}s and {@link io.netty.channel.ChannelPromise}s bundled together
 * in the message queue
 *
 * @author Jason P. Rahman
 */
class Message {
    public Message(HttpObject msg, ChannelPromise promise) {
        _msg = msg;
        _promise = promise;
    }

    public HttpObject getMessage() {
        return _msg;
    }

    public ChannelPromise getPromise() {
        return _promise;
    }

    private HttpObject _msg;
    private ChannelPromise _promise;
}
