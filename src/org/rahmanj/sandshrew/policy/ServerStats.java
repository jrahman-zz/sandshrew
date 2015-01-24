
package org.rahmanj.sandshrew.policy;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Records the statistics for a given downstream serve. Designed to be fully thread safe so a single instance
 * can be shared across all threads without and defensive copies being made. Locking is minimal, atomics used instead
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class ServerStats {

    public ServerStats() {
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
     * Record the beginning of a new request to the downstream server
     */
    public void requestStarted() {
        _pendingRequests.incrementAndGet();
    }

    /**
     * Record the completion of a request to the downstream server
     * @param successful True if the request successfully completed, false otherwise
     */
    public void requestCompleted(boolean successful) {
        if (successful) {
            // Yes, technically there is a slight race condition here
            // but I really don't care because it really doesn't matter
            // These stats don't have to be 100% accurate, being off by 1 is OK
            _pendingRequests.decrementAndGet();
            _completedRequests.incrementAndGet();
        }
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
