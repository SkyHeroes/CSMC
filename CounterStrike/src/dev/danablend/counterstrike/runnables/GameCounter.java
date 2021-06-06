package dev.danablend.counterstrike.runnables;

import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.utils.PacketUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.utils.Utils;

import static dev.danablend.counterstrike.Config.MIN_PLAYERS;

public class GameCounter extends BukkitRunnable {

    CounterStrike plugin;
    FileConfiguration config;

    public GameCounter(CounterStrike plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        if (plugin.getPlayerUpdater() != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getPlayerUpdater().deleteScoreBoards(player);
            }
        }
    }

    @Override
    public void run() {
        if (!Config.GAME_ENABLED) {
            this.cancel();
            return;
        }
        int serverSize = plugin.getCSPlayers().size();

        if (serverSize == 0) {
            CounterStrike.i.gameState = GameState.LOBBY;
            return;
        }

        int minPlayers = MIN_PLAYERS;

        if (serverSize >= minPlayers) {
            new GameStarter(plugin).runTaskTimer(plugin, 40L, 20L);
            CounterStrike.i.gameState = GameState.STARTING;
            this.cancel();
            plugin.gameCount = null;
        } else {
            CounterStrike.i.gameState = GameState.WAITING;
            String msg = (minPlayers - serverSize <= 1) ? Utils.color("&6The game needs &a" + (minPlayers - serverSize) + " &6more player to start!") : Utils.color("&6The game needs &a" + (minPlayers - serverSize) + " &6more players to start!");
            plugin.broadcastMessage(msg);

            PacketUtils.sendActionBarToWaitingInLobby(msg,2);
        }
    }

}
