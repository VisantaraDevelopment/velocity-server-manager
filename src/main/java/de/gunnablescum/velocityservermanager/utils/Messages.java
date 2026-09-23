package de.gunnablescum.velocityservermanager.utils;

import de.gunnablescum.velocityservermanager.ServerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.io.IOException;

/**
 * Created by Noah Fetz on 03.08.2016.
 * Contributors: GunnableScum
 */
public class Messages {
    public static Component PREFIX;

    private static String NO_PERMISSION;
    private static String SERVERS_LIST_HEADER;
    private static String ALL_SERVER_RELOAD_BROADCAST;
    private static String PREVIOUS_SERVER_DELETED_INFO;
    private static String SERVER_DELETED_BROADCAST;
    private static String SERVER_NOT_FOUND;
    private static String SERVER_LIST_INFO;
    private static String SERVER_RELOADED_BROADCAST;
    private static String SERVER_ENABLED_BROADCAST;
    private static String SERVER_DISABLED_BROADCAST;
    private static String SERVER_EMPTIED_BROADCAST;
    private static String SERVER_NOT_ACTIVE;
    private static String PREVIOUS_SERVER_EMPTIED;
    private static String GOTO_DESCRIPTION;
    private static String GOTO_PLAYER_NOT_ONLINE;
    private static String GOTO_CONNECTED;
    private static String GOTO_ALREADY_ON_SERVER;
    private static String ONLY_INGAME_COMMAND;
    private static String LOBBY_ALREADY_ON_LOBBY;
    private static String WHEREAMI_SERVER_INFO;
    private static String NO_ACTION_COMMITED;
    private static String INVALID_ARGS;
    private static String SERVER_ADDED_BROADCAST;
    private static String SERVER_ALREADY_EXISTS;
    private static String NO_LOBBY_AVAILABLE;
    private static String SERVER_DETAILS_UPDATED_BROADCAST;
    private static String SERVER_RESTART_ANNOUNCEMENT;
    private static String SERVER_RESTART_BOSSBAR;
    private static String SERVER_RESTART_ALREADY_RUNNING;
    private static String SERVER_RESTART_NO_DESTINATION;
    private static String SERVER_ADMIN_USAGE;
    private static String FLAGS_UPDATED;
    private static String SERVER_ONLINE_BROADCAST;
    private static String SERVER_OFFLINE_BROADCAST;

    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static void loadMessages(){
        Config config;
        try {
            config = new Config(ServerManager.getInstance().getDataDirectory(), "messages.yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PREFIX = mm.deserialize(config.getString("Messages.PREFIX", ""));
        
        NO_PERMISSION                   = config.getString("Messages.NO_PERMISSION", "");
        SERVERS_LIST_HEADER             = config.getString("Messages.SERVERS_LIST_HEADER", "");
        ALL_SERVER_RELOAD_BROADCAST     = config.getString("Messages.ALL_SERVER_RELOAD_BROADCAST", "");
        PREVIOUS_SERVER_DELETED_INFO    = config.getString("Messages.PREVIOUS_SERVER_DELETED_INFO", "");
        SERVER_DELETED_BROADCAST        = config.getString("Messages.SERVER_DELETED_BROADCAST", "");
        SERVER_NOT_FOUND                = config.getString("Messages.SERVER_NOT_FOUND", "");
        SERVER_LIST_INFO                = config.getString("Messages.SERVER_LIST_INFO", "");
        SERVER_RELOADED_BROADCAST       = config.getString("Messages.SERVER_RELOADED_BROADCAST", "");
        SERVER_ENABLED_BROADCAST        = config.getString("Messages.SERVER_ENABLED_BROADCAST", "");
        SERVER_DISABLED_BROADCAST       = config.getString("Messages.SERVER_DISABLED_BROADCAST", "");
        SERVER_EMPTIED_BROADCAST        = config.getString("Messages.SERVER_EMPTIED_BROADCAST", "");
        SERVER_ADDED_BROADCAST          = config.getString("Messages.SERVER_ADDED_BROADCAST", "");
        SERVER_NOT_ACTIVE               = config.getString("Messages.SERVER_NOT_ACTIVE", "");
        PREVIOUS_SERVER_EMPTIED         = config.getString("Messages.PREVIOUS_SERVER_EMPTIED", "");
        GOTO_DESCRIPTION                = config.getString("Messages.GOTO_DESCRIPTION", "");
        GOTO_PLAYER_NOT_ONLINE          = config.getString("Messages.GOTO_PLAYER_NOT_ONLINE", "");
        GOTO_CONNECTED                  = config.getString("Messages.GOTO_CONNECTED", "");
        GOTO_ALREADY_ON_SERVER          = config.getString("Messages.GOTO_ALREADY_ON_SERVER", "");
        ONLY_INGAME_COMMAND             = config.getString("Messages.ONLY_INGAME_COMMAND", "");
        LOBBY_ALREADY_ON_LOBBY          = config.getString("Messages.LOBBY_ALREADY_ON_LOBBY", "");
        WHEREAMI_SERVER_INFO            = config.getString("Messages.WHEREAMI_SERVER_INFO", "");
        NO_ACTION_COMMITED              = config.getString("Messages.NO_ACTION_COMMITED", "");
        INVALID_ARGS                    = config.getString("Messages.INVALID_ARGS", "");
        SERVER_ALREADY_EXISTS           = config.getString("Messages.SERVER_ALREADY_EXISTS", "");
        NO_LOBBY_AVAILABLE              = config.getString("Messages.NO_LOBBY_AVAILABLE", "<red>No lobby server is configured.");
        SERVER_DETAILS_UPDATED_BROADCAST = config.getString("Messages.SERVER_DETAILS_UPDATED_BROADCAST", "<gray>The address of server <yellow><server></yellow> has been updated by <admin>.");
        SERVER_RESTART_ANNOUNCEMENT = config.getString("Messages.SERVER_RESTART_ANNOUNCEMENT", "<yellow><server> is restarting. Estimated time: <estimate>.");
        SERVER_RESTART_BOSSBAR = config.getString("Messages.SERVER_RESTART_BOSSBAR", "<yellow><server> sedang restart, mohon menunggu... (estimasi <estimate>)");
        SERVER_RESTART_ALREADY_RUNNING = config.getString("Messages.SERVER_RESTART_ALREADY_RUNNING", "<red>A restart announcement is already active for this server.");
        SERVER_RESTART_NO_DESTINATION = config.getString("Messages.SERVER_RESTART_NO_DESTINATION", "<red>There is no available hub or limbo to evacuate players to.");
        SERVER_ADMIN_USAGE = config.getString("Messages.SERVER_ADMIN_USAGE", "<yellow>/serveradmin restart <server> [estimated-seconds]");
        FLAGS_UPDATED                   = config.getString("Messages.FLAGS_UPDATED", "");
        SERVER_ONLINE_BROADCAST         = config.getString("Messages.SERVER_ONLINE_BROADCAST", "");
        SERVER_OFFLINE_BROADCAST        = config.getString("Messages.SERVER_OFFLINE_BROADCAST", "");
    }

    // No Variables
    public static Component noPermission() { return PREFIX.append(mm.deserialize(NO_PERMISSION)); }
    public static Component serversListHeader() { return PREFIX.append(mm.deserialize(SERVERS_LIST_HEADER)); }
    public static Component previousServerDeletedInfo() { return PREFIX.append(mm.deserialize(PREVIOUS_SERVER_DELETED_INFO)); }
    public static Component serverNotFound() { return PREFIX.append(mm.deserialize(SERVER_NOT_FOUND)); }
    public static Component serverNotActive() { return PREFIX.append(mm.deserialize(SERVER_NOT_ACTIVE)); }
    public static Component previousServerEmptied() { return PREFIX.append(mm.deserialize(PREVIOUS_SERVER_EMPTIED)); }
    public static Component gotoDescription() { return PREFIX.append(mm.deserialize(GOTO_DESCRIPTION)); }
    public static Component onlyIngameCommand() { return PREFIX.append(mm.deserialize(ONLY_INGAME_COMMAND)); }
    public static Component alreadyOnLobby() { return PREFIX.append(mm.deserialize(LOBBY_ALREADY_ON_LOBBY)); }
    public static Component noActionCommited() { return PREFIX.append(mm.deserialize(NO_ACTION_COMMITED)); }
    public static Component serverAlreadyExists() { return PREFIX.append(mm.deserialize(SERVER_ALREADY_EXISTS)); }
    public static Component noLobbyAvailable() { return PREFIX.append(mm.deserialize(NO_LOBBY_AVAILABLE)); }

    public static Component serverDetailsUpdatedBroadcast(String admin, String serverName) {
        return PREFIX.append(mm.deserialize(
                SERVER_DETAILS_UPDATED_BROADCAST,
                Placeholder.unparsed("admin", admin),
                Placeholder.unparsed("server", serverName)
        ));
    }

    public static Component serverRestartAnnouncement(String serverName, String estimate) {
        return PREFIX.append(mm.deserialize(
                SERVER_RESTART_ANNOUNCEMENT,
                Placeholder.unparsed("server", serverName),
                Placeholder.unparsed("estimate", estimate)
        ));
    }

    public static Component serverRestartBossBar(String serverName, String estimate) {
        return mm.deserialize(
                SERVER_RESTART_BOSSBAR,
                Placeholder.unparsed("server", serverName),
                Placeholder.unparsed("estimate", estimate)
        );
    }

    public static Component restartAlreadyRunning() {
        return PREFIX.append(mm.deserialize(SERVER_RESTART_ALREADY_RUNNING));
    }

    public static Component restartNoDestination() {
        return PREFIX.append(mm.deserialize(SERVER_RESTART_NO_DESTINATION));
    }

    public static Component serverAdminUsage() {
        return PREFIX.append(mm.deserialize(SERVER_ADMIN_USAGE));
    }

    // One Variable
    public static Component allServerReloadBroadcast(String admin) {
        return PREFIX.append(mm.deserialize(
                ALL_SERVER_RELOAD_BROADCAST,
                Placeholder.unparsed("admin", admin)
        ));
    }

    public static Component serverOnlineBroadcast(String serverName) {
        return PREFIX.append(mm.deserialize(
                SERVER_ONLINE_BROADCAST,
                Placeholder.unparsed("server", serverName)
        ));
    }

    public static Component serverOfflineBroadcast(String serverName) {
        return PREFIX.append(mm.deserialize(
                SERVER_OFFLINE_BROADCAST,
                Placeholder.unparsed("server", serverName)
        ));
    }

    public static Component serverAddedBroadcast(String admin, String serverName) {
        return PREFIX.append(mm.deserialize(
                SERVER_ADDED_BROADCAST,
                Placeholder.unparsed("admin", admin),
                Placeholder.unparsed("server", serverName)
        ));
    }

    public static Component gotoPlayerOffline(String playerName) {
        return PREFIX.append(mm.deserialize(
                GOTO_PLAYER_NOT_ONLINE,
                Placeholder.unparsed("player", playerName)
        ));
    }

    public static Component gotoConnected(String serverName) {
        return PREFIX.append(mm.deserialize(
                GOTO_CONNECTED,
                Placeholder.unparsed("server", serverName)
        ));
    }

    public static Component gotoAlreadyOnServer(String serverName) {
        return PREFIX.append(mm.deserialize(
                GOTO_ALREADY_ON_SERVER,
                Placeholder.unparsed("server", serverName)
        ));
    }

    public static Component whereAmIServerInfo(String serverName) {
        return PREFIX.append(mm.deserialize(
                WHEREAMI_SERVER_INFO,
                Placeholder.unparsed("server", serverName)
        ));
    }

    public static Component invalidArgs(String error) {
        return PREFIX.append(mm.deserialize(
                INVALID_ARGS,
                Placeholder.unparsed("err", error)
        ));
    }

    // Two Variables
    public static Component serverDeletedBroadcast(String admin, String serverName) {
        return PREFIX.append(mm.deserialize(
                SERVER_DELETED_BROADCAST,
                Placeholder.unparsed("admin", admin),
                Placeholder.unparsed("server", serverName)
        ));
    }

    public static Component serverReloadedBroadcast(String admin, String serverName) {
        return PREFIX.append(mm.deserialize(
                SERVER_RELOADED_BROADCAST,
                Placeholder.unparsed("admin", admin),
                Placeholder.unparsed("server", serverName)
        ));
    }

    public static Component serverEnabledBroadcast(String admin, String serverName) {
        return PREFIX.append(mm.deserialize(
                SERVER_ENABLED_BROADCAST,
                Placeholder.unparsed("admin", admin),
                Placeholder.unparsed("server", serverName)
        ));
    }

    public static Component serverDisabledBroadcast(String admin, String serverName) {
        return PREFIX.append(mm.deserialize(
                SERVER_DISABLED_BROADCAST,
                Placeholder.unparsed("admin", admin),
                Placeholder.unparsed("server", serverName)
        ));
    }

    public static Component serverEmptiedBroadcast(String admin, String serverName) {
        return PREFIX.append(mm.deserialize(
                SERVER_EMPTIED_BROADCAST,
                Placeholder.unparsed("admin", admin),
                Placeholder.unparsed("server", serverName)
        ));
    }

    // Three Variables
    public static Component serverListInfo(String id, String server, String flags) {
        return mm.deserialize(
                SERVER_LIST_INFO,
                Placeholder.parsed("id", id),
                Placeholder.parsed("server", server),
                Placeholder.parsed("flags", flags)
        );
    }

    public static Component flagsUpdated(String admin, String server, String flags) {
        return PREFIX.append(mm.deserialize(
                FLAGS_UPDATED,
                Placeholder.unparsed("admin", admin),
                Placeholder.unparsed("server", server),
                Placeholder.parsed("flags", flags)
        ));
    }
}
