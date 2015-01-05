/**
 * Define details of a given proxy route
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class ProxyRoute {

    public ProxyRoute(ProxyEndpoint endpoint, ProxyPolicy policy) {
        _endpoint = endpoint;
        _policy = policy;
    }

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
