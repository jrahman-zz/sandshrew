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

        if (poolNode.get("name") == null || !poolNode.get("name").isTextual()) {
            throw new IllegalArgumentException("No name for pool");
        }

        _poolName = poolNode.get("name").asText();

        // Get the information
        JsonNode policyNode = poolNode.get("policy");
        if (policyNode == null || policyNode.get("type") == null || !policyNode.get("type").isTextual()) {
            throw new IllegalArgumentException("Bad policy name in pool " + _poolName);
        }

        String policyName = policyNode.get("type").asText();
        _policy = policyFactories.get(policyName).createPolicy(policyNode);

        JsonNode serversNode = poolNode.get("servers");
        if (!serversNode.isArray()) {
            // TODO, make more robust
            throw new IllegalArgumentException("Servers must be a list in pool " + _poolName);
        }

        _servers = new ArrayList<ServerInfo>();

        ArrayNode serverArray = (ArrayNode)serversNode;
        ServerInfo server;
        for (JsonNode serverNode : serverArray) {
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
