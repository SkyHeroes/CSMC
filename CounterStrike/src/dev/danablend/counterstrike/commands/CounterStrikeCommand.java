package dev.danablend.counterstrike.commands;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.enums.GameState;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static dev.danablend.counterstrike.Config.MIN_PLAYERS;


public class CounterStrikeCommand implements CommandExecutor {

    CounterStrike plugin;
    FileConfiguration config;
    String Map = null;

    public CounterStrikeCommand(CounterStrike plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("You need to be a player to execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {

            if (plugin.getGameState().equals(GameState.LOBBY) || plugin.getGameState().equals(GameState.WAITING) || plugin.getGameState().equals(GameState.STARTING) || plugin.getGameState().equals(GameState.SHOP)) {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Game hasn't start yet");
            } else {
                plugin.getGameTimer().terminateTimer();
                plugin.FinishGame(CounterStrike.i.getTerroristsTeam(), CounterStrike.i.getCounterTerroristsTeam());
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("setRandMap")) {

            if (this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING) || this.plugin.getGameState().equals(GameState.STARTING)) {
                this.plugin.LoadDBRandomMaps(0);
            } else {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Too late to change map " + this.plugin.getGameState());
            }
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("setMinPlayers")) {

            if (this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING)) {
                config.addDefault("min-players", args[1]);
                MIN_PLAYERS = Integer.parseInt(args[1]);
                config.options().copyDefaults(true);

                plugin.saveConfig();
                plugin.loadConfigs();

                sender.sendMessage(ChatColor.GOLD + "setMinPlayers to " + args[1]);
            } else {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Too late to change player min count ");
            }
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("setMap")) {
            Map = args[1];
            player.sendMessage(ChatColor.GOLD + "Assuming map " + Map);

        } else if (args.length == 1 && args[0].equalsIgnoreCase("setlobby")) {

            Location loc = player.getLocation();
            String world = loc.getWorld().getName();
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            float yaw = loc.getYaw();
            float pitch = loc.getPitch();
            String location = world + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;
            config.addDefault("Lobby", location);
            config.options().copyDefaults(true);

            plugin.saveConfig();

            if (this.plugin.getGameState().equals(GameState.LOBBY)) {
                plugin.loadConfigs();
            } else {
                player.sendMessage(ChatColor.GOLD + "Game is active so no config reload done");
            }

            if (Map == null) {
                player.sendMessage(ChatColor.GOLD + "Map not set, assuming map name " + world);
                Map = world;
            }

            if (Map != null) {
                plugin.SaveDBCOnfig(Map, "Lobby", location);
                player.sendMessage(ChatColor.GOLD + "Lobby location has been successfully set for map " + Map);
            }

            return true;

        } else if (args.length == 2 && args[0].equalsIgnoreCase("setspawn")) {

            if (args[1].equalsIgnoreCase("ct") || args[1].equalsIgnoreCase("counterterrorist")) {
                Location loc = player.getLocation();
                String world = loc.getWorld().getName();
                double x = loc.getX();
                double y = loc.getY();
                double z = loc.getZ();
                float yaw = loc.getYaw();
                float pitch = loc.getPitch();
                String location = world + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;
                config.addDefault("spawn-locations.counterterrorist", location);
                plugin.saveConfig();

                if (this.plugin.getGameState().equals(GameState.LOBBY)) {
                    plugin.loadConfigs();
                } else {
                    player.sendMessage(ChatColor.GOLD + "Game is active so no config reload done");
                }

                if (Map == null) {
                    player.sendMessage(ChatColor.GOLD + "Map not set, assuming map name " + world);
                    Map = world;
                }

                if (Map != null) {
                    plugin.SaveDBCOnfig(Map, "Counter", location);
                    player.sendMessage(ChatColor.GOLD + "Counter Terrorist spawn has been successfully set for map " + Map);
                }

            } else if (args[1].equalsIgnoreCase("t") || args[1].equalsIgnoreCase("terrorist")) {
                Location loc = player.getLocation();
                String world = loc.getWorld().getName();
                double x = loc.getX();
                double y = loc.getY();
                double z = loc.getZ();
                float yaw = loc.getYaw();
                float pitch = loc.getPitch();
                String location = world + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;
                config.addDefault("spawn-locations.terrorist", location);
                plugin.saveConfig();

                if (this.plugin.getGameState().equals(GameState.LOBBY)) {
                    plugin.loadConfigs();
                } else {
                    player.sendMessage(ChatColor.GOLD + "Game is active so no config reload done");
                }

                if (Map == null) {
                    player.sendMessage(ChatColor.GOLD + "Map not set, assuming map name " + world);
                    Map = world;
                }

                if (Map != null) {
                    plugin.SaveDBCOnfig(Map, "Terrorist", location);
                    player.sendMessage(ChatColor.GOLD + "Terrorist spawn has been successfully set for map " + Map);
                }

            } else {
                player.sendMessage(ChatColor.RED + "/counterstrike setspawn <counterterrorist/terrorist>");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("setbombsite")) {

            String bombSiteName = args[1];

            if (!bombSiteName.equals("A") && !bombSiteName.equals("B")) {
                sender.sendMessage("Bom site name needs to be A or B.");
                return true;
            }

            Location loc = player.getLocation();
            String world = loc.getWorld().getName();
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            float yaw = loc.getYaw();
            float pitch = loc.getPitch();
            String location = world + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;

            config.addDefault("bomb-locations." + bombSiteName, location);
            plugin.saveConfig();

            if (this.plugin.getGameState().equals(GameState.LOBBY)) {
                plugin.loadConfigs();
            } else {
                player.sendMessage(ChatColor.GOLD + "Game is active so no config reload done");
            }

            if (Map == null) {
                player.sendMessage(ChatColor.GOLD + "Map not set, assuming map name " + world);
                Map = world;
            }

            if (Map != null) {
                plugin.SaveDBCOnfig(Map, bombSiteName, location);
                player.sendMessage(ChatColor.GOLD + bombSiteName + " bomb site has been successfully set for map " + Map);
            }

        } else {
            sender.sendMessage(ChatColor.GREEN + "/counterstrike setMap - " + ChatColor.GRAY + "Sets the current map been setup with next commeads.");
            sender.sendMessage(ChatColor.GREEN + "/counterstrike setlobby - " + ChatColor.GRAY + "Sets the spawn point for when the game is in lobby state.");
            sender.sendMessage(ChatColor.GREEN + "/counterstrike setspawn <counterterrorist/terrorist> - " + ChatColor.GRAY + "Sets the spawn point for each team for when the game has started.");
            sender.sendMessage(ChatColor.GREEN + "/counterstrike setbombsite <A/B> - " + ChatColor.GRAY + "Sets the bomb site for A or B (Required for AI).");
        }
        return true;
    }

}
