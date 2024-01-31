package dev.danablend.counterstrike.runnables;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.ChatColor;

public class GameTimer {
    int timeToEnd = Config.MATCH_DURATION;
    Object task;
    CounterStrike plugin;


    public GameTimer() {
        this.plugin = CounterStrike.i;
        //        this.runTaskTimer(CounterStrike.i, 20L, 20L);
        task = plugin.myBukkit.runTaskTimer(null, null, null, () -> run(), 20L, 20L);
    }

    public void run() {
        int serverSize = CounterStrike.i.getCSPlayers().size();

        if (CounterStrike.i.gameState == GameState.PLANTED) {

//            if (CounterStrike.i.myBukkit.isFolia())
//                ((ScheduledTask) task).cancel();
//            else
//                ((BukkitTask) task).cancel();

            plugin.myBukkit.cancelTask(task);

            return;
        }

        if (serverSize == 0) {
            Utils.debug("Aborting game, no players left");

            CounterStrike.i.getTerroristsTeam().setLosses(0);
            CounterStrike.i.getTerroristsTeam().setWins(0);
            CounterStrike.i.getTerroristsTeam().setColour("WHITE");
            CounterStrike.i.getCounterTerroristsTeam().setLosses(0);
            CounterStrike.i.getCounterTerroristsTeam().setWins(0);
            CounterStrike.i.getCounterTerroristsTeam().setColour("WHITE");

//            if (CounterStrike.i.gameCount == null) {
//                CounterStrike.i.gameCount = new GameCounter(CounterStrike.i);
////                CounterStrike.i.gameCount.start();
////            } else {
////                CounterStrike.i.gameCount.start();
//            }


          //  plugin.StartGameCounter(0);

            CounterStrike.i.gameState = GameState.LOBBY;

//            if (CounterStrike.i.myBukkit.isFolia())
//                ((ScheduledTask) task).cancel();
//            else
//                ((BukkitTask) task).cancel();

            plugin.myBukkit.cancelTask(task);

            return;
        }

        if (CounterStrike.i.getGameState() != GameState.RUN) {

//            if (CounterStrike.i.myBukkit.isFolia())
//                ((ScheduledTask) task).cancel();
//            else
//                ((BukkitTask) task).cancel();

            plugin.myBukkit.cancelTask(task);

            return;
        }
        timeToEnd--;
        PacketUtils.sendActionBarToInGame(ChatColor.YELLOW + "The Counter Terrorists will win in " + Utils.getFormattedTimeString(timeToEnd));

        if (timeToEnd <= 0) {
            CounterStrike.i.restartGame(CounterStrike.i.getCounterTerroristsTeam());

//            if (CounterStrike.i.myBukkit.isFolia())
//                ((ScheduledTask) task).cancel();
//            else
//                ((BukkitTask) task).cancel();

            plugin.myBukkit.cancelTask(task);

        }
    }


    public Integer returnTimetoEnd() {
        return timeToEnd;
    }
}
