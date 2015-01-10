package org.rahmanj.sandshrew.config;

import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class to hold general server configuration information
 *
 * @author Jason P. Rahman
 */
public class ServerConfig {

    /**
     * Hide private ctor
     */
    private ServerConfig() {
    }

    /**
     *
     */
    static ServerConfig buildServerConfig(Path filePath) throws IOException {

        // TODO (JR) Make this better

        InputStream stream = Files.newInputStream(filePath);
        ServerConfig config = null;
        try (JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
            Gson gs = new Gson();
            config = gs.fromJson(reader, ServerConfig.class);
        }
        return config;
    }



    /**
     *
     * @return
     */
    public int getWorkerThreadCount() {
        return _workerThreadCount;
    }

    /**
     *
     */
    @SerializedName("WorkerThreadCount")
    private int _workerThreadCount;
}
