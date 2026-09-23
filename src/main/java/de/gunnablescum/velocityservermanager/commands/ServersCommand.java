package de.gunnablescum.velocityservermanager.commands;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import de.gunnablescum.velocityservermanager.ServerManager;
import de.gunnablescum.velocityservermanager.utils.DatabaseRegisteredServer;
import de.gunnablescum.velocityservermanager.utils.Messages;
import de.gunnablescum.velocityservermanager.utils.MySQL;
import de.gunnablescum.velocityservermanager.utils.ServerFlag;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

import static de.gunnablescum.velocityservermanager.utils.VSMCommand.*;

public class ServersCommand implements RawCommand {

    public ServersCommand(CommandManager manager) {
        manager.register(manager.metaBuilder("servers").aliases("sm", "servermanager").build(), this);
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

    @Override
    public void execute(Invocation invocation) {
        ServerManager.getInstance().runAsync(() -> executeDatabaseCommand(invocation));
    }

    private void executeDatabaseCommand(Invocation invocation) {
        CommandSource source = invocation.source();
        String arguments = invocation.arguments().trim();
        String[] args = arguments.isEmpty() ? new String[0] : arguments.split("\\s+");
        MiniMessage mm = MiniMessage.miniMessage();

        if (args.length == 0) {
            sendAvailableCommands(source, mm);
            return;
        }

        String action = args[0].toLowerCase();

        if (args.length == 1) {
            switch (action) {
                case "list" -> printServerList(source);
                case "reload" -> {
                    if(!source.hasPermission("servermanager.servers.reload")) {
                        source.sendMessage(Messages.noPermission());
                        return;
                    }
                    for (DatabaseRegisteredServer target : MySQL.getAllServers()) {
                        target.reloadInProxy();
                    }
                    sendPermittedBroadcast(Messages.allServerReloadBroadcast(getResponsible(source)));
                }
                default -> sendAvailableCommands(source, mm);
            }
            return;
        }

        String target = args[1];
        DatabaseRegisteredServer targetServer = MySQL.getServer(target);
        if (targetServer == null && !action.equalsIgnoreCase("add")) {
            source.sendMessage(Messages.serverNotFound());
            return;
        }

        if (args.length == 2) {
            switch (action) {
                case "info" -> targetServer.sendInfo(source);
                case "enable" -> {
                    if(!source.hasPermission("servermanager.servers.flags")) {
                        source.sendMessage(Messages.noPermission());
                        return;
                    }
                    if (targetServer.unsetFlag(ServerFlag.DISABLED)) {
                        sendPermittedBroadcast(Messages.flagsUpdated(getResponsible(source), targetServer.name(), ServerFlag.miniMessageFormatted(targetServer.flags() & ~ServerFlag.DISABLED.bit)));
                        return;
                    }
                    source.sendMessage(Messages.noActionCommited());
                    return;
                }
                case "disable" -> {
                    if(!source.hasPermission("servermanager.servers.flags")) {
                        source.sendMessage(Messages.noPermission());
                        return;
                    }
                    if (targetServer.setFlag(ServerFlag.DISABLED)) {
                        sendPermittedBroadcast(Messages.flagsUpdated(getResponsible(source), targetServer.name(), ServerFlag.miniMessageFormatted(targetServer.flags() | ServerFlag.DISABLED.bit)));
                        return;
                    }
                    source.sendMessage(Messages.noActionCommited());
                    return;
                }
                case "delete" -> {
                    if(!source.hasPermission("servermanager.servers.delete")) {
                        source.sendMessage(Messages.noPermission());
                        return;
                    }
                    targetServer.empty(true);
                    targetServer.deleteFromDatabase();
                    targetServer.removeFromProxy();
                    sendPermittedBroadcast(Messages.serverDeletedBroadcast(getResponsible(source), targetServer.name()));
                    return;
                }
                case "reload" -> {
                    if(!source.hasPermission("servermanager.servers.reload")) {
                        source.sendMessage(Messages.noPermission());
                        return;
                    }
                    targetServer.reloadInProxy();
                    sendPermittedBroadcast(Messages.serverReloadedBroadcast(getResponsible(source), targetServer.name()));
                }
                case "kick" -> {
                    if(!source.hasPermission("servermanager.servers.kick")) {
                        source.sendMessage(Messages.noPermission());
                        return;
                    }
                    targetServer.empty(false);
                    sendPermittedBroadcast(Messages.serverEmptiedBroadcast(getResponsible(source), targetServer.name()));
                }
                default -> sendAvailableCommands(source, mm);
            }
            return;
        }

        String flagOrIp = args[2];

        ServerFlag flagValue;

        switch (flagOrIp.toUpperCase()) {
            case "LOBBY" -> flagValue = ServerFlag.LOBBY;
            case "LIMBO" -> flagValue = ServerFlag.LIMBO;
            case "RESTRICTED" -> flagValue = ServerFlag.RESTRICTED;
            case "DISABLED" -> flagValue = ServerFlag.DISABLED;
            default -> flagValue = null;
        }

        if (args.length == 3) {
            switch (action) {
                case "flag" -> {
                    if(!source.hasPermission("servermanager.servers.flags")) {
                        source.sendMessage(Messages.noPermission());
                        return;
                    }
                    if (flagValue == null) {
                        sendAvailableCommands(source, mm);
                        return;
                    }
                    if (targetServer.setFlag(flagValue)) {
                        sendPermittedBroadcast(Messages.flagsUpdated(getResponsible(source), targetServer.name(), ServerFlag.miniMessageFormatted(targetServer.flags() | flagValue.bit)));
                        return;
                    }
                    source.sendMessage(Messages.noActionCommited());
                }
                case "unflag" -> {
                    if(!source.hasPermission("servermanager.servers.flags")) {
                        source.sendMessage(Messages.noPermission());
                        return;
                    }
                    if (flagValue == null) {
                        sendAvailableCommands(source, mm);
                        return;
                    }
                    if (targetServer.unsetFlag(flagValue)) {
                        sendPermittedBroadcast(Messages.flagsUpdated(getResponsible(source), targetServer.name(), ServerFlag.miniMessageFormatted(targetServer.flags() & ~flagValue.bit)));
                        return;
                    }
                    source.sendMessage(Messages.noActionCommited());
                }
                default -> sendAvailableCommands(source, mm);
            }
            return;
        }

        if (args.length == 4) {
            if (!action.equals("add")) {
                sendAvailableCommands(source, mm);
                return;
            }
            if(!source.hasPermission("servermanager.servers.add")) {
                source.sendMessage(Messages.noPermission());
                return;
            }
            int port;
            try {
                port = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                source.sendMessage(Messages.invalidArgs("NAN_PORT"));
                return;
            }

            if(port < 1 || port > 65535) {
                source.sendMessage(Messages.invalidArgs("PORT_OUT_OF_TCP_RANGE"));
                return;
            }

            if (MySQL.doesServerExist(target)) {
                source.sendMessage(Messages.serverAlreadyExists());
                return;
            }
            if (!MySQL.createServer(target, flagOrIp, port)) {
                source.sendMessage(Messages.invalidArgs("SERVER_ADD_FAILED"));
                return;
            }
            sendPermittedBroadcast(Messages.serverAddedBroadcast(getResponsible(source), target));
        }
    }

    private static void sendAvailableCommands(CommandSource source, MiniMessage mm) {
        if (!source.hasPermission("servermanager.help")) {
            source.sendMessage(Messages.noPermission());
            return;
        }
        source.sendMessage(Messages.PREFIX.append(mm.deserialize("<gray>The following commands are available to you<dark_gray>: <yellow>[optional] <needed>")));
        source.sendMessage(mm.deserialize("<yellow>/servermanager help <dark_gray>- <gray>Shows this help site"));
        if(source.hasPermission("servermanager.servers.add")) source.sendMessage(mm.deserialize("<yellow>/servermanager add <servername> <ip> <port> <dark_gray>- <gray>Adds a server to your network"));
        if(source.hasPermission("servermanager.servers.edit")) source.sendMessage(mm.deserialize("<yellow>/setserver <servername> <ip> <port> <dark_gray>- <gray>Updates a server's host and port"));
        if(source.hasPermission("servermanager.servers.delete")) source.sendMessage(mm.deserialize("<yellow>/servermanager delete <servername> <dark_gray>- <gray>Deletes a server from your network"));
        if(source.hasPermission("servermanager.servers.reload")) source.sendMessage(mm.deserialize("<yellow>/servermanager reload [servername] <dark_gray>- <gray>Reloads the data of a specific server or all servers in the network"));
        if(source.hasPermission("servermanager.servers.list")) source.sendMessage(mm.deserialize("<yellow>/servermanager list <dark_gray>- <gray>Lists all servers in your network"));
        if(source.hasPermission("servermanager.servers.info")) source.sendMessage(mm.deserialize("<yellow>/servermanager info <servername> <dark_gray>- <gray>Shows some information about a specific server"));
        if(source.hasPermission("servermanager.servers.flags")) {
            source.sendMessage(mm.deserialize("<yellow>/servermanager enable <servername> <dark_gray>- <gray>Enables a server, so players are able to connect to it"));
            source.sendMessage(mm.deserialize("<yellow>/servermanager disable <servername> <dark_gray>- <gray>Disables a server, so players can't connect to it"));
            source.sendMessage(mm.deserialize("<yellow>/servermanager flag <servername> <flag> <dark_gray>- <gray>Add a flag to a specific server"));
            source.sendMessage(mm.deserialize("<yellow>/servermanager unflag <servername> <flag> <dark_gray>- <gray>Remove a flag from a specific server"));
        }
        if(source.hasPermission("servermanager.servers.kick")) source.sendMessage(mm.deserialize("<yellow>/servermanager kick <servername> <dark_gray>- <gray>Kicks all players from the specific server to a random lobby"));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments().split(" ");
        boolean addArg = invocation.arguments().endsWith(" ");
        if (args.length == 1 && !addArg) {
            List<String> suggestions = new ArrayList<>();
            if(source.hasPermission("servermanager.servers.add")) suggestions.add("add");
            if(source.hasPermission("servermanager.servers.delete")) suggestions.add("delete");
            if(source.hasPermission("servermanager.servers.reload")) suggestions.add("reload");
            if(source.hasPermission("servermanager.servers.list")) suggestions.add("list");
            if(source.hasPermission("servermanager.servers.info")) suggestions.add("info");
            if(source.hasPermission("servermanager.servers.flags")) {
                suggestions.add("enable");
                suggestions.add("disable");
                suggestions.add("flag");
                suggestions.add("unflag");
            }
            if(source.hasPermission("servermanager.servers.kick")) suggestions.add("kick");
            return suggestions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        String action = args[0].toLowerCase();

        if (args.length == 2 && !addArg || args.length == 1) {
            switch (action) {
                case "info", "enable", "disable", "delete", "reload", "kick", "flag", "unflag" -> {
                    return suggestServerNames(source, args.length == 1 ? "" : args[1]);
                }
                default -> { // Add also falls through here
                    return List.of();
                }
            }
        }

        if (args.length == 2 || args.length == 3 && !addArg) {
            if (action.equals("add")) {
                return List.of("127.0.0.1");
            }
            if (action.equals("flag") || action.equals("unflag")) {
                return suggestFlags(args.length == 2 ? "" : args[2]);
            }
        }

        if (args.length == 3 && addArg) {
            if (action.equals("add")) {
                return List.of("25565");
            }
        }

        return List.of();
    }

    private List<String> suggestFlags(String arg) {
        List<String> flags = List.of("lobby", "limbo", "restricted", "disabled");
        return flags.stream()
                .filter(name -> name.startsWith(arg.toLowerCase()))
                .toList();
    }

    private List<String> suggestServerNames(CommandSource source, String arg) {
        if(!source.hasPermission("servermanager.servers.list")) {
            return List.of();
        }
        return ServerManager.serverRegistry.keySet().stream()
                .filter(name -> name.toLowerCase().startsWith(arg.toLowerCase()))
                .toList();
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
