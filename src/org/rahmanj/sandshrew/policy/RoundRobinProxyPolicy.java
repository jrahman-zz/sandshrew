
package org.rahmanj.sandshrew.policy;

import com.fasterxml.jackson.databind.JsonNode;

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
    public DownstreamServer next(RequestContext ctx) {
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
    public void addDownstreamServer(DownstreamServer server, JsonNode node) {

        if (server == null) {
            throw new NullPointerException("Null server");
        }

        if (node == null) {
            throw new NullPointerException("Null node");
        }



        JsonNode value = node.get("weight");
        if ()

        _servers.add(server);
    }

    /**
     * Store the servers for the Round Robin policy
     */
    private Queue<DownstreamServer> _servers;
}
