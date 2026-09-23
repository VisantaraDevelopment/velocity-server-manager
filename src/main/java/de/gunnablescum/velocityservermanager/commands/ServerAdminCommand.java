package de.gunnablescum.velocityservermanager.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.gunnablescum.velocityservermanager.ServerManager;
import de.gunnablescum.velocityservermanager.utils.Messages;
import de.gunnablescum.velocityservermanager.utils.RestartAnnouncementManager;
import de.gunnablescum.velocityservermanager.utils.VSMCommand;

import java.util.Optional;

public class ServerAdminCommand extends VSMCommand {

    private final RestartAnnouncementManager restartAnnouncements;

    public ServerAdminCommand(ServerManager plugin, CommandManager manager, RestartAnnouncementManager restartAnnouncements) {
        super(plugin, manager, "serveradmin");
        this.restartAnnouncements = restartAnnouncements;
    }

    @Override
    protected BrigadierCommand createBrigadierCommand(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> serverAdminNode = BrigadierCommand.literalArgumentBuilder("serveradmin")
                .requires(source -> source.hasPermission("servermanager.servers.restart"))
                .executes(context -> {
                    runAsync(() -> context.getSource().sendMessage(Messages.serverAdminUsage()));
                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.literalArgumentBuilder("restart")
                        .then(BrigadierCommand.requiredArgumentBuilder("server", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    suggestRegisteredServers(builder);
                                    proxyServer.getAllServers().forEach(server -> builder.suggest(server.getServerInfo().getName()));
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    startRestart(
                                            context.getSource(),
                                            StringArgumentType.getString(context, "server"),
                                            RestartAnnouncementManager.DEFAULT_ESTIMATE_SECONDS
                                    );
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(BrigadierCommand.requiredArgumentBuilder("estimatedSeconds", IntegerArgumentType.integer(1, 86400))
                                        .executes(context -> {
                                            startRestart(
                                                    context.getSource(),
                                                    StringArgumentType.getString(context, "server"),
                                                    IntegerArgumentType.getInteger(context, "estimatedSeconds")
                                            );
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .build();

        return new BrigadierCommand(serverAdminNode);
    }

    private void startRestart(CommandSource source, String serverName, int estimatedSeconds) {
        runAsync(() -> {
            Optional<RegisteredServer> server = plugin.getProxyServer().getServer(serverName);
            if (server.isEmpty()) {
                source.sendMessage(Messages.serverNotFound());
                return;
            }

            RestartAnnouncementManager.StartResult result = restartAnnouncements.start(server.get(), estimatedSeconds);
            switch (result) {
                case ALREADY_RUNNING -> source.sendMessage(Messages.restartAlreadyRunning());
                case NO_DESTINATION -> source.sendMessage(Messages.restartNoDestination());
                case STARTED -> { }
            }
        });
    }
}
