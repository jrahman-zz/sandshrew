package org.rahmanj.sandshrew.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.rahmanj.sandshrew.policy.RoutePolicy;
import org.rahmanj.sandshrew.policy.PolicyFactory;
import org.rahmanj.sandshrew.policy.ServerFactory;
import org.rahmanj.sandshrew.policy.ServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a Pool of upstream servers
 *
 * @author Jason P. Rahman
 */
public class Pool {

    /**
     * Construct a new {@link Pool}
     * @param poolNode {@link JsonNode} for the pool
     * @param policyFactories {@link Map<String, PolicyFactory>} containing a {@link PolicyFactory} for each type of {@link RoutePolicy}
     * @param serverFactory {@link ServerFactory} to create {@link ServerInfo} objects
     */
    public Pool(JsonNode poolNode, Map<String, PolicyFactory> policyFactories, ServerFactory serverFactory) {
        // TODO, build Pool from config

        if (!poolNode.has("name") || !poolNode.get("name").isTextual()) {
            throw new IllegalArgumentException("No name for pool");
        }

        _poolName = poolNode.get("name").asText();

        // Get the information
        if (!poolNode.has("policy")) {
            throw new IllegalArgumentException("Not policy for pool " + _poolName);
        }
        JsonNode policyNode = poolNode.get("policy");

        if (!policyNode.has("type") || !policyNode.get("type").isTextual()) {
            throw new IllegalArgumentException("Bad policy name in pool " + _poolName);
        }

        String policyName = policyNode.get("type").asText();
        _policy = policyFactories.get(policyName).createPolicy(policyNode);

        if (!poolNode.has("servers") || !poolNode.get("server").isArray()) {
            throw new IllegalArgumentException("Servers must be a list in pool " + _poolName);
        }

        JsonNode serversNode = poolNode.get("servers");
        _servers = new ArrayList<ServerInfo>();

        ServerInfo server;
        for (JsonNode serverNode : serversNode) {
            server = serverFactory.createServer(serverNode);
            _policy.addDownstreamServer(server, serverNode);
            _servers.add(server);
        }
    }

    /**
     * Get the {@link RoutePolicy} for this pool
     * @return
     */
    public RoutePolicy getPolicy() {
        return _policy;
    }

    /**
     * Get the name of this {@link Pool}
     * @return A {@link String} containing the {@link Pool}s name
     */
    public String getPoolName() {
        return _poolName;
    }

    /**
     * Custom policy for this route
     */
    private RoutePolicy _policy;

    /**
     *
     */
    private List<ServerInfo> _servers;

    /**
     *
     */
    private String _poolName;

}
