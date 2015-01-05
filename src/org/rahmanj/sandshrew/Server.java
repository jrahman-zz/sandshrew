
package org.rahmanj.sandshrew;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Main class for initializing the server
 *
 * @author Jason P. Rahman
 */
public class Server {

    public Server() {

        _proxyServers = new HashMap<Integer, ProxyServer>();

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

        Server server = new Server();

        try {
            server.run();
        } catch (Exception e) {

        }
    }

    /**
     * Initializes the Server for use
     */
    public void init() {
        // TODO write proper initialization

        // TODO (JR) Read the static configuration file

        // TODO (JR) Read the dynamic configuration file (routes)

        // TODO (JR) Start background thread to stat the dynamic configuration file

        // TODO (JR) Make this configurable with NativeEventLoopGroup()
        // TODO (JR) Make the thread count configurable
        _bossGroup = new NioEventLoopGroup(4);
        _workerGroup = new NioEventLoopGroup(12);



        ProxyServer proxy = new ProxyServer(80, _bossGroup, _workerGroup, NioServerSocketChannel.class);

        _proxyServers.put(80, proxy);


        ChannelFuture channelFuture = proxy.run();

        // Aggregate multiple ChannelFutures from each proxy's run() call into a single Promise
        ChannelPromiseAggregator aggregator = new ChannelPromiseAggregator(channelFuture);

        // TODO (JR) Add additional proxies here

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

    /**
     * Globally shared {@link EventLoopGroup} for listening sockets
     */
    public EventLoopGroup _bossGroup;

    /**
     * Globally shared {@link EventLoopGroup} for child sockets
     */
    public EventLoopGroup _workerGroup;

    /**
     * Store a map of proxies based on their listening port
     */
    public Map<Integer, ProxyServer> _proxyServers;

    private static final Logger _logger = Logger.getLogger(
            Server.class.getName()
    );

}
