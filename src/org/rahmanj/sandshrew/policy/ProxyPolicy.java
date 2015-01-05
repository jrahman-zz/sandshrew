
package org.rahmanj.sandshrew.policy;

import org.rahmanj.sandshrew.*;

/**
 * Base class for general proxy policies
 *
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public abstract class ProxyPolicy {

    public ProxyPolicy() {

    }

    public abstract DownstreamServer selectDownstreamServer();
}
