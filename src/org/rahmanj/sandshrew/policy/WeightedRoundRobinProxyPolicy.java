
package org.rahmanj.sandshrew.policy;

import com.fasterxml.jackson.databind.JsonNode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Weighted round robin policy
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class WeightedRoundRobinProxyPolicy extends RoundRobinRoutePolicy {

    public WeightedRoundRobinProxyPolicy() {
    }

    /**
     *
     * @return
     */
    public ServerInfo next(RequestContext ctx) {
        return super.next(ctx);
    }

    /**
     *
     * @param server
     * @param node
     */
    public void addDownstreamServer(ServerInfo server, JsonNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Positive value required");
        }

        if (server == null) {
            throw new NullPointerException("Null server");
        }

        // TODO, fix this syntax
        int weight = node.get("weight").asInt(1);

        while (weight-- > 0) {
            super.addDownstreamServer(server, node);
        }
    }

    public class WeightedRoundRobinPolicyFactory implements PolicyFactory {
        public RoutePolicy createPolicy(JsonNode jsonNode) {
            throw new NotImplementedException();
        }
    }
}
