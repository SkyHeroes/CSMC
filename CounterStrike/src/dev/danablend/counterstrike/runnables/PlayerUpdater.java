package dev.danablend.counterstrike.runnables;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.utils.CSUtil;
import dev.danablend.counterstrike.utils.PacketUtils;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static dev.danablend.counterstrike.Config.MAX_ROUNDS;

public class PlayerUpdater extends BukkitRunnable {

    public Collection<UUID> playersWithScoreboard;
    CounterStrike plugin;
    boolean isFolia;

    public PlayerUpdater(CounterStrike plugin) {
        this.plugin = plugin;
        this.playersWithScoreboard = new ArrayList<>();
        isFolia = this.plugin.myBukkit.isFolia();
    }


    public void run() {

        if (!Config.GAME_ENABLED) {
            this.cancel();
            return;
        }

        for (CSPlayer csplayer : plugin.getCSPlayers()) {
            csplayer.update();

            if (!playersWithScoreboard.contains(csplayer.getPlayer().getUniqueId())) {
                setScoreBoard(csplayer);
                playersWithScoreboard.add(csplayer.getPlayer().getUniqueId());
            }
            updateScoreBoard(csplayer);
        }

        for (CSPlayer csplayer : plugin.getCSPlayers()) {
            Player player = csplayer.getPlayer();

            if (CSUtil.isOutOfShopZone(player) || plugin.getGameState() != GameState.SHOP) {
                player.getInventory().remove(CounterStrike.i.getShopItem());
            } else {
                if (player.getInventory().getItem(8) == null)
                    player.getInventory().setItem(8, CounterStrike.i.getShopItem());
            }

            if (player.getGameMode().equals(GameMode.SPECTATOR)) {
                if (player.getSpectatorTarget() != null) {
                    PacketUtils.sendActionBar(player, Color.YELLOW + "Spectating: " + ChatColor.GREEN + player.getSpectatorTarget().getName());
                }
            }
        }

    }


