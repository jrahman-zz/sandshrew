
package org.rahmanj.sandshrew.policy;

import org.rahmanj.sandshrew.*;

/**
 * Base class for general proxy policies
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public interface ProxyPolicy {


    public DownstreamServer next();
}
