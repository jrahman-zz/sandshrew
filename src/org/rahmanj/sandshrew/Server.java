
package org.rahmanj.sandshrew;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.rahmanj.sandshrew.config.*;
import org.rahmanj.sandshrew.policy.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Main class for initializing the server
 *
 * @author Jason P. Rahman
 */
public class Server implements FileChangedErrorHandler, FileChangedHandler {

    public Server(String configFilePath) {

        _configFilePath = Paths.get(configFilePath);
        _proxyServers = new HashMap<Integer, ProxyServer>();


        // TODO, refactor this

        // Create all possible policies
        Map<String, PolicyFactory> policyFactories = new HashMap<String, PolicyFactory>();
        policyFactories.put("ip_hash", new IpHashPolicy.IpHashPolicyFactory());
        policyFactories.put("even_load", new EvenLoadPolicy.EvenLoadPolicyFactory());
        policyFactories.put("round_robin", new RoundRobinRoutePolicy.RoundRobinPolicyFactory());
        policyFactories.put("weighted_round_robin", new WeightedRoundRobinProxyPolicy.WeightedRoundRobinPolicyFactory());

        ServerFactory serverFactory = new ServerFactory();

        _configFactory = new RouteConfigFactory(policyFactories, serverFactory);
    }


    /**
     * 1. Read static config file
     * 2. Read dynamic config file
     * 3. Start config reloader worker
     * 4. Create EventLoopGroups for the application
     * 5. Inspect routes to determine which ports need to be listened to
     * 6. Setup ProxyServers on those ports
     */

    public static void main(String[] args) {

        String configFilePath = args[1];

        // TODO, arg parsing

        Server server = new Server(configFilePath);

        try {
            server.run();
        } catch (Exception e) {
            _logger.severe("Exception " + e.toString());
        }
    }

    public void fileChanged(Path filePath) {
        // TODO (JR) Stub later
    }

    public void fileAccessError(IOException exception) {
        // TODO (JR) Stub later
    }


    /**
     * Begin running all the proxy servers, returns immediately
     * @throws Exception
     */
    public void run() throws Exception {

        // Start each proxy independently
        for (ProxyServer proxy : _proxyServers.values()) {
            proxy.run();
        }
    }

    public void onFileChanged(Path filePath) {
        RouteConfig config;

        try {
            config = _configFactory.buildRouteConfig(filePath);
        } catch (Exception e) {
            // TODO
            throw new NotImplementedException();
        }

        configurationMigration(config);
    }

    public void onFileChangedError(IOException e) {
        // Handle errors when looking up the config file
        throw new NotImplementedException();
    }

    /**
     * Perform a migration from the current configuration to a new configuration
     * @param config New {@link RouteConfig} to migrate to
     */
    private void configurationMigration(RouteConfig config) {
        throw new NotImplementedException();
    }

    /**
     * Initializes the Server for use
     */
    private void init() {
        // TODO write proper initialization

        // TODO (JR) Read the static configuration file

        // TODO (JR) Read the dynamic configuration file (routes)

        // TODO (JR) Start background thread to stat the dynamic configuration file

        // TODO (JR) Make this configurable with NativeEventLoopGroup()
        // TODO (JR) Make the thread count configurable
        _bossGroup = new NioEventLoopGroup(4);
        _workerGroup = new NioEventLoopGroup(12);

        /**
         * (JR) Sample of using {@link org.rahmanj.sandshrew.config.FileWatcher} with {@link nio.NioEventLoopGroup}
         */
        _configWatcher = new FileWatcher(Paths.get("test"), this, this, _bossGroup, 3);
    }

    private ProxyServer startProxy(ChannelPromiseAggregator promises, int port) {

        // TODO, add support for binding to a given interface as well
        ProxyServer proxy = new ProxyServer(port, _bossGroup, _workerGroup, NioServerSocketChannel.class);

        _proxyServers.put(port, proxy);

        ChannelFuture channelFuture = proxy.run();

        // Aggregate multiple ChannelFutures from each proxy's run() call into a single Promise
        promises.add(channelFuture);

        return proxy;
    }

    /**
     * Globally shared {@link EventLoopGroup} for listening sockets
     */
    private EventLoopGroup _bossGroup;

    /**
     * Globally shared {@link EventLoopGroup} for child sockets
     */
    private EventLoopGroup _workerGroup;

    /**
     * Store a map of proxies based on their listening port
     */
    private Map<Integer, ProxyServer> _proxyServers;

    /**
     * Path to the configuration file
     */
    private Path _configFilePath;

    /**
     *
     */
    private RouteConfigFactory _configFactory;

    /**
     *
     */
    private FileWatcher _configWatcher;

    private static final Logger _logger = Logger.getLogger(
            Server.class.getName()
    );
}
