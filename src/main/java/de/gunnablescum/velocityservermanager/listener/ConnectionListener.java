package de.gunnablescum.velocityservermanager.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import de.gunnablescum.velocityservermanager.utils.DatabaseRegisteredServer;
import de.gunnablescum.velocityservermanager.ServerManager;

/**
 * Created by Noah Fetz on 21.05.2016.
 * Contributors: GunnableScum
 */
public class ConnectionListener {

    public ConnectionListener(ServerManager plugin) {
        plugin.getProxyServer().getEventManager().register(plugin, this);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onJoin(ServerPreConnectEvent e) {
        if (e.getPreviousServer() != null) return;

        String originalServerName = e.getOriginalServer().getServerInfo().getName();
        DatabaseRegisteredServer managedServer = ServerManager.getManagedServer(originalServerName).orElse(null);
        // Authentication backends may be intentionally outside VSM's server list, or marked as
        // limbo. Leave those first connections to the proxy/auth plugin.
        if (managedServer == null || managedServer.isLimbo()) return;

        // Keep a route another plugin has already selected (for example nLogin's auth server).
        if (e.getResult().getServer().filter(server ->
                !server.getServerInfo().getName().equalsIgnoreCase(originalServerName)
        ).isPresent()) return;

        ServerManager.getRandomFallback().ifPresent(fallback ->
                e.setResult(ServerPreConnectEvent.ServerResult.allowed(fallback)));
    }
}
