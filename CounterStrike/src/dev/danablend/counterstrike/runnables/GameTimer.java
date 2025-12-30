package dev.danablend.counterstrike.runnables;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.enums.GameState;
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
        int serverSize = plugin.getCSPlayers().size();

        if (plugin.getGameState().equals(GameState.PLANTED)) {
            plugin.myBukkit.cancelTask(task);
            return;
        }

        if ((serverSize == 0 || plugin.getServer().getOnlinePlayers().size() == 0) && plugin.quitExitGame) {
            Utils.debug("Aborting game, no players left");
            plugin.myBukkit.cancelTask(task);
            plugin.FinishGame(plugin.getTerroristsTeam(), plugin.getCounterTerroristsTeam());
            return;
        }

        if (!plugin.getGameState().equals(GameState.RUN)) {
            plugin.myBukkit.cancelTask(task);
            return;
        }
        timeToEnd--;

        if (plugin.modeValorant || plugin.modeRealms) {
            PacketUtils.sendActionBarToInGame(ChatColor.YELLOW + "The Defenders will win in " + Utils.getFormattedTimeString(timeToEnd));
        } else {
            PacketUtils.sendActionBarToInGame(ChatColor.YELLOW + "The Counter Terrorists will win in " + Utils.getFormattedTimeString(timeToEnd));
        }

        if (timeToEnd <= 0) {
            plugin.restartGame(plugin.getCounterTerroristsTeam());
            plugin.myBukkit.cancelTask(task);
        }
    }


    public Integer returnTimetoEnd() {
        return timeToEnd;
    }


    public void terminateTimer() {
        plugin.myBukkit.cancelTask(task);
    }
}
