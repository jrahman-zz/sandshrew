package org.rahmanj.sandshrew.policy;

import com.fasterxml.jackson.databind.JsonNode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global factory to create a server based on a {@link JsonNode}.
 * Used to ensure we do not duplicate {@link ServerInfo} objects so that server stats are shared across pools and routes
 *
 * @author Jason P. Rahman
 */
public class ServerFactory {

    public ServerFactory() {
        _uniqueServers = new HashMap<String, ServerInfo>();
    }

    /**
     *
     * @param serverNode
     * @return
     */
    public ServerInfo createServer(JsonNode serverNode) {

        if (serverNode == null) {
            throw new NullPointerException("Null server node");
        }


        // TODO, find server name

        // TODO, try to look up

        // TODO, insert if not present

        throw new NotImplementedException();
    }

    /**
     *
     * @param serverNode
     * @return
     */
    private ServerInfo parseServer(JsonNode serverNode) {
        if (!serverNode.has("server") || !serverNode.get("server").isTextual()) {
            throw new IllegalArgumentException("server field is empty or incorrect datatype");
        }

        String hostname = serverNode.get("server").asText();
        hostname = cannonicalizeHostname(hostname);

        // TODO, complete this
        throw new NotImplementedException();
    }


    /**
     *
     * @param hostname
     * @return
     */
    private String cannonicalizeHostname(String hostname) {
        throw new NotImplementedException();
    }

    /**
     * Store a unique set of {@link ServerInfo} objects
     */
    private Map<String, ServerInfo> _uniqueServers;
}
