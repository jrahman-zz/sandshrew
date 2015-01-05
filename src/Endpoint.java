/**
 * Represents an external endpoint on the proxy server
 *
 * @uathor Jason P. Rahman
 */
public class Endpoint {

    public Endpoint(String hostname, int port) {
        _hostname = hostname;
        _port = port;
    }

    // TODO (JR) Later consider wildcarding for this
    private String _hostname;
    private int _port;
}
