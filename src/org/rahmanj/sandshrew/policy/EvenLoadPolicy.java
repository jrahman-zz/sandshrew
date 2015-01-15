package org.rahmanj.sandshrew.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Policy based on even distribution of pending requests
 *
 * @author Jason P. Rahman
 */
public class EvenLoadPolicy {

    public EvenLoadPolicy() {
        _sortedServers = new TreeMap<Long, DownstreamServer>();
        _servers = new ArrayList<DownstreamServer>();
    }

    /**
     *
     * @param ctx
     * @return
     */
    public DownstreamServer next(RequestContext ctx) {
        if (ctx == null) {
            throw new NullPointerException("Null request context");
        }

        if (_servers.size() == 0) {
            throw new IllegalStateException("No servers added");
        }

        // Sort all the servers according to the pending request count and fetch the first
        _sortedServers.clear();
        for (DownstreamServer server : _servers) {
            _sortedServers.put(server.stats().getPendingRequests(), server);
        }
        long firstKey = _sortedServers.firstKey();
        return _sortedServers.get(firstKey);
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
     *
     */
    private SortedMap<Long, DownstreamServer> _sortedServers;

    /**
     *
     */
    private List<DownstreamServer> _servers;

}
