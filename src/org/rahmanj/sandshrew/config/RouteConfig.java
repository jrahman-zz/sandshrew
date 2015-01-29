package org.rahmanj.sandshrew.config;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import org.rahmanj.sandshrew.routes.ProxyRoute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/**
 * Primary object representing the current route configuration
 *
 * @author Jason P. Rahman
 */
public class RouteConfig {

    /**
     * Private constructor to prevent public instantiation
     */
    public RouteConfig(List<Route> routes, List<Pool> pools) {
        _routes = routes;
        _pools = pools;

        // TODO, create a route resolver
    }

    public ProxyRoute lookupRoute(HttpRequest request) {

        // Gather useful information for later use
        String location = request.getUri();
        HttpHeaders headers = request.headers();

        // TODO implement
        return null;
    }


    public List<Route> getRoutes() {
        return _routes;
    }

    public List<Pool> getPools() {
        return _pools;
    }

    /**
     *
     */
    private List<Route> _routes;

    /**
     *
     */
    private List<Pool> _pools;
}
