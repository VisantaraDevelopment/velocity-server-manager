package de.gunnablescum.velocityservermanager;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.gunnablescum.velocityservermanager.commands.*;
import de.gunnablescum.velocityservermanager.listener.ConnectionListener;
import de.gunnablescum.velocityservermanager.listener.ServerKickListener;
import de.gunnablescum.velocityservermanager.listener.ServerSwitchListener;
import de.gunnablescum.velocityservermanager.utils.DatabaseRegisteredServer;
import de.gunnablescum.velocityservermanager.utils.Messages;
import de.gunnablescum.velocityservermanager.utils.MySQL;
import de.gunnablescum.velocityservermanager.utils.RestartAnnouncementManager;
import de.gunnablescum.velocityservermanager.utils.ServerPinger;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Noah Fetz on 20.05.2016.
 * Contributors: GunnableScum
 */
@Plugin(
        id = "velocityservermanager",
        name = "VelocityServerManager",
        version = "1.0",
        description = "Plugin for Dynamic Server Management for the Velocity Proxy.",
        authors = {"Noah Fetz", "GunnableScum"}
)
public class ServerManager {

    @Inject
    private ProxyServer proxyServer;

    @Inject
    private Logger logger;

    @Inject
    @DataDirectory
    private Path dataDirectory;

    /** Registered database servers explicitly marked as lobbies. */
    public static volatile List<RegisteredServer> lobbies = List.of();
    /** Registered database servers explicitly marked as limbos. */
    public static volatile List<RegisteredServer> limbos = List.of();
    public static final Map<String, Boolean> serverStatusCache = new ConcurrentHashMap<>();
    public static volatile Map<String, DatabaseRegisteredServer> serverRegistry = Map.of();

    private static ServerManager instance;
    private RestartAnnouncementManager restartAnnouncements;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "velocity-server-manager-db");
        thread.setDaemon(true);
        return thread;
    });
    private final CompletableFuture<Void> initialized = new CompletableFuture<>();

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        logger.info("Initializing VelocityServerManager...");
        restartAnnouncements = new RestartAnnouncementManager(this);

        // Register listeners and commands during Velocity's initialization phase. Their database
        // work waits for the asynchronous startup task below to finish.
        registerCommands();
        registerListener();

        CompletableFuture.runAsync(() -> {
            MySQL.init();
            if (!MySQL.isConnected()) {
                throw new IllegalStateException("VelocityServerManager could not connect to the database.");
            }

            MySQL.migrateLegacyServerFlags();
            Messages.loadMessages();
            DatabaseRegisteredServer.addAllServers();
            refreshServerRegistry();
            startServerPinging();
            logger.info("VelocityServerManager has been successfully initialized!");
        }, databaseExecutor).whenComplete((ignored, error) -> {
            if (error == null) {
                initialized.complete(null);
            } else {
                logger.error("VelocityServerManager initialization failed.", error);
                initialized.completeExceptionally(error);
                proxyServer.shutdown();
            }
        });
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (restartAnnouncements != null) restartAnnouncements.stopAll();
        databaseExecutor.shutdown();
        MySQL.close();
    }

    public CompletableFuture<Void> runAsync(Runnable operation) {
        return initialized.thenRunAsync(operation, databaseExecutor)
                .exceptionally(error -> {
                    logger.error("VelocityServerManager could not complete an asynchronous operation.", error);
                    return null;
                });
    }

    /** Refreshes the read-only routing cache after loading or changing database records. */
    public synchronized void refreshServerRegistry() {
        List<DatabaseRegisteredServer> servers = MySQL.getAllServers();
        serverRegistry = servers.stream().collect(Collectors.toUnmodifiableMap(
                DatabaseRegisteredServer::name,
                server -> server,
                (first, second) -> second
        ));
        lobbies = servers.stream()
                .filter(DatabaseRegisteredServer::isLobby)
                .map(DatabaseRegisteredServer::getFromProxy)
                .filter(java.util.Objects::nonNull)
                .toList();
        limbos = servers.stream()
                .filter(DatabaseRegisteredServer::isLimbo)
                .map(DatabaseRegisteredServer::getFromProxy)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public static Optional<RegisteredServer> getRandomLobby() {
        return chooseRandom(available(lobbies));
    }

    public static Optional<RegisteredServer> getRandomLobbyExcluding(String serverName) {
        return chooseRandom(availableExcluding(lobbies, serverName));
    }

    public static Optional<RegisteredServer> getRandomFallback() {
        return chooseRandom(getFallbackCandidatesExcluding(null));
    }

    public static Optional<RegisteredServer> getRandomFallbackExcluding(String serverName) {
        return chooseRandom(getFallbackCandidatesExcluding(serverName));
    }

    public static List<RegisteredServer> getFallbackCandidatesExcluding(String serverName) {
        List<RegisteredServer> onlineHubs = availableExcluding(lobbies, serverName);
        if (!onlineHubs.isEmpty()) return onlineHubs;

        List<RegisteredServer> availableLimbos = availableExcluding(limbos, serverName);
        if (!availableLimbos.isEmpty()) return availableLimbos;

        // If every hub is down, still attempt a configured limbo even when its last ping failed.
        return limbos.stream()
                .filter(server -> !hasName(server, serverName))
                .toList();
    }

    private static List<RegisteredServer> available(List<RegisteredServer> servers) {
        return servers.stream().filter(ServerManager::isOnlineOrUnknown).toList();
    }

    private static List<RegisteredServer> availableExcluding(List<RegisteredServer> servers, String serverName) {
        return servers.stream()
                .filter(ServerManager::isOnlineOrUnknown)
                .filter(server -> !hasName(server, serverName))
                .toList();
    }

    private static boolean isOnlineOrUnknown(RegisteredServer server) {
        return !Boolean.FALSE.equals(serverStatusCache.get(server.getServerInfo().getName()));
    }

    private static boolean hasName(RegisteredServer server, String name) {
        return name != null && server.getServerInfo().getName().equalsIgnoreCase(name);
    }

    private static Optional<RegisteredServer> chooseRandom(List<RegisteredServer> servers) {
        if (servers.isEmpty()) return Optional.empty();
        return Optional.of(servers.get(ThreadLocalRandom.current().nextInt(servers.size())));
    }

    private void registerCommands() {
        CommandManager manager = proxyServer.getCommandManager();
        new AddServerCommand(this, manager);
        new ClearServerCommand(this, manager);
        new DeleteServerCommand(this, manager);
        new DisableServerCommand(this, manager);
        new EnableServerCommand(this, manager);
        new GotoCommand(this, manager);
        new HubCommand(this, manager);
        new ReloadServerCommand(this, manager);
        new ServerInfoCommand(this, manager);
        new ServersCommand(manager);
        new ServerListCommand(this, manager);
        new FlagServerCommand(this, manager);
        new UnflagServerCommand(this, manager);
        new WhereAmICommand(this, manager);
        new SetServerCommand(this, manager);
        new ServerAdminCommand(this, manager, restartAnnouncements);
    }

    private void registerListener() {
        new ServerKickListener(this);
        new ConnectionListener(this);
        new ServerSwitchListener(this);
    }

    public static ServerManager getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public RestartAnnouncementManager getRestartAnnouncements() {
        return restartAnnouncements;
    }

    private void startServerPinging() {
        int checkDelay = 10;
        ServerPinger.checkAllServers();
        proxyServer.getScheduler().buildTask(this, ServerPinger::checkAllServers)
                .delay(checkDelay, TimeUnit.SECONDS)
                .repeat(checkDelay, TimeUnit.SECONDS)
                .schedule();
    }
}
