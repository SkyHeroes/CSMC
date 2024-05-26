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
        task = plugin.myBukkit.runTaskTimer(null, null, null, () -> run(), 20L, 20L);
    }

    public void run() {
        int serverSize = CounterStrike.i.getCSPlayers().size();

        if (CounterStrike.i.gameState == GameState.PLANTED) {
            plugin.myBukkit.cancelTask(task);
            return;
        }

        if (serverSize == 0 || plugin.getServer().getOnlinePlayers().size() == 0) {
            Utils.debug("Aborting game, no players left");

            plugin.myBukkit.cancelTask(task);
            CounterStrike.i.FinishGame(CounterStrike.i.getTerroristsTeam(),CounterStrike.i.getCounterTerroristsTeam());
            return;
        }

        if (CounterStrike.i.getGameState() != GameState.RUN) {
            plugin.myBukkit.cancelTask(task);
            return;
        }
        timeToEnd--;
        PacketUtils.sendActionBarToInGame(ChatColor.YELLOW + "The Counter Terrorists will win in " + Utils.getFormattedTimeString(timeToEnd));

        if (timeToEnd <= 0) {
            CounterStrike.i.restartGame(CounterStrike.i.getCounterTerroristsTeam());
            plugin.myBukkit.cancelTask(task);
        }
    }


    public Integer returnTimetoEnd() {
        return timeToEnd;
    }
}
