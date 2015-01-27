package org.rahmanj.sandshrew.policy;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interface for factory objects that create route policies
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public interface PolicyFactory {
    public RoutePolicy createPolicy(JsonNode policyNode);
}
