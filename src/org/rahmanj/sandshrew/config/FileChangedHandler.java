package org.rahmanj.sandshrew.config;

import java.nio.file.Path;

/**
 *@author Jason P. Rahman
 */
public interface FileChangedHandler {

    public void fileChanged(Path filePath);
}
