
package org.rahmanj.sandshrew;

/**
 * Represents an service, either a proxy server endpoint, or a downstream server
 *
 * @uathor Jason P. Rahman
 */
public class Service {

    public Service(String hostname, int port) {
        _hostname = hostname;
        _port = port;
    }

    /**
     * Get the hostname for the downstream server
     * @return A String hostname for the downstream server
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

    // TODO (JR) Fill this out more robustly

    private final String _hostname;
    private final int _port;


}
