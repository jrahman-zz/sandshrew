package org.rahmanj.sandshrew.config;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * Represents a route, mapping a location/request, to a pool of servers
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class Route {

    /**
     * Create a new {@link Route} instance
     * @param routeNode {@link JsonNode} from the configuration file
     * @param pools {@link Map<String, Pool>} containing pool name to {@link Pool} mappings
     */
    public Route(JsonNode routeNode, Map<String, Pool> pools) {

        if (routeNode == null) {
            throw new NullPointerException("Null route node");
        }

        if (pools == null) {
            throw new NullPointerException("Null pools");
        }

        if (!routeNode.has("pool") || !routeNode.get("pool").isTextual()) {
            throw new IllegalArgumentException("No pool for route");
        }

        String poolName = routeNode.get("pool").asText();
        if (!pools.containsKey(poolName)) {
            throw new IllegalArgumentException("Pool " + poolName + " not found");
        }
        _pool = pools.get(poolName);
    }


    public Pool getPool() {
        return _pool;
    }

    /**
     * Server pool for this given route
     */
    private Pool _pool;
}
