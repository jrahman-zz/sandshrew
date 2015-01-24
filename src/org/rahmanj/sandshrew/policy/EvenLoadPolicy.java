package org.rahmanj.sandshrew.policy;

import com.fasterxml.jackson.databind.JsonNode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Policy based on even distribution of pending requests
 *
 * TODO, consider rewriting the next() method to only update the ordering after a certain interval
 *
 * @author Jason P. Rahman
 */
public class EvenLoadPolicy implements ProxyPolicy {

    public EvenLoadPolicy() {
        _sortedServers = new TreeMap<Long, ServerInfo>();
        _servers = new ArrayList<ServerInfo>();
    }

    /**
     *
     * @param ctx
     * @return
     */
    public ServerInfo next(RequestContext ctx) {
        if (ctx == null) {
            throw new NullPointerException("Null request context");
        }

        if (_servers.size() == 0) {
            throw new IllegalStateException("No servers added");
        }

        // Sort all the servers according to the pending request count and fetch the first
        _sortedServers.clear();
        for (ServerInfo server : _servers) {
            _sortedServers.put(server.stats().getPendingRequests(), server);
        }
        long firstKey = _sortedServers.firstKey();
        return _sortedServers.get(firstKey);
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
     * @param node {@link com.fasterxml.jackson.databind.JsonNode} from the route entry that the {@link ServerInfo} object is associated with
     */
    public void addDownstreamServer(ServerInfo server, JsonNode node) {

        if (server == null) {
            throw new NullPointerException("Null server");
        }

        _servers.add(server);
    }

    /**
     * Sorted list of all servers in this route
     */
    private SortedMap<Long, ServerInfo> _sortedServers;

    /**
     *
     */
    private List<ServerInfo> _servers;

}
