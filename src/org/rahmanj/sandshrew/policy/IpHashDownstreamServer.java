package org.rahmanj.sandshrew.policy;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Downstream wrapper
 *
 * @author Jason P. Rahman
 */
public class IpHashDownstreamServer implements DownstreamServerInterface {

    public IpHashDownstreamServer(DownstreamServer base, JsonNode node) {
        _base = base;
    }


    @Override
    public String getHostname() {
        return _base.getHostname();
    }

    @Override
    public int getPort() {
        return _base.getPort();
    }

    @Override
    public boolean isThrottled() {
        return _base.isThrottled();
    }

    @Override
    public boolean isDown() {
        return _base.isDown();
    }

    @Override
    public int decrementThrottle() {
        return _base.decrementThrottle();
    }

    @Override
    public int incrementThrottle() {
        return _base.incrementThrottle();
    }

    /**
     *
     */
    DownstreamServer _base;


}
