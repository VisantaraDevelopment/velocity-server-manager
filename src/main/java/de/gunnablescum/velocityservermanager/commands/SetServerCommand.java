package de.gunnablescum.velocityservermanager.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import de.gunnablescum.velocityservermanager.ServerManager;
import de.gunnablescum.velocityservermanager.utils.DatabaseRegisteredServer;
import de.gunnablescum.velocityservermanager.utils.Messages;
import de.gunnablescum.velocityservermanager.utils.MySQL;
import de.gunnablescum.velocityservermanager.utils.VSMCommand;

public class SetServerCommand extends VSMCommand {

    public SetServerCommand(ServerManager plugin, CommandManager manager) {
        super(plugin, manager, "setserver", "editserver");
    }

    @Override
    protected BrigadierCommand createBrigadierCommand(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> setServerNode = BrigadierCommand.literalArgumentBuilder("setserver")
                .requires(source -> source.hasPermission("servermanager.servers.edit"))
                .then(BrigadierCommand.requiredArgumentBuilder("server", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            suggestRegisteredServers(builder);
                            proxyServer.getAllServers().forEach(server -> builder.suggest(server.getServerInfo().getName()));
                            return builder.buildFuture();
                        })
                        .then(BrigadierCommand.requiredArgumentBuilder("host", StringArgumentType.word())
                                .then(BrigadierCommand.requiredArgumentBuilder("port", IntegerArgumentType.integer(1, 65535))
                                        .executes(context -> {
                                            String serverName = StringArgumentType.getString(context, "server");
                                            String host = StringArgumentType.getString(context, "host");
                                            int port = IntegerArgumentType.getInteger(context, "port");
                                            runAsync(() -> updateServer(context.getSource(), serverName, host, port));
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .build();

        return new BrigadierCommand(setServerNode);
    }

    private void updateServer(CommandSource source, String serverName, String host, int port) {
        DatabaseRegisteredServer server = MySQL.getServer(serverName);
        if (server == null) {
            if (plugin.getProxyServer().getServer(serverName).isEmpty()) {
                source.sendMessage(Messages.serverNotFound());
                return;
            }
            if (!MySQL.createServer(serverName, host, port)) {
                source.sendMessage(Messages.invalidArgs("SERVER_UPDATE_FAILED"));
                return;
            }
        } else if (!server.updateDetails(host, port)) {
            source.sendMessage(Messages.noActionCommited());
            return;
        }

        sendPermittedBroadcast(Messages.serverDetailsUpdatedBroadcast(getResponsible(source), serverName));
    }
}