    public void setScoreBoard(CSPlayer csplayer) {
        Player player = csplayer.getPlayer();

        if (!player.isOnline()) return;

        //inits player colour
        player.setPlayerListName(ChatColor.valueOf(csplayer.getColour()) + player.getName());

        if (isFolia) {
            FastBoard board = new FastBoard(player);
            board.updateTitle(ChatColor.BOLD + "----Miner Strike v" + CounterStrike.i.getDescription().getVersion() + "----");
            csplayer.setBoard(board);

        } else {
            //CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

            Objective obj = board.registerNewObjective("counterStrike", "dummy", "counterStrike");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.setDisplayName(ChatColor.BOLD + "----Miner Strike v" + CounterStrike.i.getDescription().getVersion() + "----");

            dev.danablend.counterstrike.csplayer.Team myTeam;

            if (csplayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
                myTeam = CounterStrike.i.getCounterTerroristsTeam();
            } else {
                myTeam = CounterStrike.i.getTerroristsTeam();
            }

            // Teams to hide name tags
            Team t = board.registerNewTeam("t");
            Team ct = board.registerNewTeam("ct");
            t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
            ct.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);

            for (CSPlayer p : CounterStrike.i.getTerroristsTeam().getCsPlayers()) {
                t.addEntry(p.getPlayer().getName());
            }
            for (CSPlayer p : CounterStrike.i.getCounterTerroristsTeam().getCsPlayers()) {
                ct.addEntry(p.getPlayer().getName());
            }

            // Display the round Map
            Team Maps = board.registerNewTeam("MapsRound");
            Maps.addEntry(ChatColor.GRAY.toString());
            Maps.setPrefix(ChatColor.LIGHT_PURPLE + "Map: ");
            Maps.setSuffix(ChatColor.GREEN + "");
            obj.getScore(ChatColor.GRAY.toString()).setScore(18);

            Team roundTeams = board.registerNewTeam("Teams");
            roundTeams.addEntry(ChatColor.RED.toString());
            roundTeams.setPrefix(ChatColor.LIGHT_PURPLE + "Teams: ");
            roundTeams.setSuffix("");
            obj.getScore(ChatColor.RED.toString()).setScore(17);

            Integer scoreCounter = 16;

            for (CSPlayer csplayer1 : myTeam.getCsPlayers()) {
                Team killCounter = board.registerNewTeam("k" + scoreCounter);

                killCounter.addEntry(ChatColor.valueOf(csplayer1.getColour()) + csplayer1.getPlayer().getName());
                killCounter.setPrefix("");
                killCounter.setSuffix(": " + ChatColor.GREEN + "$" + csplayer1.getMoney() + ChatColor.LIGHT_PURPLE + " K: " + ChatColor.GREEN + "" + csplayer1.getKills() + "  " + ChatColor.LIGHT_PURPLE + "D: " + ChatColor.GREEN + "" + csplayer1.getDeaths());
                obj.getScore(ChatColor.valueOf(csplayer1.getColour()) + csplayer1.getPlayer().getName()).setScore(scoreCounter);
                scoreCounter--;
            }

            player.setScoreboard(board);
        }
    }


    public void updateScoreBoard(CSPlayer csplayer) {
        Player player = csplayer.getPlayer();
        //CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

        if (!player.isOnline()) return;

        if (isFolia) {
            updateFastScoreBoard(csplayer);
        } else {
            Scoreboard board = player.getScoreboard();
            board.resetScores(player.getName());

            dev.danablend.counterstrike.csplayer.Team myTeam;

            if (csplayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
                myTeam = CounterStrike.i.getCounterTerroristsTeam();
            } else {
                myTeam = CounterStrike.i.getTerroristsTeam();
            }

            // Display the world
            Team Maps = board.getTeam("MapsRound");

            if (Maps == null) return;

            Maps.setPrefix(ChatColor.LIGHT_PURPLE + "Map: ");
            Maps.setSuffix(ChatColor.GREEN + plugin.Map + "  " + ChatColor.LIGHT_PURPLE + "R: " + ChatColor.GREEN + "" + (myTeam.getLosses() + myTeam.getWins() + 1) + " of " + MAX_ROUNDS);


            Team roundTeams = board.getTeam("Teams");
            roundTeams.setPrefix(ChatColor.LIGHT_PURPLE + "Teams: ");

            String TeamA = ChatColor.valueOf(csplayer.getColour()) + "" + csplayer.getColour() + ": ";
            String TeamB = ChatColor.valueOf(csplayer.getOpponentColour()) + csplayer.getOpponentColour() + ": ";

            String teamString = TeamA + myTeam.getWins() + ChatColor.GRAY + " vs " + TeamB + myTeam.getLosses();
            roundTeams.setSuffix(teamString);


            Integer scoreCounter = 16;
            boolean wasnull;

            for (CSPlayer csplayer1 : myTeam.getCsPlayers()) {
                Team killCounter = board.getTeam("k" + scoreCounter);
                wasnull = false;

                if (killCounter == null) {
                    wasnull = true;
                    killCounter = board.registerNewTeam("k" + scoreCounter);
                }
                killCounter.addEntry(ChatColor.valueOf(csplayer1.getColour()) + csplayer1.getPlayer().getName());
                killCounter.setPrefix("");

                Player play = csplayer1.getPlayer();

                if (play.isDead()) {
                    killCounter.setSuffix(": " + ChatColor.WHITE + ChatColor.UNDERLINE + " DEAD  " + ChatColor.BOLD + "$" + csplayer1.getMoney() + " K: " + "" + csplayer1.getKills() + "  " + "D: " + "" + csplayer1.getDeaths());
                } else {
                    killCounter.setSuffix(": " + ChatColor.GREEN + "$" + csplayer1.getMoney() + ChatColor.LIGHT_PURPLE + " K:" + ChatColor.GREEN + "" + csplayer1.getKills() + " " + ChatColor.LIGHT_PURPLE + "D:" + ChatColor.GREEN + "" + csplayer1.getDeaths() + " " + ChatColor.LIGHT_PURPLE + "MVP:" + ChatColor.GREEN + "" + csplayer1.getMVP());
                }

                if (wasnull) {
                    Objective obj = board.getObjective("counterStrike");
                    obj.getScore(ChatColor.valueOf(csplayer1.getColour()) + csplayer1.getPlayer().getName()).setScore(scoreCounter);
                }
                scoreCounter--;
            }
        }
    }


    public void updateFastScoreBoard(CSPlayer csplayer) {

        if (csplayer.returnBoard() == null) return;

        dev.danablend.counterstrike.csplayer.Team myTeam;

        if (csplayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
            myTeam = CounterStrike.i.getCounterTerroristsTeam();
        } else {
            myTeam = CounterStrike.i.getTerroristsTeam();
        }

        String TeamA = ChatColor.valueOf(csplayer.getColour()) + "" + csplayer.getColour() + ": ";
        String TeamB = ChatColor.valueOf(csplayer.getOpponentColour()) + csplayer.getOpponentColour() + ": ";

        String[] lines = new String[21];

        lines[0] = ChatColor.LIGHT_PURPLE + "Map: " + ChatColor.GREEN + plugin.Map + "  " + ChatColor.LIGHT_PURPLE + "R: " + ChatColor.GREEN + "" + (myTeam.getLosses() + myTeam.getWins() + 1) + " of " + MAX_ROUNDS ;
        lines[1] = ChatColor.LIGHT_PURPLE + "Teams: " + TeamA + myTeam.getWins() + ChatColor.GRAY + " vs " + TeamB + myTeam.getLosses();

        ChatColor c1 = ChatColor.valueOf(plugin.counterTerroristsTeam.getColour());
        lines[2] = ChatColor.BOLD + "" + plugin.counterTerrorists.size() + " " + c1 + "Counters" + ChatColor.WHITE + " with " + plugin.counterTerroristsTeam.getWins() + " wins: ";
        int linha = 3;

        for (CSPlayer csplayer1 : plugin.counterTerrorists) {

            Player play = csplayer1.getPlayer();

            if (play.isDead()) {
                lines[linha] = play.getName() + ": " + ChatColor.WHITE + ChatColor.UNDERLINE + " DEAD  " + ChatColor.BOLD + "$" + csplayer1.getMoney() + " K: " + "" + csplayer1.getKills() + "  " + "D: " + csplayer1.getDeaths();
            } else {
                lines[linha] = play.getName() + ": " + ChatColor.GREEN + "$" + csplayer1.getMoney() + ChatColor.LIGHT_PURPLE + " K:" + ChatColor.GREEN + "" + csplayer1.getKills() + " " + ChatColor.LIGHT_PURPLE + "D:" + ChatColor.GREEN + csplayer1.getDeaths() + " " + ChatColor.LIGHT_PURPLE + "MVP:" + ChatColor.GREEN + "" + csplayer1.getMVP();
            }
            linha++;
        }

        c1 = ChatColor.valueOf(plugin.terroristsTeam.getColour());
        lines[linha] = ChatColor.BOLD + "" + plugin.terrorists.size() + " " + c1 + "Terrors" + ChatColor.WHITE + " with " + plugin.terroristsTeam.getWins() + " wins: ";
        linha++;

        for (CSPlayer csplayer1 : plugin.terrorists) {
            Player play = csplayer1.getPlayer();

            if (play.isDead()) {
                lines[linha] = play.getName() + ": " + ChatColor.WHITE + ChatColor.UNDERLINE + " DEAD  " + ChatColor.BOLD + "$" + csplayer1.getMoney() + " K: " + "" + csplayer1.getKills() + "  " + "D: " + csplayer1.getDeaths();
            } else {
                lines[linha] = play.getName() + ": " + ChatColor.GREEN + "$" + csplayer1.getMoney() + ChatColor.LIGHT_PURPLE + " K:" + ChatColor.GREEN + "" + csplayer1.getKills() + " " + ChatColor.LIGHT_PURPLE + "D:" + ChatColor.GREEN + csplayer1.getDeaths() + " " + ChatColor.LIGHT_PURPLE + "MVP:" + ChatColor.GREEN + "" + csplayer1.getMVP();
            }
            linha++;
        }

        String[] finalFines = new String[linha];

        for (int i = 0; i < linha; i++) {
            finalFines[i] = lines[i];
        }

        csplayer.returnBoard().updateLines(finalFines);
    }


    public void deleteScoreBoards(Player player) {
        if (playersWithScoreboard.contains(player.getUniqueId())) {
            playersWithScoreboard.remove(player.getUniqueId());

            if (isFolia) {
                CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

                if (csplayer == null) {
                    return;
                }

                FastBoard board = csplayer.returnBoard();

                if (board != null) {
                    board.delete();
                }
            } else {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }
    }
}
