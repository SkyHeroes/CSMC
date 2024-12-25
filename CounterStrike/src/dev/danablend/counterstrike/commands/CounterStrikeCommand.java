package dev.danablend.counterstrike.commands;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
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

        if (args.length == 1 && args[0].equalsIgnoreCase("setRandMap")) {

            if (this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING) || this.plugin.getGameState().equals(GameState.STARTING)) {
                this.plugin.LoadDBRandomMaps();
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

        if (args.length == 1 && args[0].equalsIgnoreCase("setlobby")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You need to be a player to execute this command.");
                return true;
            }
            Player player = (Player) sender;
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
            plugin.loadConfigs();

            if (Map == null) {
                player.sendMessage(ChatColor.GOLD + "Map not set, assuming " + world);
                Map = world;
            }

            if (Map != null) {
                plugin.SaveDBCOnfig(Map, "Lobby", location);
                player.sendMessage(ChatColor.GOLD + "Lobby location has been successfully set.");
            }

            return true;

        } else if (args.length == 2 && args[0].equalsIgnoreCase("setspawn")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You need to be a player to execute this command.");
                return true;
            }
            Player player = (Player) sender;
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
                plugin.loadConfigs();

                if (Map == null) {
                    player.sendMessage(ChatColor.GOLD + "Map not set, assuming " + world);
                    Map = world;
                }

                if (Map != null) {
                    plugin.SaveDBCOnfig(Map, "Counter", location);
                    player.sendMessage(ChatColor.GOLD + "Counter Terrorist spawn has been successfully set.");
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
                plugin.loadConfigs();

                if (Map == null) {
                    player.sendMessage(ChatColor.GOLD + "Map not set, assuming " + world);
                    Map = world;
                }

                if (Map != null) {
                    plugin.SaveDBCOnfig(Map, "Terrorist", location);
                    player.sendMessage(ChatColor.GOLD + "Terrorist spawn has been successfully set.");
                }

            } else {
                player.sendMessage(ChatColor.RED + "/counterstrike setspawn <counterterrorist/terrorist>");
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "/counterstrike setlobby - " + ChatColor.GRAY + "Sets the spawn point for when the game is in lobby state.");
            sender.sendMessage(ChatColor.GREEN + "/counterstrike setspawn <counterterrorist/terrorist> - " + ChatColor.GRAY + "Sets the spawn point for each team for when the game has started.");
        }
        return true;
    }

}
