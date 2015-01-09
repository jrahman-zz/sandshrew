
package org.rahmanj.sandshrew.policy;

import org.rahmanj.sandshrew.DownstreamServer;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Round Robin proxy policy
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class RoundRobinProxyPolicy implements ProxyPolicy {

    /**
     *
     */
    public RoundRobinProxyPolicy() {
        _servers = new ArrayDeque<DownstreamServer>();
    }

    /**
     *
     * @return
     */
    public DownstreamServer next() {
        if (_servers.size() == 0) {
            throw new IllegalStateException("No servers added");
        }

        DownstreamServer server = _servers.remove();
        _servers.add(server);
        return server;
    }

    /**
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
     * Store the servers for the Round Robin policy
     */
    private Queue<DownstreamServer> _servers;
}
