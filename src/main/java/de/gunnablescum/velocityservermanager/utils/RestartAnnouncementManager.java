package de.gunnablescum.velocityservermanager.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import de.gunnablescum.velocityservermanager.ServerManager;
import net.kyori.adventure.bossbar.BossBar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RestartAnnouncementManager {

    public static final int DEFAULT_ESTIMATE_SECONDS = 120;

    public enum StartResult {
        STARTED,
        ALREADY_RUNNING,
        NO_DESTINATION
    }

    private final ServerManager plugin;
    private final Map<String, RestartSession> sessions = new ConcurrentHashMap<>();

    public RestartAnnouncementManager(ServerManager plugin) {
        this.plugin = plugin;
    }

    public StartResult start(RegisteredServer server, int estimatedSeconds) {
        String serverName = server.getServerInfo().getName();
        String key = serverName.toLowerCase(java.util.Locale.ROOT);
        List<Player> playersToMove = List.copyOf(server.getPlayersConnected());
        List<RegisteredServer> destinations = ServerManager.getFallbackCandidatesExcluding(serverName);
        if (!playersToMove.isEmpty() && destinations.isEmpty()) return StartResult.NO_DESTINATION;

        RestartSession session = new RestartSession(key, server, estimatedSeconds);
        if (sessions.putIfAbsent(key, session) != null) return StartResult.ALREADY_RUNNING;

        var announcement = Messages.serverRestartAnnouncement(
                serverName,
                formatEstimate(estimatedSeconds)
        );
        plugin.getProxyServer().getConsoleCommandSource().sendMessage(announcement);
        plugin.getProxyServer().getAllPlayers().forEach(player -> player.sendMessage(announcement));

        session.start();
        distributePlayers(playersToMove, destinations);
        return StartResult.STARTED;
    }

    public void stopAll() {
        sessions.values().forEach(RestartSession::finish);
        sessions.clear();
    }

    private static void distributePlayers(List<Player> players, List<RegisteredServer> destinations) {
        if (players.isEmpty() || destinations.isEmpty()) return;

        Map<RegisteredServer, Integer> projectedLoads = new HashMap<>();
        destinations.forEach(server -> projectedLoads.put(server, server.getPlayersConnected().size()));

        for (Player player : players) {
            RegisteredServer destination = destinations.stream()
                    .min(Comparator.comparingInt(projectedLoads::get))
                    .orElseThrow();
            projectedLoads.compute(destination, (server, count) -> count + 1);
            player.createConnectionRequest(destination).connect();
        }
    }

    private static String formatEstimate(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return minutes == 0
                ? remainingSeconds + "d"
                : minutes + "m " + remainingSeconds + "d";
    }

    private final class RestartSession {
        private final String key;
        private final RegisteredServer server;
        private final int estimatedSeconds;
        private final long startedAtNanos = System.nanoTime();
        private final long estimateNanos;
        private final BossBar bossBar;
        private final Map<UUID, Player> viewers = new ConcurrentHashMap<>();
        private final AtomicBoolean pingInFlight = new AtomicBoolean();
        private final AtomicBoolean finished = new AtomicBoolean();
        private volatile boolean observedOffline;
        private volatile long nextPingAtNanos;
        private volatile ScheduledTask task;

        private RestartSession(String key, RegisteredServer server, int estimatedSeconds) {
            this.key = key;
            this.server = server;
            this.estimatedSeconds = estimatedSeconds;
            this.estimateNanos = TimeUnit.SECONDS.toNanos(estimatedSeconds);
            this.bossBar = BossBar.bossBar(
                    Messages.serverRestartBossBar(server.getServerInfo().getName(), formatEstimate(estimatedSeconds)),
                    1.0f,
                    BossBar.Color.YELLOW,
                    BossBar.Overlay.PROGRESS
            );
        }

        private void start() {
            updateBossBar();
            syncLobbyViewers();
            task = plugin.getProxyServer().getScheduler().buildTask(plugin, this::tick)
                    .delay(1, TimeUnit.SECONDS)
                    .repeat(1, TimeUnit.SECONDS)
                    .schedule();
        }

        private void tick() {
            if (finished.get()) return;
            updateBossBar();
            if (System.nanoTime() - startedAtNanos >= estimateNanos) {
                finish();
                return;
            }
            syncLobbyViewers();
            pollServer();
        }

        private void updateBossBar() {
            long elapsedNanos = Math.max(0, System.nanoTime() - startedAtNanos);
            long remainingSeconds = TimeUnit.NANOSECONDS.toSeconds(Math.max(0, estimateNanos - elapsedNanos));
            float progress = (float) Math.max(0, 1.0 - (double) elapsedNanos / estimateNanos);
            bossBar.name(Messages.serverRestartBossBar(server.getServerInfo().getName(), formatEstimate((int) remainingSeconds)));
            bossBar.progress(progress);
        }

        private void syncLobbyViewers() {
            Map<UUID, Player> currentViewers = new HashMap<>();
            for (RegisteredServer lobby : ServerManager.lobbies) {
                for (Player player : lobby.getPlayersConnected()) {
                    currentViewers.put(player.getUniqueId(), player);
                }
            }

            for (Map.Entry<UUID, Player> entry : new ArrayList<>(viewers.entrySet())) {
                if (!currentViewers.containsKey(entry.getKey()) && viewers.remove(entry.getKey(), entry.getValue())) {
                    entry.getValue().hideBossBar(bossBar);
                }
            }

            currentViewers.forEach((uuid, player) -> {
                if (viewers.putIfAbsent(uuid, player) == null) player.showBossBar(bossBar);
            });
        }

        private void pollServer() {
            long now = System.nanoTime();
            if (now < nextPingAtNanos || !pingInFlight.compareAndSet(false, true)) return;
            nextPingAtNanos = now + TimeUnit.SECONDS.toNanos(2);
            server.ping().whenComplete((ping, error) -> {
                pingInFlight.set(false);
                if (finished.get()) return;
                if (error != null) {
                    observedOffline = true;
                    ServerManager.serverStatusCache.put(server.getServerInfo().getName(), false);
                } else if (observedOffline) {
                    ServerManager.serverStatusCache.put(server.getServerInfo().getName(), true);
                    finish();
                } else {
                    ServerManager.serverStatusCache.put(server.getServerInfo().getName(), true);
                }
            });
        }

        private void finish() {
            if (!finished.compareAndSet(false, true)) return;
            ScheduledTask scheduledTask = task;
            if (scheduledTask != null) scheduledTask.cancel();
            viewers.values().forEach(player -> player.hideBossBar(bossBar));
            viewers.clear();
            sessions.remove(key, this);
        }
    }
}
