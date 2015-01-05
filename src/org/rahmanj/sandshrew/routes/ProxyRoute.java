
package org.rahmanj.sandshrew.routes;

import org.rahmanj.sandshrew.DownstreamServer;
import org.rahmanj.sandshrew.policy.ProxyPolicy;

/**
 * Define details of a given proxy route
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class ProxyRoute {

    /**
     * Construct a {@link ProxyRoute} instance
     * @param endpoint {@link ProxyEndpoint} publicly exposed endpoint for this route
     * @param policy The {@link ProxyPolicy} that decides which {@link DownstreamServer} should receive traffic
     */
    public ProxyRoute(ProxyEndpoint endpoint, ProxyPolicy policy) {
        _endpoint = endpoint;
        _policy = policy;
    }

    // TODO (JR) We need to add transformation and extraction so that postfixes of a wildcard
    // match can be postfixed to the end of the DownstreamServer postfix

    /**
     * Get the current proxy policy in effect
     * @return
     */
    public ProxyPolicy getPolicy() {
        return _policy;
    }

    /**
     * Get this route's public endpoint
     * @return
     */
    public ProxyEndpoint getPublicEndpoint() {
        return _endpoint;
    }

    /**
     * Unique identifier for the service defined by our endpoint
     */
    private ProxyEndpoint _endpoint;

    /**
     * Which policy this proxy route should follow when choosing among multiple downstream servers
     * Note that this object will encapsulate all possible downstream servers
     * If you wish to change the set
     */
    private ProxyPolicy _policy;

}
