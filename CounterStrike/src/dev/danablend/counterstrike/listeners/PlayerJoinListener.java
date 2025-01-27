package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.database.Worlds;
import dev.danablend.counterstrike.enums.GameState;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;

import static dev.danablend.counterstrike.Config.*;
import static org.bukkit.event.player.PlayerResourcePackStatusEvent.Status.*;

public class PlayerJoinListener implements Listener {

    CounterStrike plugin;
    FileConfiguration config;

    public PlayerJoinListener() {
        this.plugin = CounterStrike.i;
        this.config = plugin.getConfig();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerResourcePackStatusEvent(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        String world = player.getWorld().getName();
        String resPackName = player.getName() + "RES";

        if (CounterStrike.i.HashWorlds != null) {
            Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

            if (md != null && !md.modoCs) {
                if (plugin.ResourceHash.get(resPackName) == null) {
                    plugin.ResourceHash.put(resPackName, "DEFAULT");
                }
            }
        }

        boolean success = true;

        if (!event.getStatus().equals(ACCEPTED) && !event.getStatus().equals(SUCCESSFULLY_LOADED)) {
            Utils.debug(" ResourcePack load status: " + event.getStatus());
            success = false;
        }

        if (success && !CounterStrike.i.myBukkit.isDownloded(event)) {
            Utils.debug(" ResourcePack load status: " + event.getStatus());
            success = false;
        }

        if (!success) {
            //goes back in loaded resource pack cache status
            if (plugin.ResourceHash.get(resPackName) == null || plugin.ResourceHash.get(resPackName).equals("DEFAULT")) {
                plugin.ResourceHash.put(resPackName, "QUALITY");
            } else {
                plugin.ResourceHash.put(resPackName, "DEFAULT");
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String world = player.getWorld().getName();

        if (plugin.HashWorlds != null) {
            Object obj = plugin.HashWorlds.get(world);

            if (obj != null) {
                Worlds md = (Worlds) obj;

                if (md != null && !md.modoCs) {
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                    player.setFoodLevel(20);
                    player.setHealth(20);
                    return;
                }
            }
        }

        if (plugin.getLobbyLocation() != null) {
            player.setFallDistance(1);
            player.setGameMode(GameMode.SURVIVAL);
            player.setFoodLevel(8); //was 6
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }

        if (!plugin.getPlayerUpdater().playersWithScoreboard.contains(player.getUniqueId()) && (CounterStrike.i.getGameState().equals(GameState.LOBBY) || CounterStrike.i.getGameState().equals(GameState.WAITING))) {
            Utils.debug("#### Player " + player.getName() + " entered the lobby");
            plugin.myBukkit.playerTeleport(player, plugin.getLobbyLocation());

            if (plugin.getCSPlayers().size() >= MAX_PLAYERS) {
                PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "Welcome to CSMC World", ChatColor.RED + "The game is full, please try again later.", 1, 4, 1);
            }

            player.getInventory().clear();
            player.getInventory().setArmorContents(null);

            PacketUtils.sendTitleAndSubtitle(player, ChatColor.GOLD + "Welcome to CSMC World", ChatColor.RED + "Left click to join game.", 1, 4, 1);

        } else {
            Utils.debug("#### Returning Player " + player.getName() + " to map");
            CSPlayer csplayer = plugin.getCSPlayer(player, false, null);

            if (csplayer != null) {
                csplayer.setPlayer(player);
                plugin.returnPlayertoGame(csplayer);
            }
        }

    }


    @EventHandler(ignoreCancelled = true)
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String world = player.getWorld().getName();
        Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

        plugin.ResourceHash.remove(player.getName() + "RES");

        if (md != null && !md.modoCs) {
            return;
        }

        Utils.debug("#### Player " + player.getName() + " left ");


        CSPlayer csplayer = plugin.getCSPlayer(player, false, null);

        plugin.leaveGame(csplayer);
    }


    @EventHandler(ignoreCancelled = true)
    public void playerChangedWorldEvent(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String world = player.getWorld().getName();
        Worlds md = null;
        String resPackName = player.getName() + "RES";

        if (CounterStrike.i.HashWorlds != null) {
            Object obj = plugin.HashWorlds.get(world);

            if (obj == null) {
                return; //no support without a map mapping
            } else {
                md = (Worlds) obj;
            }
        }

        Utils.debug("#### Player " + player.getName() + " changed world from " + event.getFrom().getName() + " to " + world);

        if (md != null && !md.modoCs) { //Leaving CSMC map

            Object md_old = plugin.HashWorlds.get(event.getFrom().getName());

            if (md_old != null && ((Worlds) md_old).modoCs) {

                CSPlayer csplayer = plugin.getCSPlayer(player, false, null);

                if (csplayer != null) {
                    plugin.getPlayerUpdater().deleteScoreBoards(player);
                    csplayer.clear();
                }

                //clears QUALITY resoursepack and loads default (inOnline filters NPCs)
                if (player.isOnline() && (plugin.ResourceHash.get(resPackName) == null || plugin.ResourceHash.get(resPackName).equals("QUALITY"))) {
                    plugin.ResourceHash.put(resPackName, "DEFAULT");

                    plugin.loadResourcePack(player, DEFAULT_RESOURCE, DEFAULT_RESOURCE_HASH);
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

        if (plugin.getLobbyLocation() != null) {
            Location lobbyLoc = plugin.getLobbyLocation();
            plugin.myBukkit.playerTeleport(player, lobbyLoc);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setFoodLevel(8); //was 6
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }

    }


    @EventHandler(ignoreCancelled = true)
    public void ServerListMotd(final ServerListPingEvent event) {

        if (plugin.getGameState().equals(GameState.WAITING)) {
            event.setMotd(ChatColor.AQUA + "CSMC Game is waiting for more players... ");
        } else if (plugin.getGameState().equals(GameState.STARTING)) {
            event.setMotd(ChatColor.AQUA + "CSMC Game is starting... ");
        } else if (plugin.getGameState().equals(GameState.RUN)) {
            event.setMotd(ChatColor.AQUA + "CSMC Game is running, next round in " + plugin.getGameTimer().returnTimetoEnd() + " secs");
        }

    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoinLobby(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getState() instanceof Sign) {
                Sign s = (Sign) e.getClickedBlock().getState();

                if (s != null && s.getLine(0) != null && (s.getLine(0).equalsIgnoreCase("[CSGo]") || s.getLine(0).equalsIgnoreCase("[CSMC]"))) {
                    plugin.myBukkit.playerTeleport(player, plugin.getLobbyLocation());
                }
            }
        }

    }


    @EventHandler(ignoreCancelled = true)
    public void onSignChangeEvent(SignChangeEvent e) {
        Player player = e.getPlayer();

        Sign s = (Sign) e.getBlock().getState();

        if (s != null && s.getLine(0) != null && (s.getLine(0).equalsIgnoreCase("[CSGo]") || s.getLine(0).equalsIgnoreCase("[CSMC]"))) {
            s.setEditable(true);
            s.setLine(0, ChatColor.BLUE + s.getLine(0));
            s.setLine(1, ChatColor.GREEN + s.getLine(1));
            //s.setLine(2,ChatColor.GREEN + "0/" + MAX_PLAYERS);
            s.setEditable(false);
        }

    }


}
