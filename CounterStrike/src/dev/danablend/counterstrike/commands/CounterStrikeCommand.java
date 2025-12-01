package dev.danablend.counterstrike.commands;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.enums.GameState;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static dev.danablend.counterstrike.Config.*;


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

        if (!player.hasPermission("cs.admin")) {
            player.sendMessage("You need permissions to execute this command.");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("setMap")) {
            Map = args[1];
            player.sendMessage(ChatColor.GOLD + "Assuming map " + Map + " continue with the lobby and spawn configs or run again to change name");

        } else if (args.length == 2 && args[0].equalsIgnoreCase("delMap")) {

            Map = args[1];
            int linhas = plugin.DeleteDBConfig(Map);
            player.sendMessage(ChatColor.GOLD + "Deleting map " + Map + " was completed with " + linhas + " lines afetcted");
            Map = null;

        } else if (args.length == 1 && args[0].equalsIgnoreCase("setlobby")) {

            if (Map == null) {
                player.sendMessage(ChatColor.GOLD + "Map not set, set it with setMap command");
                return true;
            }

            if (!(this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING))) {
                player.sendMessage(ChatColor.GOLD + "Game is active so no config done");
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

            plugin.SaveDBCOnfig(Map, "Lobby", location);
            player.sendMessage(ChatColor.GOLD + "Lobby location has been successfully set for map " + Map);

        } else if (args.length == 2 && args[0].equalsIgnoreCase("setspawn")) {

            if (Map == null) {
                player.sendMessage(ChatColor.GOLD + "Map not set, set it with setMap command");
                return true;
            }

            if (!(this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING))) {
                player.sendMessage(ChatColor.GOLD + "Game is active so no config done");
                return true;
            }

            if (args[1].equalsIgnoreCase("ct") || args[1].equalsIgnoreCase("counterterrorist")) {
                Location loc = player.getLocation();
                String world = loc.getWorld().getName();
                double x = loc.getX();
                double y = loc.getY();
                double z = loc.getZ();
                float yaw = loc.getYaw();
                float pitch = loc.getPitch();
                String location = world + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;

                plugin.SaveDBCOnfig(Map, "Counter", location);
                player.sendMessage(ChatColor.GOLD + "Counter Terrorist spawn has been successfully set for map " + Map);

            } else if (args[1].equalsIgnoreCase("t") || args[1].equalsIgnoreCase("terrorist")) {
                Location loc = player.getLocation();
                String world = loc.getWorld().getName();
                double x = loc.getX();
                double y = loc.getY();
                double z = loc.getZ();
                float yaw = loc.getYaw();
                float pitch = loc.getPitch();
                String location = world + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;

                plugin.SaveDBCOnfig(Map, "Terrorist", location);
                player.sendMessage(ChatColor.GOLD + "Terrorist spawn has been successfully set for map " + Map);

            } else {
                player.sendMessage(ChatColor.RED + "/counterstrike setspawn <counterterrorist/terrorist>");
            }

        } else if (args.length == 2 && args[0].equalsIgnoreCase("setbombsite")) {

            if (Map == null) {
                player.sendMessage(ChatColor.GOLD + "Map not set, set it with setMap command");
                return true;
            }

            if (!(this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING))) {
                player.sendMessage(ChatColor.GOLD + "Game is active so no config done");
                return true;
            }

            String bombSiteName = args[1];

            if (!bombSiteName.equals("A") && !bombSiteName.equals("B")) {
                player.sendMessage("Bom site name needs to be A or B.");
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

            plugin.SaveDBCOnfig(Map, bombSiteName, location);
            player.sendMessage(ChatColor.GOLD + bombSiteName + " bomb site has been successfully set for map " + Map);

        } else if (args.length == 2 && args[0].equalsIgnoreCase("setMinPlayers")) {

            if (this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING)) {
                config.addDefault("min-players", args[1]);
                MIN_PLAYERS = Integer.parseInt(args[1]);
                config.options().copyDefaults(true);

                plugin.saveConfig();
                plugin.loadConfigs();

                player.sendMessage(ChatColor.GOLD + "MinPlayers was set to " + args[1]);
            } else {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Too late to change player min count ");
            }

        } else if (args.length == 2 && args[0].equalsIgnoreCase("setMaxPlayers")) {

            if (this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING)) {
                config.addDefault("max-players", args[1]);
                MAX_PLAYERS = Integer.parseInt(args[1]);
                config.options().copyDefaults(true);

                plugin.saveConfig();
                plugin.loadConfigs();

                player.sendMessage(ChatColor.GOLD + "MaxPlayers was set to " + args[1]);
            } else {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Too late to change player max count ");
            }

        } else if (args.length == 2 && args[0].equalsIgnoreCase("setBombBlock")) {

            if (this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING)) {

                String block = args[1];

                if (Material.getMaterial(block) == null) {
                    player.sendMessage(ChatColor.GOLD + "Material " + args[1] + " can't be used to plant bombs");
                    return true;
                }

                config.addDefault("bomb-block", args[1]);
                BOMB_MATERIAL = Material.getMaterial(config.getString("bomb-block", "BEDROCK"));
                config.options().copyDefaults(true);

                plugin.saveConfig();
                plugin.loadConfigs();

                player.sendMessage(ChatColor.GOLD + "Material was set to " + args[1]);
            } else {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Too late to change player max count ");
            }

        } else if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {

            if (plugin.getGameState().equals(GameState.LOBBY) || plugin.getGameState().equals(GameState.WAITING) || plugin.getGameState().equals(GameState.STARTING) || plugin.getGameState().equals(GameState.SHOP)) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Game hasn't start yet");
            } else {
                plugin.getGameTimer().terminateTimer();
                plugin.FinishGame(CounterStrike.i.getTerroristsTeam(), CounterStrike.i.getCounterTerroristsTeam());
            }

        } else if (args.length == 1 && args[0].equalsIgnoreCase("setRandMap")) {

            if (this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING) || this.plugin.getGameState().equals(GameState.STARTING)) {
                this.plugin.LoadDBRandomMaps(0);
            } else {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Too late to change map " + this.plugin.getGameState());
            }

        } else if (args.length == 1 && args[0].equalsIgnoreCase("maintenance")) {

            this.plugin.Maintenance(player);

        } else {
            player.sendMessage(ChatColor.GREEN + "/counterstrike setMap - " + ChatColor.GRAY + "Sets the current map been setup with next commands.");
            player.sendMessage(ChatColor.GREEN + "/counterstrike setlobby - " + ChatColor.GRAY + "Sets the spawn point for when the game is in lobby state.");
            player.sendMessage(ChatColor.GREEN + "/counterstrike setspawn <counterterrorist/terrorist> - " + ChatColor.GRAY + "Sets the spawn point for each team for when the game has started.");
            player.sendMessage(ChatColor.GREEN + "/counterstrike setbombsite <A/B> - " + ChatColor.GRAY + "Sets the bomb site for A or B (Required for AI).");
            player.sendMessage(ChatColor.GREEN + "/counterstrike delMap - " + ChatColor.GRAY + "To delete Map configuration from BD (You don't need this if you just want to fix a spawn).");
            player.sendMessage(ChatColor.GREEN + "/counterstrike maintenance - " + ChatColor.GRAY + "Set Maps on maintenance mode, so that you can change them.");
            player.sendMessage(ChatColor.GOLD + "Other commands setMinPlayers, setMaxPlayers, setRandMap, stop, setBombBlock.");
        }

        return true;
    }

}
