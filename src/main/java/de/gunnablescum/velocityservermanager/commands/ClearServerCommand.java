package de.gunnablescum.velocityservermanager.commands;

import com.mojang.brigadier.Command;
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

public class ClearServerCommand extends VSMCommand {

    public ClearServerCommand(ServerManager plugin, CommandManager manager) {
        super(plugin, manager, "clearserver", "kickserver");
    }

    @Override
    protected BrigadierCommand createBrigadierCommand(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> enableServerNode = BrigadierCommand.literalArgumentBuilder("clearserver")
            .requires(source -> source.hasPermission("servermanager.servers.kick"))
            .then(BrigadierCommand.requiredArgumentBuilder("server", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    suggestRegisteredServers(builder);
                    return builder.buildFuture();
                })
                .executes(context -> {
                    runAsync(() -> {
                        String server = StringArgumentType.getString(context, "server");
                        DatabaseRegisteredServer target = MySQL.getServer(server);
                        if (target == null) {
                            context.getSource().sendMessage(Messages.serverNotFound());
                            return;
                        }
                        if (target.getFromProxy() == null || !target.active()) {
                            context.getSource().sendMessage(Messages.serverNotActive());
                            return;
                        }
                        target.empty(false);
                        sendPermittedBroadcast(Messages.serverEmptiedBroadcast(getResponsible(context), server));
                    });
                    return Command.SINGLE_SUCCESS;
                })).build();

        return new BrigadierCommand(enableServerNode);
    }
}
