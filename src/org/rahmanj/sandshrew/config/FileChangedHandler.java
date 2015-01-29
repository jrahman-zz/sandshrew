package org.rahmanj.sandshrew.config;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Handler to track changes to a given file
 *
 * @author Jason P. Rahman
 */
public interface FileChangedHandler {

    public void fileChanged(Path filePath);
}
