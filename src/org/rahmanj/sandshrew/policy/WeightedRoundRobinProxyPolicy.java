
package org.rahmanj.sandshrew.policy;


/**
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public class WeightedRoundRobinProxyPolicy extends RoundRobinProxyPolicy {

    public WeightedRoundRobinProxyPolicy() {
    }

    /**
     *
     * @return
     */
    public DownstreamServer next(RequestContext ctx) {
        return super.next(ctx);
    }

    /**
     *
     * @param server
     * @param weight
     */
    public void addDownstreamServer(DownstreamServer server, int weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Positive value required");
        }

        if (server == null) {
            throw new NullPointerException("Null server");
        }

        while (weight-- > 0) {
            super.addDownstreamServer(server);
        }
    }
}
