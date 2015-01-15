package org.rahmanj.sandshrew.policy;

/**
 * Base interface for DownstreamServer proxy interfaces
 *
 * @author Jason P. Rahman
 */
public interface DownstreamServerInterface {


    /**
     * Get the hostname for the service
     * @return A String hostname for the service
     */
    public String getHostname();

    /**
     * Get the port for the downstream server
     * @return An int port for the downstream server
     */
    public int getPort();

    /**
     * Determines if writing to the given endpoint is currently being throttled by backpressure
     * @return True if the given endpoint is experiencing backpressure
     */
    public boolean isThrottled();

    /**
     * Determines if the given downstream server is up or down
     *
     * @return True ifthe downstream is down, false otherwise
     */
    public boolean isDown();

    /**
     * Decrement the number of write throttled channels from this endpoint
     * @return The number of throttled channels to the endpoint
     */
    public int decrementThrottle();

    /**
     * Increment the number of throttled channels from this endpoint
     * @return The number of throttle channels to the endpoint
     */
    public int incrementThrottle();

}
