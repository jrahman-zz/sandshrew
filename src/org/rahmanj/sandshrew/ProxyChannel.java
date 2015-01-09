package org.rahmanj.sandshrew;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpObject;

import java.net.InetSocketAddress;

/**
 * Consistent public interface for access to Upstream and Downstream channel
 *
 * @author Jason P. Rahman
 */
public interface ProxyChannel {

    /**
     * Throttle automatic reads from the {@link ProxyChannel}
     */
    public void throttle();

    /**
     * Unthrottles auto reading from the {@link ProxyChannel}
     */
    public void unthrottle();

    /**
     * Get the {@link InetSocketAddress} for the remote machine connected through this {@link ProxyChannel}
     *
     * @return Returns the {@link InetSocketAddress} for the remote machine if the connection has been established, null otherwise
     */
    public InetSocketAddress getRemoteAddress();

    /**
     * Determine if the {@link ProxyChannel} is able to write data
     *
     * @return Returns true if the {@link ProxyChannel} is writable, false otherwise
     */
    public boolean isWritable();

    /**
     * Determine if the {@link ProxyChannel} is currently shutting down and draining out
     *
     * @return Returns true if the {@link ProxyChannel} is draining, false otherwise
     */
    public boolean isDraining();

    /**
     * Send a given {@link HttpObject} over the {@link ProxyChannel}. This method is asynchronous
     *
     * @param msg The {@link HttpObject} to send over the {@link ProxyChannel}
     */
    public void send(HttpObject msg);

    /**
     * Send a given {@link HttpObject} over the {@link ProxyChannel}. This method is asynchronous
     *
     * @param msg The {@link HttpObject} to send over the {@link ProxyChannel}
     * @param promise A {@ChannelPromise} to be triggered when the {@link HttpObject} is sent
     */
    public void send(HttpObject msg, ChannelPromise promise);

    /**
     * Asynchronously start the given {@link ProxyChannel}
     */
    public ChannelFuture run();

    /**
     * Asynchronously shutdown the given {@link ProxyChannel}
     */
    public void shutdown();
}
