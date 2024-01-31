package dev.danablend.counterstrike.runnables;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static dev.danablend.counterstrike.Config.MIN_PLAYERS;

public class GameCounter {

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

    //CouterStrike->GameCounter (aguarda que jogadores digam que querem jogar)->GameStarter (espera pelo minimo de jogadores e passa a starting)->GameTimer
    //  GameState.LOBBY    - qd jogadores entram, depois de terminar qd jogadores ficam em espera ou qd nao tem jogadores a jogar
    //  GameState.WAITING  - qd aguarda que cheguem mais jogadores em GameCounter
    //  GameState.STARTING - qd esta memso a começar, qd arrnaca GameStarter

    public void run() {

        if (!Config.GAME_ENABLED) {
            CounterStrike.i.gameState = GameState.LOBBY;
            plugin.StopGameCounter();
            return;
        }
        int serverSize = plugin.getCSPlayers().size();

        if (serverSize == 0) {
            CounterStrike.i.gameState = GameState.LOBBY;
            plugin.StopGameCounter();
            return;
        }

        int minPlayers = MIN_PLAYERS;

        if (serverSize >= minPlayers) {
            GameStarter start = new GameStarter(plugin);
            Object task;
            task = plugin.myBukkit.runTaskTimer(null, null, null, () -> start.run(), 40L, 20L);
            start.setScheduledTask(task);

            CounterStrike.i.gameState = GameState.STARTING;
            plugin.StopGameCounter();

        } else {
            CounterStrike.i.gameState = GameState.WAITING;
            String msg = (minPlayers - serverSize <= 1) ? Utils.color("&6The game needs &a" + (minPlayers - serverSize) + " &6more player to start!") : Utils.color("&6The game needs &a" + (minPlayers - serverSize) + " &6more players to start!");
            plugin.broadcastMessage(msg);

            PacketUtils.sendActionBarToWaitingInLobby(msg);
        }
    }
}
