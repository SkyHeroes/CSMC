package dev.danablend.counterstrike.commands;

import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.utils.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.shop.Shop;

import java.awt.*;

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
        if (args.length == 4 && args[0].equalsIgnoreCase("money")) {
            if (args[1].equalsIgnoreCase("give")) {
                CSPlayer playerToGiveMoneyTo = CounterStrike.i.getCSPlayer(Bukkit.getPlayer(args[2]), false, null);

                if (playerToGiveMoneyTo == null) {
                    return false;
                }

                playerToGiveMoneyTo.setMoney(playerToGiveMoneyTo.getMoney() + Integer.parseInt(args[3]));
                sender.sendMessage("Added " + Integer.parseInt(args[3]) + ". New balance is " + playerToGiveMoneyTo.getMoney());
                return true;
            }
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("shopt")) {
            Shop.getShop().openTerroristShop((Player) sender);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("shopct")) {
            Shop.getShop().openCounterTerroristShop((Player) sender);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("leave")) {

            if (this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING) || this.plugin.getGameState().equals(GameState.STARTING)) {
                CSPlayer csplayer = CounterStrike.i.getCSPlayer((Player) sender, false, null);

                if (csplayer != null) {
                    csplayer.clear();
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "You have left the game");
                }
            } else {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Too late to leave game");
            }

            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("Ok")) {

            if (this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING) || this.plugin.getGameState().equals(GameState.STARTING)) {
                CSPlayer csplayer = CounterStrike.i.getCSPlayer((Player) sender, false, null);

                if (csplayer != null) {

                    JSONMessage.create("Overpass")
                            .color(ChatColor.GREEN)
                            .tooltip("Click to select map")
                            .runCommand("/csmc LoadMap Overpass")

                            .then(" or ")
                            .color(ChatColor.GRAY)
                            .style(ChatColor.BOLD)
                            .then("Dust2")
                            .color(ChatColor.LIGHT_PURPLE)
                            .tooltip("Click to select map")
                            .runCommand("/csmc LoadMap Dust2")

                            .then(" or ")
                            .color(ChatColor.GRAY)
                            .style(ChatColor.BOLD)
                            .then("Mirage")
                            .color(ChatColor.AQUA)
                            .tooltip("Click to join TR")
                            .runCommand("/csmc LoadMap Mirage")

                            .then(" or ")
                            .color(ChatColor.GRAY)
                            .style(ChatColor.BOLD)
                            .then("Nuke")
                            .color(ChatColor.GOLD)
                            .tooltip("Click to join TR")
                            .runCommand("/csmc LoadMap Nuke")
                            .send((Player) sender);
                }
            }

            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("LoadMap")) {

            if (this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING) || this.plugin.getGameState().equals(GameState.STARTING)) {
                this.plugin.Map = args[1];
            } else {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Too late to change map ");
            }
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("setMap")) {
            Map = args[1];
            System.out.println("Mapa " + Map);
            sender.sendMessage("Map set to " + Map);
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
            config.set("Lobby", location);
            plugin.saveConfig();
            plugin.LoadCOnfigs();

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
                config.set("spawn-locations.counterterrorist", location);
                plugin.saveConfig();
                plugin.LoadCOnfigs();

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
                config.set("spawn-locations.terrorist", location);
                plugin.saveConfig();
                plugin.LoadCOnfigs();

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
