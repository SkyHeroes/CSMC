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
import org.bukkit.scoreboard.Scoreboard;

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

            if (csplayer.getPlayer() == null) return;

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

        FastBoard board = new FastBoard(player);
        board.updateTitle(ChatColor.BOLD + "----Miner Strike v" + CounterStrike.i.getDescription().getVersion() + "----");
        csplayer.setBoard(board);

    }


    public void updateScoreBoard(CSPlayer csplayer) {
        Player player = csplayer.getPlayer();

        if (!player.isOnline()) return;

        updateFastScoreBoard(csplayer);
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

        lines[0] = ChatColor.LIGHT_PURPLE + "Map: " + ChatColor.GREEN + plugin.Map + "  " + ChatColor.LIGHT_PURPLE + "R: " + ChatColor.GREEN + "" + (myTeam.getLosses() + myTeam.getWins() + 1) + " of " + MAX_ROUNDS;
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

            CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

            if (csplayer == null) {
                return;
            }

            Scoreboard board1 = Bukkit.getScoreboardManager().getMainScoreboard();
            if (board1.getTeam("team1") != null) board1.getTeam("team1").unregister();
            if (board1.getTeam("team2") != null) board1.getTeam("team2").unregister();

            FastBoard board = csplayer.returnBoard();

            if (board != null) {
                board.delete();
            }
        }
    }
}
