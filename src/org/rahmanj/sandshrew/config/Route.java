package org.rahmanj.sandshrew.config;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * Represents a route, mapping a location/request, to a pool of servers
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class Route {

    public Route(JsonNode routeNode, Map<String, Pool> pools) {



    }



    /**
     * Server pool for this given route
     */
    private Pool _pool;


}
