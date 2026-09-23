package de.gunnablescum.velocityservermanager.commands;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.gunnablescum.velocityservermanager.ServerManager;
import de.gunnablescum.velocityservermanager.utils.Messages;

import java.util.Optional;

/**
 * Created by Noah Fetz on 09.06.2016.
 * Contributors: GunnableScum
 */
public class HubCommand implements SimpleCommand {

    private final ServerManager plugin;

    public HubCommand(ServerManager plugin, CommandManager manager) {
        this.plugin = plugin;
        manager.register(manager.metaBuilder("hub").aliases("lobby").plugin(plugin).build(), this);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        plugin.runAsync(() -> {
            if (!(source instanceof Player p)) {
                source.sendMessage(Messages.onlyIngameCommand());
                return;
            }
            Optional<String> currentName = p.getCurrentServer()
                    .map(connection -> connection.getServer().getServerInfo().getName());
            if (currentName.filter(name -> ServerManager.lobbies.stream()
                    .anyMatch(lobby -> lobby.getServerInfo().getName().equalsIgnoreCase(name))).isPresent()) {
                p.sendMessage(Messages.alreadyOnLobby());
                return;
            }

            ServerManager.getRandomLobby().ifPresentOrElse(
                    lobby -> p.createConnectionRequest(lobby).connect(),
                    () -> p.sendMessage(Messages.noLobbyAvailable())
            );
        });
    }
}
