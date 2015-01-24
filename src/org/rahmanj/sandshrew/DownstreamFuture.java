package org.rahmanj.sandshrew;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.rahmanj.sandshrew.DownstreamClient;

import java.util.concurrent.TimeUnit;

/**
 * Future representing a downstream operation
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamFuture extends Future<DownstreamClient> {

    DownstreamFuture(ChannelFuture future, DownstreamClient client) {
        _future = future;
    }

    public boolean isDone() {
        return _future.isDone();
    }


    public boolean awaitUninterruptibly(long time, TimeUnit unit) {
        return _future.awaitUninterruptibly(time, unit);
    }

    public Throwable cause() {
        return _future.cause();
    }

    public Future<DownstreamClient> syncUninterruptibly()  {
        _future.syncUninterruptibly();
        return this;
    }

    public DownstreamClient getNow() {
        if (_future.getNow() != null) {
            return _client;
        }
        return null;
    }

    public Future<DownstreamClient> addListeners(GenericFutureListener<Future<DownstreamClient>>... listeners) {
        _future.addListeners(listeners);
        return this;
    }

    /**
     * Wrapper around our future
     */
    private Future _future;
    private DownstreamClient _client;
}
