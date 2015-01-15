package org.rahmanj.sandshrew.policy;

import java.util.Vector;

/**
 *
 *
 * @author Jason P. Rahman
 */
public class IpHashPolicy {

    public IpHashPolicy() {
        _servers = new Vector<DownstreamServer>();
    }


    public DownstreamServer next(RequestContext ctx) {
        if (_servers.size() == 0) {
            throw new IllegalStateException("No servers added");
        }


        int idx = ctx.getUri().hashCode() % _servers.size();
        return _servers.get(idx);

        // TODO, what happens if addDownstreamServer() is called after we start hashing
        // We could have inconsistencies where a client maps to a different downstream server
        // following the addition of a new downstream server
    }

    /**
     * Add a new server to the {@link IpHashPolicy}
     *
     * @param server
     */
    public void addDownstreamServer(DownstreamServer server) {
        if (server == null) {
            throw new NullPointerException("Null server");
        }
        _servers.add(server);
    }

    /**
     * Vector of {@link DownstreamServer}s for the policy to use
     */
    private Vector<DownstreamServer> _servers;

}
