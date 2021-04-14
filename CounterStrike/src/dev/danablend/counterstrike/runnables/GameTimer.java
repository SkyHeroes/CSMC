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
		if(CounterStrike.i.getGameState() != GameState.STARTED) {
			this.cancel();
			return;
		}
		timeToEnd--;
		PacketUtils.sendActionBarToAll(ChatColor.YELLOW + "The Counter Terrorists will win in " + Utils.getFormattedTimeString(timeToEnd), 1);
		if(timeToEnd <= 0) {
			CounterStrike.i.restartGame(CounterStrike.i.getCounterTerroristsTeam());
			this.cancel();
		}
	}

}
