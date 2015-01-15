
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
public class DownstreamStats {

    public DownstreamStats() {
        _pendingRequests = new AtomicLong(0);
        _completedRequests = new AtomicLong(0);
        _bytesSent = new AtomicLong(0);
        _bytesReceived = new AtomicLong(0);
        _throttleRequests = new AtomicLong(0);

        _throttleListeners = new HashSet<ThrottleListener>();
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
     * Increment the throttle count for this downstream
     */
    public void incrementThrottle() {
        long count = _throttleRequests.getAndIncrement();
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
    }

    /**
     * Decrement the throttle count for this downstream. Listeners may be notified
     */
    public void decrementThrottle() {
        long count = _throttleRequests.decrementAndGet();
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

    /**
     * Remove a throttle listener
     *
     * @param listener
     */
    public void deregisterThrottleListener(ThrottleListener listener) {
        synchronized (_throttleListeners) {
            _throttleListeners.remove(listener);
        }
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


    /**
     * Total number of throttle requests
     */
    private AtomicLong _throttleRequests;

    /**
     * Set of entities waiting to be notified of a throttle state change event
     */
    private Set<ThrottleListener> _throttleListeners;

    // TODO (JR) Handle the latency information

    /**
     * Record the window of requests over which to record latency stats
     */
    private static final int REQUEST_LATENCY_WINDOW = 1000;

    /**
     * Stores
     */
    private double[] _latencyStats;

    private static final Logger _logger = Logger.getLogger(
            DownstreamStats.class.getName()
    );

}
