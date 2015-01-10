package org.rahmanj.sandshrew.config;

import java.io.IOException;

/**
 * @author Jason P. Rahman
 */
public interface FileChangedErrorHandler {

    public void fileAccessError(IOException exception);

}
