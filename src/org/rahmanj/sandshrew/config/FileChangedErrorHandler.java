package org.rahmanj.sandshrew.config;

import java.io.IOException;

/**
 * @author Jason P. Rahman (jprahman93@gmail.com, rahmanj@purdue.edu)
 */
public interface FileChangedErrorHandler {

    public void onFileAccessError(IOException exception);
}
