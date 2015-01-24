package org.rahmanj.sandshrew.config;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import org.rahmanj.sandshrew.routes.ProxyRoute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;


/**
 * Primary object representing the current route configuration
 *
 * @author Jason P. Rahman
 */
public class RouteConfig {

    /**
     * Private constructor to prevent public instantiation
     */
    private RouteConfig() {

    }

    public static RouteConfig buildRouteConfig(Path filePath) throws IOException {

        // TODO (JR) Open file

        // TODO (JR) Read contents

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(Files.newInputStream(filePath));

        Iterator<String> fieldNames = node.fieldNames();
        List<JsonNode> upstreams = node.findValues("upstreams");
        List<JsonNode> locations = node.findValues("locations");

        for (JsonNode upstream : upstreams) {

        }

        for (JsonNode location : locations) {

        }

        return null;
    }

    public ProxyRoute lookupRoute(HttpRequest request) {

        // Gather useful information for later use
        String location = request.getUri();
        HttpHeaders headers = request.headers();

        // TODO implement
        return null;
    }
}
