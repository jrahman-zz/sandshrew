
package org.rahmanj.sandshrew.policy;

import com.fasterxml.jackson.databind.JsonNode;
import org.rahmanj.sandshrew.routes.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * TODO, refactor DownstreamServer, and DownstreamStats
 *
 * Need to reconsider the object model, and consider a model based more on composition than inheritance
 *
 */

/**
 * Base class Representation of given downstream remote server and its information
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamServer extends Service implements DownstreamServerInterface {

    public DownstreamServer(JsonNode node) throws URISyntaxException {
        super((new URI(node.get("server").asText())).getHost(), (new URI(node.get("server").asText())).getPort());
        _throttleCount = new AtomicInteger(0);
        _stats = new DownstreamStats();
        _isDown = false;
    }


    /**
     * Determines if writing to the given endpoint is currently being throttled by backpressure
     * @return True if the given endpoint is experiencing backpressure
     */
    public boolean isThrottled() {
        return _throttleCount.get() != 0;
    }

    /**
     * Determines if the given downstream server is up or down
     *
     * @return True if the downstream is down, false otherwise
     */
    public boolean isDown() {
        return _isDown;
    }

    /**
     * Decrement the number of write throttled channels from this endpoint
     * @return The number of throttled channels to the endpoint
     */
    public int decrementThrottle() {
        return _throttleCount.decrementAndGet();
    }

    /**
     * Increment the number of throttled channels from this endpoint
     * @return The number of throttle channels to the endpoint
     */
    public int incrementThrottle() {
        return _throttleCount.incrementAndGet();
    }

    /**
     * Get the {@link DownstreamStats} instance for this {@link DownstreamServer}
     *
     * @return Returns the {@link DownstreamStats} for this {@link DownstreamServer}
     */
    public DownstreamStats stats() {
        return _stats;
    }


    /**
     * Record the number of channels accessing this endpoint which are throttling reads from it
     */
    private AtomicInteger _throttleCount;

    /**
     * TODO (JR) Setters for this
     */
    private boolean _isDown;

    /**
     * Object holding stats for the given downstream server
     */
    private DownstreamStats _stats;

}
