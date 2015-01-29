package org.rahmanj.sandshrew.policy;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Abstract base class for factory objects that create route policies
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public abstract class PolicyFactory {

    public RoutePolicy createPolicy(JsonNode policyNode, ServerPair[] servers) {
        RoutePolicy policy = instantiatePolicy(policyNode);
        for (ServerPair pair : servers) {
            policy.addDownstreamServer(pair.getServer(), pair.getServerNode());
        }
        return policy;
    }

    protected abstract RoutePolicy instantiatePolicy(JsonNode policyNode);

    public class ServerPair {
        public ServerPair(ServerInfo server, JsonNode serverNode) {
            _server = server;
            _serverNode = serverNode;
        }

        public ServerInfo getServer() {
            return _server;
        }

        public JsonNode getServerNode() {
            return _serverNode;
        }

        private ServerInfo _server;
        private JsonNode _serverNode;
    }
}
