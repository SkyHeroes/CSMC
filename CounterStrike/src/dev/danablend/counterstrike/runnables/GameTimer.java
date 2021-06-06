package dev.danablend.counterstrike.runnables;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;

public class GameTimer extends BukkitRunnable {

    int timeToEnd = Config.MATCH_DURATION;

    public GameTimer() {
        this.runTaskTimer(CounterStrike.i, 20L, 20L);
    }

    @Override
    public void run() {
        int serverSize = CounterStrike.i.getCSPlayers().size();

        if (serverSize == 0) {
            System.out.println("Aborting game, no players left");

            CounterStrike.i.getTerroristsTeam().setLosses(0);
            CounterStrike.i.getTerroristsTeam().setWins(0);
            CounterStrike.i.getTerroristsTeam().setColour("WHITE");
            CounterStrike.i.getCounterTerroristsTeam().setLosses(0);
            CounterStrike.i.getCounterTerroristsTeam().setWins(0);
            CounterStrike.i.getCounterTerroristsTeam().setColour("WHITE");

            if (CounterStrike.i.gameCount == null) {
                CounterStrike.i.gameCount = new GameCounter(CounterStrike.i);
                CounterStrike.i.gameCount.runTaskTimer(CounterStrike.i, 40L, 200L);
            }

            CounterStrike.i.gameState = GameState.LOBBY;
            this.cancel();
            return;
        }

        if (CounterStrike.i.getGameState() != GameState.RUN) {
            this.cancel();
            return;
        }
        timeToEnd--;
        PacketUtils.sendActionBarToInGame(ChatColor.YELLOW + "The Counter Terrorists will win in " + Utils.getFormattedTimeString(timeToEnd), 1);

        if (timeToEnd <= 0) {
            CounterStrike.i.restartGame(CounterStrike.i.getCounterTerroristsTeam());
            this.cancel();
        }
    }


    public Integer returnTimetoEnd() {
        return timeToEnd;
    }
}
