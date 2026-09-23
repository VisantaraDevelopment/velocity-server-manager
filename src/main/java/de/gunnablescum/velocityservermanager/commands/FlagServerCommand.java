package de.gunnablescum.velocityservermanager.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import de.gunnablescum.velocityservermanager.ServerManager;
import de.gunnablescum.velocityservermanager.utils.*;

public class FlagServerCommand extends VSMCommand {

    public FlagServerCommand(ServerManager plugin, CommandManager manager) {
        super(plugin, manager, "flagserver");
    }

    @Override
    protected BrigadierCommand createBrigadierCommand(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> enableServerNode = BrigadierCommand.literalArgumentBuilder("flagserver")
            .requires(source -> source.hasPermission("servermanager.servers.flags"))
            .then(BrigadierCommand.requiredArgumentBuilder("server", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        suggestRegisteredServers(builder);
                        return builder.buildFuture();
                    })
            .then(BrigadierCommand.requiredArgumentBuilder("flag", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        builder.suggest("lobby");
                        builder.suggest("limbo");
                        builder.suggest("restricted");
                        builder.suggest("disabled");
                        return builder.buildFuture();
                    })
                    .executes(context -> {
                        runAsync(() -> {
                            String serverName = StringArgumentType.getString(context, "server");
                            String flagName = StringArgumentType.getString(context, "flag").toUpperCase();

                            DatabaseRegisteredServer server = MySQL.getServer(serverName);
                            if (server == null) {
                                context.getSource().sendMessage(Messages.serverNotFound());
                                return;
                            }

                            ServerFlag flagValue = switch (flagName) {
                                case "LOBBY" -> ServerFlag.LOBBY;
                                case "LIMBO" -> ServerFlag.LIMBO;
                                case "RESTRICTED" -> ServerFlag.RESTRICTED;
                                case "DISABLED" -> ServerFlag.DISABLED;
                                default -> null;
                            };

                            if (flagValue == null) {
                                context.getSource().sendMessage(Messages.invalidArgs("INVALID_FLAG"));
                                return;
                            }

                            if (!server.setFlag(flagValue)) {
                                context.getSource().sendMessage(Messages.noActionCommited());
                                return;
                            }
                            sendPermittedBroadcast(Messages.flagsUpdated(getResponsible(context), serverName, ServerFlag.miniMessageFormatted(server.flags() | flagValue.bit)));
                        });
                        return Command.SINGLE_SUCCESS;
                    })
            )).build();

        return new BrigadierCommand(enableServerNode);
    }
}
