
package org.rahmanj.sandshrew;

import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handler to restrict the maximum waiting time for an inbound message
 * Useful for proxy routes with SLAs
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class ReadTimeoutHandler extends ChannelInboundHandlerAdapter {

    /**
     *
     * @param timeout
     */
    public ReadTimeoutHandler(int timeout) {
        _timeout = timeout;
    }



    /**
     * Waiting timeout value, in milliseconds
     */
    private int _timeout;

}
