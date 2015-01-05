
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

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
