package de.gunnablescum.velocityservermanager.utils;

import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.gunnablescum.velocityservermanager.ServerManager;
import net.kyori.adventure.text.Component;

public class VSMCommand {

    protected final ServerManager plugin;

    public VSMCommand(ServerManager plugin, CommandManager manager, String... names) {
        this.plugin = plugin;
        var b = manager.metaBuilder(names[0]);
        for(int i = 1; i < names.length; i++) {
            b.aliases(names[i]);
        }
        b.plugin(plugin);
        manager.register(b.build(), createBrigadierCommand(plugin.getProxyServer()));

    }

    protected BrigadierCommand createBrigadierCommand(ProxyServer proxyServer) {
        ServerManager.getInstance().getLogger().error("Command not implemented. Contact the Developer via a GitHub Issue if you see this message during Runtime: https://github.com/GunnableScum/velocity-server-manager/issues");
        return null;
    }

    public static void sendPermittedBroadcast(Component component){
        ProxyServer proxyServer = ServerManager.getInstance().getProxyServer();
        proxyServer.getConsoleCommandSource().sendMessage(component);
        proxyServer.getAllPlayers().stream().filter(all -> all.hasPermission("servermanager.notify")).forEach(all -> all.sendMessage(component));
    }

    protected static String getResponsible(CommandContext<CommandSource> context) {
        return getResponsible(context.getSource());
    }

    public static String getResponsible(CommandSource source) {
        String name = "the network Administrator";
        if(source instanceof Player p) {
            name = p.getUsername();
        }
        return name;
    }

    protected void runAsync(Runnable operation) {
        plugin.runAsync(operation);
    }

    protected static void suggestRegisteredServers(com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        ServerManager.serverRegistry.keySet().forEach(builder::suggest);
    }

}
