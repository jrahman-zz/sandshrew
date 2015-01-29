
package org.rahmanj.sandshrew.policy;

import com.fasterxml.jackson.databind.JsonNode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Round Robin proxy policy
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class RoundRobinRoutePolicy implements RoutePolicy {

    /**
     *
     */
    public RoundRobinRoutePolicy() {
        _servers = new ArrayDeque<ServerInfo>();
    }

    /**
     *
     * @return
     */
    public ServerInfo next(RequestContext ctx) {
        if (_servers.size() == 0) {
            throw new IllegalStateException("No servers added");
        }

        ServerInfo server = _servers.remove();
        _servers.add(server);
        return server;
    }

    /**
     * Mark currently added server as failed and unavailable
     *
     * @param server {@link ServerInfo} about the server we have updated the status of
     */
    public void markAsFailed(ServerInfo server) {
        throw new NotImplementedException();
    }

    /**
     * Mark currently added server as alive and available
     *
     * @param server {@link ServerInfo} about the server we have updated the status of
     */
    public void markAsLive(ServerInfo server) {
        throw new NotImplementedException();
    }

    /**
     * Add a new server to a given route policy
     *
     * @param server Shared {@link ServerInfo} for the route entry
     * @param node {@link JsonNode} from the route entry that the {@link ServerInfo} object is associated with
     */
    public void addDownstreamServer(ServerInfo server, JsonNode node) {

        if (server == null) {
            throw new NullPointerException("Null server");
        }

        if (node == null) {
            throw new NullPointerException("Null node");
        }

        _servers.add(server);
    }

    public class RoundRobinPolicyFactory extends PolicyFactory {
        public RoutePolicy instantiatePolicy(JsonNode policyNode) {
            throw new NotImplementedException();
        }
    }

    /**
     * Store the servers for the Round Robin policy
     */
    private Queue<ServerInfo> _servers;
}
