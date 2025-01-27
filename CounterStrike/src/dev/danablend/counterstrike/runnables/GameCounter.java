package dev.danablend.counterstrike.runnables;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.enums.GameState;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static dev.danablend.counterstrike.Config.MIN_PLAYERS;

public class GameCounter {

    CounterStrike plugin;
    FileConfiguration config;
    private int counter = 0;

    public GameCounter(CounterStrike plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        if (plugin.getPlayerUpdater() != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getPlayerUpdater().deleteScoreBoards(player);
            }
        }
    }

    // CouterStrike->GameCounter (waits for players to join the game - WAITING)->GameStarter (waits for mininum players to start countdown and passes to STARTING)->GameTimer
    //  GameState.LOBBY    - when players join the server but haven't join the CS game, also after game finishs
    //  GameState.WAITING  - when waiting for more players to join before start in GameCounter
    //  GameState.STARTING - when it is starting will fire GameStarter

    public void run() {

        if (!Config.GAME_ENABLED) {
            CounterStrike.i.setGameState(GameState.LOBBY);
            plugin.StopGameCounter();
            return;
        }
        int serverSize = plugin.getCSPlayers().size();

        if ((serverSize == 0 || plugin.getServer().getOnlinePlayers().size() == 0) && plugin.quitExitGame) {
            Utils.debug("Aborting Counter, no players left");
            CounterStrike.i.setGameState(GameState.LOBBY);
            plugin.StopGameCounter();
            return;
        }

        int minPlayers = MIN_PLAYERS;

        if (serverSize >= minPlayers) {

            if (!CounterStrike.i.getGameState().equals(GameState.STARTING)) {
                GameStarter start = new GameStarter(plugin);
                Object task;
                task = plugin.myBukkit.runTaskTimer(null, null, null, () -> start.run(), 40L, 20L);
                start.setScheduledTask(task);

                CounterStrike.i.setGameState(GameState.STARTING);
            }

            plugin.StopGameCounter();

        } else {
            CounterStrike.i.setGameState(GameState.WAITING);

            String msg = (minPlayers - serverSize <= 1) ? Utils.color("&6The game needs &a" + (minPlayers - serverSize) + " &6more player to start!") : Utils.color("&6The game needs &a" + (minPlayers - serverSize) + " &6more players to start!");

            if ((counter % 5) == 0) {
                plugin.broadcastMessage(msg);
                PacketUtils.sendActionBarToWaitingInLobby(msg);
            }

            counter++;
        }
    }
}
