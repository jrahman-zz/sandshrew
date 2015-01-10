
package org.rahmanj.sandshrew.routes;

import com.google.gson.annotations.SerializedName;

/**
 * External endpoint for the proxy server
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class ProxyEndpoint extends Service {

    public ProxyEndpoint(String hostname, int port, String pathPrefix) {
        super(hostname, port);
        _pathPrefix = pathPrefix;
    }


    public ProxyEndpoint(String hostname, String pathPrefix) {
        super(hostname, 80);
        _pathPrefix = pathPrefix;
    }

    /**
     * Prefix for paths based on this {@link ProxyEndpoint}
     */
    @SerializedName("PathPrefix")
    private String _pathPrefix;

    // TODO (JR) need to define additional parameters to fine tune proxy route details
}
