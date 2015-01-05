/**
 *
 * Base class to store information regarding a given downstream target
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamServer {

    public DownstreamServer(String hostname, int port) {
        _port = port;
        _hostname = hostname;
    }

    public String getHostname() {
        return _hostname;
    }

    public int getPort() {
        return _port;
    }

    private int _port;
    private String _hostname;

}
