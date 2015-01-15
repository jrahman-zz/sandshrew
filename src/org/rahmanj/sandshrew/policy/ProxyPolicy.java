
package org.rahmanj.sandshrew.policy;

/**
 * Base class for general proxy policies
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public interface ProxyPolicy {


    public DownstreamServer next(RequestContext ctx);
}
