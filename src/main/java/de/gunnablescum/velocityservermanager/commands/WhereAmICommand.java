package de.gunnablescum.velocityservermanager.commands;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.gunnablescum.velocityservermanager.ServerManager;
import de.gunnablescum.velocityservermanager.utils.Messages;

/**
 * Created by Noah Fetz on 17.06.2016.
 * Contributors: GunnableScum
 */
public class WhereAmICommand implements SimpleCommand {

    private final ServerManager plugin;

    public WhereAmICommand(ServerManager plugin, CommandManager manager) {
        this.plugin = plugin;
        manager.register(manager.metaBuilder("whereami").aliases("wai").plugin(plugin).build(), this);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        plugin.runAsync(() -> {
            if (!(source instanceof Player p)) {
                source.sendMessage(Messages.onlyIngameCommand());
                return;
            }
            p.getCurrentServer().ifPresent(connection ->
                    p.sendMessage(Messages.whereAmIServerInfo(connection.getServerInfo().getName())));
        });
    }
}
