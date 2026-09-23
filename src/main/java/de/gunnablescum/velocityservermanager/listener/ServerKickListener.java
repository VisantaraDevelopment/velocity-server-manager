package de.gunnablescum.velocityservermanager.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import de.gunnablescum.velocityservermanager.ServerManager;

/**
 * Created by Noah Fetz on 06.09.2016.
 * Contributors: GunnableScum
 */
public class ServerKickListener {

    public ServerKickListener(ServerManager plugin) {
        plugin.getProxyServer().getEventManager().register(plugin, this);
    }

    @Subscribe
    public void onKick(KickedFromServerEvent event) {
        // If a player was kicked while attempting another server and still has a previous
        // connection, keep Velocity's default behavior so they remain on that previous server.
        if (event.kickedDuringServerConnect() && event.getPlayer().getCurrentServer().isPresent()) return;
        String kickedServer = event.getServer().getServerInfo().getName();
        ServerManager.getRandomFallbackExcluding(kickedServer)
                .ifPresent(destination -> event.setResult(KickedFromServerEvent.RedirectPlayer.create(destination)));
    }
}
