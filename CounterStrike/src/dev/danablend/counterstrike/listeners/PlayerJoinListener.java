package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.database.Worlds;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;

import static dev.danablend.counterstrike.Config.*;
import static org.bukkit.event.player.PlayerResourcePackStatusEvent.Status.ACCEPTED;
import static org.bukkit.event.player.PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED;

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

        if (CounterStrike.i.HashWorlds != null) {
            Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

            if (md != null && !md.modoCs) {
                if (plugin.ResourseHash.get(player.getName() + "RES") == null) {
                    plugin.ResourseHash.put(player.getName() + "RES", "DEFAULT");
                }
            }
        }

        if (!event.getStatus().equals(ACCEPTED) && !event.getStatus().equals(SUCCESSFULLY_LOADED)) {
        //    Utils.debug("Loading resource status " + event.getStatus());

            //goes back in loaded resource pack
            if (plugin.ResourseHash.get(player.getName() + "RES") == null || plugin.ResourseHash.get(player.getName() + "RES") == "DEFAULT") {
                plugin.ResourseHash.remove(player.getName() + "RES");
                plugin.ResourseHash.put(player.getName() + "RES", "QUALITY");
            } else {
                plugin.ResourseHash.remove(player.getName() + "RES");
                plugin.ResourseHash.put(player.getName() + "RES", "DEFAULT");
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

        if (!plugin.getPlayerUpdater().playersWithScoreboard.contains(player.getUniqueId())) {
            Utils.debug("#### Player " + player.getName() + " entered the lobby");
           plugin.myBukkit.playerTeleport(player,plugin.getLobbyLocation());

            if (plugin.getCSPlayers().size() >= MAX_PLAYERS) {
                PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "Welcome to CSMC World", ChatColor.RED + "The game is full, please try again later.", 1, 4, 1);
            }

            player.getInventory().clear();
            player.getInventory().setArmorContents(null);

            PacketUtils.sendTitleAndSubtitle(player, ChatColor.GOLD + "Welcome to CSMC World", ChatColor.RED + "Left click to join game.", 1, 4, 1);

        } else {
            Utils.debug("#### Returning Player " + player.getName() + " to map");
            CSPlayer csplayer = plugin.getCSPlayer(player, false, null);
            csplayer.setPlayer(player);
            plugin.returnPlayertoGame(csplayer);
        }

    }


    @EventHandler(ignoreCancelled = true)
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String world = player.getWorld().getName();
        Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

        plugin.ResourseHash.remove(player.getName() + "RES");

        if (md != null && !md.modoCs) {
            return;
        }

        Utils.debug("#### Player " + player.getName() + " left ");

        CSPlayer csplayer = plugin.getCSPlayer(player, false, null);

        if (plugin.quitExitGame) {
            if (csplayer != null) {
                plugin.getPlayerUpdater().deleteScoreBoards(player);
                csplayer.clear();
            }
        } else {
            //if has bomb drops it
            if (csplayer != null && csplayer.getBomb() != null) {

                ItemStack item = player.getInventory().getItem(4);

                if (item != null) {
                    //Utils.debug("Dropping bomb ");
                    player.getInventory().remove(item);
                    Item itemDropped = player.getWorld().dropItemNaturally(player.getLocation(), item);
                    itemDropped.setPickupDelay(40);
                }
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void playerChangedWorldEvent(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String world = player.getWorld().getName();
        Worlds md = null;

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

                CSPlayer csplayer = plugin.getCSPlayer(event.getPlayer(), false, null);

                if (csplayer != null) {
                    plugin.getPlayerUpdater().deleteScoreBoards(event.getPlayer());
                    csplayer.clear();
                }

                //clears QUALITY resoursepack and loads default
                if (plugin.ResourseHash.get(player.getName() + "RES") == null || plugin.ResourseHash.get(player.getName() + "RES") == "QUALITY") {
                    plugin.ResourseHash.remove(player.getName() + "RES");
                    plugin.ResourseHash.put(player.getName() + "RES", "DEFAULT");

                    plugin.loadResourcePack(player, DEFAULT_RESOURCE, DEFAULT_RESOURCE_HASH);
                    //player.setResourcePack(DEFAULT_RESOURCE, DEFAULT_RESOURCE_HASH);  //fast unload
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
           plugin.myBukkit.playerTeleport(player,lobbyLoc);
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

        if (plugin.gameState.equals(GameState.WAITING)) {
            event.setMotd(ChatColor.AQUA + "CSMC Game is waiting for more players... ");
        } else if (plugin.gameState.equals(GameState.STARTING)) {
            event.setMotd(ChatColor.AQUA + "CSMC Game is starting... ");
        } else if (plugin.gameState.equals(GameState.RUN)) {
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
                    plugin.myBukkit.playerTeleport(player,plugin.getLobbyLocation());
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
