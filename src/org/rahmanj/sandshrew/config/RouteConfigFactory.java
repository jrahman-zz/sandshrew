package org.rahmanj.sandshrew.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rahmanj.sandshrew.policy.PolicyFactory;
import org.rahmanj.sandshrew.policy.ServerFactory;
import org.rahmanj.sandshrew.policy.ServerInfo;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Factory to create {@link RouteConfig} objects
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class RouteConfigFactory {

    /**
     *
     * @param policyFactories
     * @param serverFactory
     */
    public RouteConfigFactory(Map<String, PolicyFactory> policyFactories, ServerFactory serverFactory) {
        _policyFactories = policyFactories;
        _serverFactory = serverFactory;
    }

    public RouteConfig buildRouteConfig(Path filePath) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode configNode = mapper.readTree(Files.newInputStream(filePath));

        // Get the pools first since the routes are dependant on the pools
        if (!configNode.has("pools") || !configNode.get("pools").isArray()) {
            throw new IllegalArgumentException("No pools defined");
        }
        JsonNode poolsNode = configNode.get("pools");
        Map<String, Pool> pools = parsePools(poolsNode);

        // Get the routes now that we have the pools
        if (!configNode.has("routes") || !configNode.get("routes").isArray()) {
            throw new IllegalArgumentException("No routes defined");
        }
        JsonNode routesNode = configNode.get("routes");
        Map<String, Route> routes = parseRoutes(routesNode, pools);

        // TODO, update
        return new RouteConfig(new ArrayList(routes.values()), new ArrayList(pools.values()));
    }

    /**
     * Parse the {@link Pool}s from the configuration file
     *
     * @param poolsNode {@link JsonNode} of an array containing {@link Pool}s
     * @return
     */
    private Map<String, Pool> parsePools(JsonNode poolsNode) {

        Map<String, Pool> pools = new HashMap<String, Pool>();

        Pool pool;
        for (JsonNode poolNode : poolsNode) {
            try {
                pool = new Pool(poolNode, _policyFactories, _serverFactory);
                if (pools.containsKey(pool.getPoolName())) {
                    // TODO, just treat this as warning, think more about this
                    _logger.warning("Duplicate pool " + pool.getPoolName());
                }
                pools.put(pool.getPoolName(), pool);
            } catch (IllegalArgumentException e) {
                // DO we warn, or crash??
            } catch (Exception e) {
                throw new NotImplementedException();
            }
        }

        return pools;
    }

    /**
     * Parse the routes from the config file
     *
     * @param routesNode {@link JsonNode} containing route configuration information
     * @param pools @{link Map<String, Pool>} mapping from pool names to {@link Pool}s
     * @return
     */
    private Map<String, Route> parseRoutes(JsonNode routesNode, Map<String, Pool> pools) {
        Map<String, Route> routes = new HashMap<String, Route>();

        Route route;
        for (JsonNode routeNode : routesNode) {
            try {
                route = new Route(routeNode, pools);
                // TODO, insert into the routes list
            } catch (IllegalArgumentException e) {
                throw new NotImplementedException();
            } catch (Exception e) {
                throw new NotImplementedException();
            }
        }

        return routes;
    }

    /**
     * Mappings from policy types to each {@link PolicyFactory}
     */
    private Map<String, PolicyFactory> _policyFactories;

    /**
     * Globally shared server factory used to ensure we do not duplicate any {@link ServerInfo} objects
     */
    private ServerFactory _serverFactory;

    private static final Logger _logger = Logger.getLogger(
            RouteConfigFactory.class.getName()
    );
}
