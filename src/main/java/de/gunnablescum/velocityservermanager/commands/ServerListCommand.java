package de.gunnablescum.velocityservermanager.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import de.gunnablescum.velocityservermanager.ServerManager;
import de.gunnablescum.velocityservermanager.utils.*;

public class ServerListCommand extends VSMCommand {

    public ServerListCommand(ServerManager plugin, CommandManager manager) {
        super(plugin, manager, "serverlist", "sl");
    }

    @Override
    protected BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
        LiteralCommandNode<CommandSource> serverInfoNode = BrigadierCommand.literalArgumentBuilder("serverlist")
            .requires(source -> source.hasPermission("servermanager.servers.list"))
            .executes(context -> {
                runAsync(() -> printServerList(context.getSource()));
                return Command.SINGLE_SUCCESS;
            }).build();

        return new BrigadierCommand(serverInfoNode);
    }

    private static void printServerList(CommandSource source) {
        if(!source.hasPermission("servermanager.servers.list")) {
            source.sendMessage(Messages.noPermission());
            return;
        }
        source.sendMessage(Messages.serversListHeader());
        int i = 1;
        for (DatabaseRegisteredServer server : MySQL.getAllServers()) {
            source.sendMessage(Messages.serverListInfo(
                    String.valueOf(i),
                    onlineOfflineString(server),
                    flagString(server)
            ));
            i++;
        }
    }

    // If there's more than one flag, just return "<yellow>Multiple Flags</yellow>"
    private static String flagString(DatabaseRegisteredServer server) {
        try {
            return ServerFlag.valueOf(server.flags()).toString();
        } catch (IllegalArgumentException e) {
            return "<yellow>Multiple Flags</yellow>";
        }
    }

    private static String onlineOfflineString(DatabaseRegisteredServer server) {
        return (ServerManager.serverStatusCache.getOrDefault(server.name(), false) ? "<green>✔" : "<red>❌") + server.name() + "<reset>";
    }

}
