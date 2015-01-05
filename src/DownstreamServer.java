import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Represents a given downstream remote server and its information
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamServer {

    public DownstreamServer(String hostname, int port) {
        _port = port;
        _hostname = hostname;
        _throttleCount = new AtomicInteger(0);
        _stats = new DownstreamStats();
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
     * Determines if writting to the given endpoint is currently being throttled by backpressure
     * @return True if the given endpoint is experiencing backpressure
     */
    public boolean isThrottled() {
        return _throttleCount.get() != 0;
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
     *
     */
    private final String _hostname;

    /**
     *
     */
    private final int _port;



    /**
     * Record the number of channels accessing this endpoint which are throttling reads from it
     */
    private AtomicInteger _throttleCount;

    /**
     * Object holding stats for the given downstream server
     */
    private DownstreamStats _stats;

}
