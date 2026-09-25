# Velocity Server Manager

This project is a fork of [BungeeServerManager](https://git.fetz-gr.ch/NoahFetz/bungeeservermanager) by Noah Fetz, edited to work on Velocity Proxies.<br>
This project follows the MIT License as per upstream, it has not been edited.

Check out the original Plugin on SpigotMC here: [BungeeServerManager \[BungeeCord\] \[MySQL\]](https://www.spigotmc.org/resources/bungeeservermanager-bungeecord-mysql.24837/)

## Features
 * Add or Remove Servers from your Velocity Proxy without restarting
 * Restriction who can join specific servers with permissions
 * Jump to any player's server you want
 * Servers are saved in a MySQL Database
 * Receive notifications when servers go online or offline
 * Kick all players from a specific server to a random lobby
 * Route kicked players to available hubs first, with limbo fallback when all hubs are offline
 * Announce server restarts with lobby bossbars and balanced player evacuation
 * Manage Server Flags to customize server behavior
 * /hub or /lobby command to go to a lobby server, separate from limbo servers
 * Configurable messages

## Known Issues.
- No known issues at this time.

## Installation

### Prerequisites
You'll need:
 * A Proxy that is running Velocity (duh)
 * A MySQL or MariaDB Database
 * About 5-10 minutes of your time to set everything up

### Setup
1. Download the latest release of Velocity Server Manager.
2. Place the downloaded `.jar` file into your `plugins` folder of your Velocity Proxy.
3. Start your Proxy to generate the default configuration files. It will shutdown automatically due to a missing database connection.
4. Open the generated `mysql.yml` file in a text editor and fill in the details for a valid database connection.
5. Start your Proxy again. The plugin should connect to the database and create the necessary tables.
6. Add each managed server with `/addserver <name> <host> <port>`, or adopt a server already listed in `velocity.toml` with `/setserver <name> <host> <port>`. VSM stores its address in the database and applies address changes while the proxy is running.
7. Mark at least one server as a lobby with `/flagserver <name> lobby`. Mark an authentication server as limbo with `/flagserver <name> limbo`; `/hub` ignores limbo servers, and VSM leaves limbo connections and kicks to the authentication plugin.
8. (Optional) Configure the messages in `messages.yml` to your liking.

You are now ready to use Velocity Server Manager! Have fun managing your servers on the fly, dynamically.

### FAQ
If you read this, you've probably encountered an issue or are interested in some other stuff. Here are some common things and what to do:
1. **The Proxy shuts down immediately after starting!**
   - Make sure your database connection details in `mysql.yml` are correct.
   - Ensure that your database server is running and accessible from the machine running the Proxy.
   - If any of the above do not apply, check the Proxy logs for any error messages related to the plugin. It is probably not this plugin's fault. (Hopefully)
2. **I can't connect to a server that I added!**
   - Make sure the server is online and reachable from the Proxy.
   - Check `/server` to see if the server is listed. If it isn't, it probably has the `DISABLED` [flag](#flags) set.
   - Check if the server is marked as `RESTRICTED` and ensure you have the necessary permissions to join it.
3. **The plugin doesn't seem to be working at all!**
   - Update Velocity if you haven't updated in a while.
   - Check the Proxy logs for any error messages related to the plugin.
4. **I found a bug or have a feature request!**
   - Open an issue [here](https://github.com/GunnableScum/velocity-server-manager/issues) on GitHub describing the problem or feature you'd like to see.
   - Please do not contact me on any other platform regarding bugs or features. GitHub is the only place where I want and will track these things. I've had somebody interested in a different project message me on Reddit in a Private Chat. Who does that?

## Permissions
Have fun configuring to your heart's desire.
 * `servermanager.goto` - Be able to use /goto and /jumpto.
 * `servermanager.help` - See the help list.
 * `servermanager.notify` - Receive notifications when somebody manages servers.
 * `servermanager.servers.add` - Add servers to your network.
 * `servermanager.servers.edit` - Change a server's host and port while the proxy is running.
 * `servermanager.servers.delete` - Delete servers from your network.
 * `servermanager.servers.reload` - Reload server data from the database and re-add them to the proxy.
 * `servermanager.servers.list` - List all servers in your network.
 * `servermanager.servers.kick` - Kick all players from a specific server to a random lobby.
 * `servermanager.servers.restart` - Announce a server restart, show a lobby bossbar, and evacuate its players.
 * `servermanager.ignorekick` - Be exempt from being kicked when a server is cleared.
 * `servermanager.servers.info` - View information about a specific server.
 * `servermanager.servers.flags` - Manage Server flags. Check [here](#flags) for more info.
 * `servermanager.server.[servername]` - Access a specific server if it has the `RESTRICTED` flag.
 * `servermanager.server.*` - Access all servers that have the `RESTRICTED` flag.
 * `servermanager.ignorerestriction` - Ditto of the above, if your Permission system does not support wildcards.

## Commands
- `[hub, lobby]` - Go to a Lobby Server
- `[goto, jumpto]` - Go to a player's current server
- `[whereami, wai]` - Find which server you are currently on
- `[addserver]` - Adds a server to your network
- `[setserver, editserver]` - Changes a server's host and port while the proxy is running
- `[delserver]` - Deletes a server from your network
- `[reloadserver]` - Reloads the data of a specific server or all servers in the network
- `[serverlist, sl]` - Lists all servers in your network
- `[serverinfo, si]` - Shows some information about a specific server
- `[flagserver]` - Add a flag to a server
- `[unflagserver]` - Remove a flag from a server
- `[clearserver, kickserver]` - Kicks all players from the specific server to a random lobby
- `[serveradmin restart <server> [estimated-seconds]]` - Announces a restart, moves players evenly across available hubs, and shows a countdown bossbar to players in hubs. The estimate defaults to 120 seconds; the bar is removed when the countdown reaches zero.
- `[servermanager]` - A unified command for every action

## Flags
Flags identify server roles and conditions.
 * `EMPTY (Bitvalue: 0)` - No flags set
 * `LOBBY (Bitvalue: 1)` - Hub servers. Players are sent here when they join, use `/hub`, and as the first fallback after a backend kick.
 * `RESTRICTED (Bitvalue: 2)` - This server is restricted. Only players with the `servermanager.server.*` or `servermanager.ignorerestriction` permission can join.
 * `DISABLED (Bitvalue: 4)` - This server is disabled. Players cannot join this server, not even staff. If you want this server to be joinable by only staff, use RESTRICTED instead.
 * `LIMBO (Bitvalue: 16)` - A separate server role for limbo and authentication servers. Fallbacks use limbo if all hubs are offline; `/hub` never selects limbo. VSM preserves their initial connections and leaves their kick handling to other plugins.

**NOTE: Flags of Servers are stored in the database. Please do not tamper with the database manually unless you know exactly what you are doing. I will not fix any bugs where the database has been manually tampered with. You are on your own.**

## Contribution
I made this as a small side project to learn a tiny bit more about Programming for the Velocity Proxy. I will probably not look at PR Requests anytime soon. If you do feel up to the task, you can fork this repository instead.<br>
Just make sure to follow the license.<br>
Instead, if you'd like to support me financially, you can [Donate on my Ko-Fi Page](https://ko-fi.com/GunnableScum)!

## License

This fork is licensed under the upstream MIT License - see the LICENSE file here or [upstream](https://git.fetz-gr.ch/NoahFetz/bungeeservermanager/-/blob/master/LICENSE?ref_type=heads) for details
