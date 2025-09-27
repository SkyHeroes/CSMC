package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.database.Worlds;
import dev.danablend.counterstrike.enums.GameState;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;

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
        String resPackName = player.getName() + "RES";

        if (plugin.HashWorlds != null) {
            Worlds md = (Worlds) plugin.HashWorlds.get(world);

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

        if (success && !plugin.myBukkit.isDownloded(event)) {
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

        player.setGameMode(GameMode.SURVIVAL);

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
            CSPlayer csplayer = plugin.getCSPlayer(player, false, null);

            if (csplayer != null) {
                Utils.debug("#### Returning Player " + player.getName() + " to map");
                csplayer.setPlayer(player);
                plugin.returnPlayertoGame(csplayer);
            } else {
                Utils.debug("#### Player " + player.getName() + " entered the lobby");
                plugin.myBukkit.playerTeleport(player, plugin.getLobbyLocation());

                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
            }
        }

    }


    @EventHandler(ignoreCancelled = true)
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String world = player.getWorld().getName();
        Worlds md = (Worlds) plugin.HashWorlds.get(world);

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

        if (plugin.HashWorlds != null) {
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
        }

    }


    @EventHandler(ignoreCancelled = true)
    public void ServerListMotd(final ServerListPingEvent event) {

        if (plugin.getGameState().equals(GameState.LOBBY)) {

            if (plugin.VoteHash.size() > 0 && plugin.getServer().getOnlinePlayers().size() > 0) {
                event.setMotd(ChatColor.AQUA + "CSMC Game has map been voted..");
            }
        } else if (plugin.getGameState().equals(GameState.WAITING)) {

            if (plugin.VoteHash.size() > 0) {
                event.setMotd(ChatColor.AQUA + "CSMC Game has map been voted...");
            } else {
                event.setMotd(ChatColor.AQUA + "CSMC Game is waiting for more players...");
            }

        } else if (plugin.getGameState().equals(GameState.STARTING)) {
            event.setMotd(ChatColor.YELLOW + "CSMC Game is starting...");
        } else if (plugin.getGameState().equals(GameState.RUN)) {
            event.setMotd(ChatColor.GREEN + "CSMC Game is running!");
        }

    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoinGame(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        String world = player.getWorld().getName();

        if (plugin.HashWorlds != null) {
            Worlds md = (Worlds) plugin.HashWorlds.get(world);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        int maxPlayers = MAX_PLAYERS;

        if (maxPlayers > 16) {
            maxPlayers = 16;
        }

        CSPlayer csplayer = plugin.getCSPlayer(player, false, null);

        if (plugin.getCSPlayers().size() > maxPlayers && csplayer == null) {
            PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "We are sorry", ChatColor.GREEN + "The game is full, please try again later.", 1, 4, 1);
            return;
        }

        if (csplayer == null && event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {

            if (!plugin.allowJoinRunningGame && (plugin.getGameState().equals(GameState.SHOP) || plugin.getGameState().equals(GameState.RUN) || plugin.getGameState().equals(GameState.PLANTED))) {
                dev.danablend.counterstrike.csplayer.Team myTeam = plugin.getTerroristsTeam();

                int remainTime;

                if (CounterStrike.i.getGameTimer() == null) {
                    remainTime = Config.MATCH_DURATION;
                } else {
                    remainTime = CounterStrike.i.getGameTimer().returnTimetoEnd();
                }

                PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "Wait for the end of the current round to join", ChatColor.GREEN + "Current round: " + (myTeam.getLosses() + myTeam.getWins() + 1) + " of " + MAX_ROUNDS + ". Estimated time for new " + remainTime + "secs", 1, 4, 1);
                return;
            }

            Block blockUnder = event.getClickedBlock();

            String materialColour = blockUnder.getBlockData().getMaterial().toString();

            if (materialColour.contains("CYAN") || materialColour.contains("BLUE")) {
                materialColour = "BLUE";
            } else if (materialColour.contains("RED") || materialColour.contains("PINK")) {
                materialColour = "RED";
            } else if (materialColour.contains("GREEN") || materialColour.contains("LIME")) {
                materialColour = "GREEN";
            } else if (materialColour.contains("YELLOW")) {
                materialColour = "YELLOW";

            } else if (materialColour.contains("GOLD")) {
                materialColour = "GOLD";
            } else if (materialColour.contains("AQUA")) {
                materialColour = "AQUA";

            } else {
                player.sendMessage("You have to choose one of the floors with colour");
                return;
            }

            csplayer = plugin.getCSPlayer(player, true, materialColour);

            if (!csplayer.returStatus()) {
                player.sendMessage("You have to choose one of the active colours/team in order to join, try  " + plugin.getTerroristsTeam().getColour() + "  or  " + plugin.getCounterTerroristsTeam().getColour());
                csplayer.clear();
                return;
            }

            String corAdversaria;

            if (csplayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
                corAdversaria = plugin.getTerroristsTeam().getColour();
            } else {
                corAdversaria = plugin.getCounterTerroristsTeam().getColour();
            }

            csplayer.setColourOpponent(corAdversaria);

            if ((plugin.getGameState().equals(GameState.LOBBY) || plugin.getGameState().equals(GameState.WAITING) || plugin.getGameState().equals(GameState.STARTING))) {
                plugin.StartGameCounter(0);
            } else {
                PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "Waiting for next round to join", ChatColor.GREEN + " please wait", 1, 10, 1);

                //already runnig, will join next round
                plugin.myBukkit.runTask(player, null, null, () -> {
                    plugin.myBukkit.playerTeleport(player, plugin.bombSiteA());
                    player.setGameMode(GameMode.SPECTATOR);
                });
            }

        }
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMapVote(PlayerInteractEvent e) {
        if (!plugin.activated) return;

        Player player = e.getPlayer();
        String world = player.getWorld().getName();

        if (plugin.HashWorlds != null) {
            Object obj = plugin.HashWorlds.get(world);

            if (obj != null) {
                Worlds md = (Worlds) obj;

                if (md != null && !md.modoCs) { //must not work here
                    return;
                }
            }
        }

        if ((plugin.getGameState().equals(GameState.LOBBY) || plugin.getGameState().equals(GameState.WAITING) || plugin.getGameState().equals(GameState.STARTING))) {

            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

                if (e.getClickedBlock().getState() instanceof Sign) {

                    if (plugin.getGameState().equals(GameState.STARTING)) {
                        player.sendMessage("Game is already starting");
                        e.setCancelled(true);
                        return;
                    }

                    Sign s = (Sign) e.getClickedBlock().getState();

                    if (s != null && s.getLine(0) != null && (s.getLine(0).equalsIgnoreCase("[CSGo]") || s.getLine(0).equalsIgnoreCase("[CSMC]"))) {

                        if (s.getLine(1).equalsIgnoreCase("Vote for")) {
                            plugin.VoteHash.put(player.getName(), Integer.parseInt(s.getLine(3)));
                            player.sendMessage("Vote registered for map " + s.getLine(2));
                            plugin.Map = "Under voting";

                        } else if (s.getLine(1) == null || (s.getLine(0).equalsIgnoreCase("Join Lobby"))) {
                            plugin.myBukkit.playerTeleport(player, plugin.getLobbyLocation());
                            player.sendMessage("Joined Lobby");
                        }
                    }

                    e.setCancelled(true);
                }
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onSignChangeEvent(SignChangeEvent e) {

        Player player = e.getPlayer();
        String world = player.getWorld().getName();

        if (plugin.HashWorlds != null) {
            Object obj = plugin.HashWorlds.get(world);

            if (obj != null) {
                Worlds md = (Worlds) obj;

                if (md != null && !md.modoCs) { //must not work here
                    return;
                }
            }
        }

        Sign s = (Sign) e.getBlock().getState();

        if (s != null && s.getLine(0) != null && (s.getLine(0).equalsIgnoreCase("[CSGo]") || s.getLine(0).equalsIgnoreCase("[CSMC]"))) {
            s.setEditable(true);
            s.setLine(0, ChatColor.BLUE + s.getLine(0));
            s.setLine(1, ChatColor.GREEN + s.getLine(1));
            //s.setLine(2,ChatColor.GREEN + "0/" + MAX_PLAYERS);
            s.setEditable(false);
        }

        e.setCancelled(true);
    }


}
