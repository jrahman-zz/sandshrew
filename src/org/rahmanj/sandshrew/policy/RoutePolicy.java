
package org.rahmanj.sandshrew.policy;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Base class for general proxy policies
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public interface RoutePolicy {

    /**
     * Add a new server to a given route policy
     *
     * @param server Shared {@link ServerInfo} for the route entry
     * @param node {@link com.fasterxml.jackson.databind.JsonNode} from the route entry that the {@link ServerInfo} object is associated with
     */
    public void addDownstreamServer(ServerInfo server, JsonNode node);

    /**
     *
     * @param ctx
     * @return
     */
    public ServerInfo next(RequestContext ctx);

    /**
     *
     * @param server
     */
    public void markAsFailed(ServerInfo server);

    /**
     *
     * @param server
     */
    public void markAsLive(ServerInfo server);
}
