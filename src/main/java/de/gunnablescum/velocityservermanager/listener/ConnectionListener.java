package de.gunnablescum.velocityservermanager.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import de.gunnablescum.velocityservermanager.ServerManager;

/**
 * Created by Noah Fetz on 21.05.2016.
 * Contributors: GunnableScum
 */
public class ConnectionListener {

    public ConnectionListener(ServerManager plugin) {
        plugin.getProxyServer().getEventManager().register(plugin, this);
    }

    @Subscribe
    public void onJoin(ServerPreConnectEvent e) {
        if (e.getPreviousServer() != null) return;
        ServerManager.getRandomLobby().ifPresent(lobby ->
                e.setResult(ServerPreConnectEvent.ServerResult.allowed(lobby)));
    }
}
