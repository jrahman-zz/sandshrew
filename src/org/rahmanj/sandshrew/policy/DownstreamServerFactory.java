package org.rahmanj.sandshrew.policy;

import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 *
 * @author Jason P. Rahman
 */
public interface DownstreamServerFactory {

    /**
     * Build a {@link ServerInfo} based on
     * @param node
     * @return
     */
    public ServerInfo createDownstreamServer(JsonNode node);

}
