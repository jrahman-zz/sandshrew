package org.rahmanj.sandshrew.policy;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import java.net.InetSocketAddress;

/**
 *
 * Context surrounding a request for use by {@link RoutePolicy}
 *
 * @author Jason P. Rahman
 */
public class RequestContext {


    public RequestContext(HttpRequest request, Channel channel) {

        if (request == null) {
            throw new NullPointerException("Null request");
        }

        if (channel == null) {
            throw new NullPointerException("Null channel");
        }

        _uri = request.getUri();
        _remoteAddress = ((InetSocketAddress)channel.remoteAddress()).getHostString();
        _headers = request.headers();
    }

    public String getUri() {
        return _uri;
    }

    public String getRemoteAddress() {
        return _remoteAddress;
    }

    public String getHeaderField(String name) {
        return _headers.get(name);
    }

    public String getHeaderField(CharSequence name) {
        return getHeaderField(name.toString());
    }

    public boolean contains(String name) {
        return _headers.contains(name);
    }

    public boolean container(CharSequence name) {
        return contains(name.toString());
    }


    /**
     *
     */
    private String _uri;

    /**
     *
     */
    private String _remoteAddress;

    /**
     *
     */
    private HttpHeaders _headers;

}
