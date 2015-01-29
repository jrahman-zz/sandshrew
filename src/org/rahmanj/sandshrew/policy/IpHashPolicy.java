package org.rahmanj.sandshrew.policy;

import com.fasterxml.jackson.databind.JsonNode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Vector;

/**
 *
 *
 * @author Jason P. Rahman
 */
public class IpHashPolicy implements RoutePolicy {

    public IpHashPolicy() {
        _servers = new Vector<ServerInfo>();
    }


    public ServerInfo next(RequestContext ctx) {
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
     * Add a new server to the {@link IpHashPolicy}
     *
     * @param server
     */
    public void addDownstreamServer(ServerInfo server, JsonNode serverNode) {
        if (server == null) {
            throw new NullPointerException("Null server");
        }

        _servers.add(server);
    }


    public static class IpHashPolicyFactory extends PolicyFactory {
        public RoutePolicy instantiatePolicy(JsonNode policyNode) {
            throw new NotImplementedException();
        }
    }

    /**
     * Vector of {@link ServerInfo}s for the policy to use
     */
    private Vector<ServerInfo> _servers;

}
