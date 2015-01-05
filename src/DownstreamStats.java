
import java.util.concurrent.atomic.AtomicLong;

/**
 * Records the statistics for a given downstream server
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class DownstreamStats {

    public DownstreamStats() {
        _pendingRequests = new AtomicLong(0);
        _completedRequests = new AtomicLong(0);
        _bytesSent = new AtomicLong(0);
        _bytesReceived = new AtomicLong(0);
    }

    /**
     * Record bytes sent to the proxy
     * @param count Number of bytes sent to the downstream server
     */
    public void bytesSent(long count) {
        if (count <= 0) {
            return;
        }
        _bytesSent.addAndGet(count);
    }

    /**
     *
     * @return
     */
    public long getPendingRequests() {
        return _pendingRequests.get();
    }

    /**
     *
     * @return
     */
    public long getCompletedRequests() {
        return _completedRequests.get();
    }

    /**
     *
     * @return
     */
    public long getBytesSent() {
        return _bytesSent.get();
    }

    /**
     *
     * @return
     */
    public long getBytesReceived() {
        return _bytesReceived.get();
    }

    /**
     * Total requests currently in flight
     */
    private AtomicLong _pendingRequests;

    /**
     * Total requests fully served
     */
    private AtomicLong _completedRequests;

    /**
     * Total number of bytes sent to this server
     */
    private AtomicLong _bytesSent;

    /**
     * Total number of bytes received from this server
     */
    private AtomicLong _bytesReceived;

    // TODO (JR) Handle the latency information

    /**
     * Record the window of requests over which to record latency stats
     */
    private static final int REQUEST_LATENCY_WINDOW = 1000;

    /**
     * Stores
     */
    private double[] _latencyStats;

}
