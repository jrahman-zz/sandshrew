/**
 * External endpoint for the proxy server
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class ProxyEndpoint extends Service {

    public ProxyEndpoint(String hostname, int port) {
        super(hostname, port);
    }

    public ProxyEndpoint(String hostname) {
        super(hostname, 80);
    }

}
