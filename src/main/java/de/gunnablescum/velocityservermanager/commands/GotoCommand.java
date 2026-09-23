package de.gunnablescum.velocityservermanager.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.gunnablescum.velocityservermanager.ServerManager;
import de.gunnablescum.velocityservermanager.utils.Messages;

import java.util.Optional;

/**
 * Created by Noah Fetz on 03.08.2016.
 * Contributors: GunnableScum
 */
public class GotoCommand {

    public GotoCommand(ServerManager plugin, CommandManager manager) {
        manager.register(manager.metaBuilder("goto").aliases("jumpto").plugin(plugin).build(), createBrigadierCommand(plugin.getProxyServer()));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
        LiteralCommandNode<CommandSource> gotoNode = BrigadierCommand.literalArgumentBuilder("goto")
                .requires(source -> source.hasPermission("servermanager.goto"))
                .executes(context -> {
                    ServerManager.getInstance().runAsync(() -> context.getSource().sendMessage(Messages.gotoDescription()));
                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            proxy.getAllPlayers().forEach(player -> builder.suggest(player.getUsername()));
                            return builder.buildFuture();
                        })
                ).executes(context -> {
                    ServerManager.getInstance().runAsync(() -> {
                        if (!(context.getSource() instanceof Player p)) {
                            context.getSource().sendMessage(Messages.onlyIngameCommand());
                            return;
                        }
                        String pname = StringArgumentType.getString(context, "player");
                        Optional<Player> p2 = proxy.getPlayer(pname);
                        if (p2.isEmpty()) {
                            p.sendMessage(Messages.gotoPlayerOffline(pname));
                            return;
                        }
                        Optional<RegisteredServer> target = p2.get().getCurrentServer().map(connection -> connection.getServer());
                        if (target.isEmpty()) {
                            p.sendMessage(Messages.gotoPlayerOffline(pname));
                            return;
                        }
                        if (p.getCurrentServer().map(connection -> connection.getServer()).filter(target.get()::equals).isEmpty()) {
                            p.createConnectionRequest(target.get()).connect().thenAccept(result -> p.sendMessage(Messages.gotoConnected(pname)));
                        } else {
                            p.sendMessage(Messages.gotoAlreadyOnServer(pname));
                        }
                    });
                    return Command.SINGLE_SUCCESS;
                }).build();

        return new BrigadierCommand(gotoNode);
    }
}
