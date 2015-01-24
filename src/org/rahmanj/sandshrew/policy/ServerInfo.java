
package org.rahmanj.sandshrew.policy;

import com.fasterxml.jackson.databind.JsonNode;
import org.rahmanj.sandshrew.routes.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;


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
public class ServerInfo extends Service implements DownstreamInfoInterface {

    public ServerInfo(JsonNode node) throws URISyntaxException {
        super((new URI(node.get("server").asText())).getHost(), (new URI(node.get("server").asText())).getPort());
        _stats = new ServerStats();
        _isDown = false;

        _throttleRequests = new AtomicLong(0);
        _throttleListeners = new HashSet<ThrottleListener>();
    }


    /**
     * Determines if writing to the given endpoint is currently being throttled by backpressure
     * @return True if the given endpoint is experiencing backpressure
     */
    public boolean isThrottled() {
        return _throttleRequests.get() != 0;
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
     * Increment the throttle count for this downstream
     */
    public int incrementThrottle() {
        int count = (int)_throttleRequests.getAndIncrement();
        if (count == 0) {
            // We must run the notifiers since the throttle count raised above 0
            synchronized (_throttleListeners) {
                for (ThrottleListener listener : _throttleListeners) {
                    try {
                        listener.onThrottle();
                    } catch (Exception e) {
                        _logger.fine("Exception: " + e.toString());
                    }
                }
            }
        }
        return count + 1;
    }

    /**
     * Decrement the throttle count for this downstream. Listeners may be notified
     */
    public int decrementThrottle() {
        int count = (int)_throttleRequests.decrementAndGet();
        if (count == 0) {
            // Count hit 0, run notifiers
            synchronized (_throttleListeners) {
                for (ThrottleListener listener : _throttleListeners) {
                    try {
                        listener.onStopThrottle();
                    } catch (Exception e) {
                        _logger.fine("Exception: " + e.toString());
                    }
                }
            }
        }
        return count;
    }

    /**
     * Registers a throttle listener to receive notifications. Note that the callback will be
     * invoked on an arbitrary event loop.
     *
     * @param listener {@link ThrottleListener} to register to receive notifications
     */
    public void registerThrottleListener(ThrottleListener listener) {
        synchronized (_throttleListeners) {
            _throttleListeners.add(listener);
        }
    }

    public void deregisterThrottleListener(ThrottleListener listener) {
        synchronized (_throttleListeners) {
            _throttleListeners.remove(listener);
        }
    }

    /**
     * Get the {@link ServerStats} instance for this {@link ServerInfo}
     *
     * @return Returns the {@link ServerStats} for this {@link ServerInfo}
     */
    public ServerStats stats() {
        return _stats;
    }


    /**
     * Total number of throttle requests
     */
    private AtomicLong _throttleRequests;

    /**
     * Set of entities waiting to be notified of a throttle state change event
     */
    private Set<ThrottleListener> _throttleListeners;

    /**
     * TODO (JR) Setters for this
     */
    private boolean _isDown;

    /**
     * Object holding stats for the given downstream server
     */
    private ServerStats _stats;

    private static final Logger _logger = Logger.getLogger(
            ServerInfo.class.getName()
    );

}
