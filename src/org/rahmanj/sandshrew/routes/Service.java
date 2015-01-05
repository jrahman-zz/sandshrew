
package org.rahmanj.sandshrew.routes;

/**
 * Represents an service, either a proxy server endpoint, or a downstream server
 * For performance reasons, we store a copy of the hash code to avoid rehashing
 * Therefore, DO NOT modify the _hostname and _port fields
 *
 * @uathor Jason P. Rahman (jprahman93@purdue.edu, rahmanj@purdue.edu)
 */
public class Service {

    public Service(String hostname, int port) {
        _hostname = hostname;
        _port = port;

        // Cached copy of the hashCode
        _hashCode = (hostname + (Integer.toString(_port))).hashCode();
    }

    /**
     * Get the hostname for the service
     * @return A String hostname for the service
     */
    public String getHostname() {
        return _hostname;
    }

    /**
     * Get the port for the downstream server
     * @return An int port for the downstream server
     */
    public int getPort() {
        return _port;
    }

    /**
     * Possible endpoint protocols
     */
    public static final class ServiceProtocol {
        private ServiceProtocol() {}

        public static final int HTTP = 1;
        public static final int HTTPS = 2;
        public static final int SPDY = 4;
    }

    /**
     * Get the hashCode for this
     * @return
     */
    @Override
    public int hashCode() {
        return _hashCode;
    }

    // TODO (JR) Fill this out more robustly

    /**
     *
     */
    private final String _hostname;

    /**
     *
     */
    private final int _port;


    /**
     * Cached copy of the hash code since immutable
     */
    private int _hashCode;
}
