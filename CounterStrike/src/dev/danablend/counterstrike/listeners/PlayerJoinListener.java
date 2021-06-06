package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.database.Mundos;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.server.ServerListPingEvent;

import static dev.danablend.counterstrike.Config.MAX_PLAYERS;
import static org.bukkit.event.player.PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED;


public class PlayerJoinListener implements Listener {

    CounterStrike plugin;
    FileConfiguration config;

    public PlayerJoinListener() {
        this.plugin = CounterStrike.i;
        this.config = plugin.getConfig();
    }

    @EventHandler
    public void onPlayerResourcePackStatusEvent(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();

        String mundo = event.getPlayer().getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        if (plugin.ResourseHash.get(player.getName()) == null && event.getStatus().equals(SUCCESSFULLY_LOADED)) {
            plugin.ResourseHash.put(player.getName(), true);
        }
    }


    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        String mundo = player.getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Object obj = plugin.HashWorlds.get(mundo);

            //if has generalP loaded
            if (obj != null) {
                Mundos md = (Mundos) obj;

                if (md != null && !md.modoCs) {
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                    player.setFoodLevel(20);
                    player.setHealth(20);
                    return;
                }
            }
        }

        System.out.println("#### Player " + player.getName() + " entered ");

        if (plugin.getCSPlayers().size() >= MAX_PLAYERS) {
            PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "Welcome to CSMC World", ChatColor.RED + "The game is full, please try again later.", 1, 4, 1);
            return;
        }

        PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "Welcome to CSMC World", ChatColor.RED + "Left click to join game.", 1, 4, 1);

        if (config.contains("lobby-location")) {
            Location lobbyLoc = plugin.getLobbyLocation();
            player.setFallDistance(1);
            player.teleport(lobbyLoc);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setFoodLevel(8); //era 6
            //player.setExhaustion(20);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }
    }


    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {

        String mundo = event.getPlayer().getWorld().getName();
        Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

        if (md != null && !md.modoCs) {
            return;
        }

        System.out.println("#### Player " + event.getPlayer().getName() + " left ");

        CSPlayer csplayer = plugin.getCSPlayer(event.getPlayer(), false, null);

        if (csplayer != null) {
            plugin.getPlayerUpdater().deleteScoreBoards(event.getPlayer());
            csplayer.clear();
        }

        //Clears cache resoursepack
        if (plugin.ResourseHash.get(event.getPlayer().getName()) != null) {
            plugin.ResourseHash.remove(event.getPlayer().getName());
        }
    }


    @EventHandler
    public void playerChangedWorldEvent(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String mundo = player.getWorld().getName();
        Mundos md = null;

        if (CounterStrike.i.HashWorlds != null) {
            Object obj = plugin.HashWorlds.get(mundo);

            //if has generalP loaded
            if (obj == null) {
                return; //no support without a master plugin
            } else {
                md = (Mundos) obj;
            }
        }

        System.out.println("#### Player " + player.getName() + " changes world from " + event.getFrom().getName() + " to " + mundo);

        if (md != null && !md.modoCs) { //Leaving CSMC map

            Object md_old = plugin.HashWorlds.get(event.getFrom().getName());

            if (md_old != null && ((Mundos) md_old).modoCs) {

                CSPlayer csplayer = plugin.getCSPlayer(event.getPlayer(), false, null);

                if (csplayer != null) {
                    plugin.getPlayerUpdater().deleteScoreBoards(event.getPlayer());
                    csplayer.clear();
                }

                //clears cache resoursepack
                if (plugin.ResourseHash.get(event.getPlayer().getName()) != null) {
                    plugin.ResourseHash.remove(event.getPlayer().getName());
                    player.setResourcePack("www.google.com");  //fast unload
                }

                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                player.setFoodLevel(20);
                player.setHealth(20);

                PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "Was nice too see you", ChatColor.GREEN + "Hope to see you soon @CSMC World.", 1, 4, 1);
            }
            return;
        }

        if (plugin.getCSPlayers().size() >= MAX_PLAYERS) {
            PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "Welcome to CSMC World", ChatColor.GREEN + "The game is full, please try again later.", 1, 4, 1);
            return;
        }

        PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "Welcome to CSMC World", ChatColor.GREEN + "Left click to join game.", 1, 4, 1);

        if (config.contains("lobby-location")) {
            Location lobbyLoc = plugin.getLobbyLocation();
            player.teleport(lobbyLoc);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setFoodLevel(8); //era 6
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }

    }


    @EventHandler
    public void ServerListMotd(final ServerListPingEvent event){

        if (plugin.gameState.equals(GameState.WAITING)) {
            event.setMotd(ChatColor.AQUA +"CSMC Game is waiting for more players... ");
        } else if (plugin.gameState.equals(GameState.STARTING)) {
            event.setMotd(ChatColor.AQUA +"CSMC Game is starting... ");
        } else if (plugin.gameState.equals(GameState.RUN)) {
            event.setMotd(ChatColor.AQUA + "CSMC Game is running, next round in " + plugin.getGameTimer().returnTimetoEnd() + " secs");
        }

    }
}
