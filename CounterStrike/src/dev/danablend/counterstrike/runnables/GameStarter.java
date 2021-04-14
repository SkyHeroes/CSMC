package dev.danablend.counterstrike.runnables;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;

public class GameStarter extends BukkitRunnable {
	
	CounterStrike plugin;
	
	Collection<CSPlayer> csPlayers;
	int timeToStart;
	
	public GameStarter(CounterStrike plugin) {
		this.plugin = plugin;
		csPlayers = plugin.getCSPlayers();
		if(plugin.getTerroristsTeam().getWins()+plugin.getTerroristsTeam().getLosses()==0) {
			timeToStart = 10;
		}else timeToStart = 1;
		
		for(Player player : Bukkit.getOnlinePlayers()) {
			CSPlayer csplayer = CounterStrike.i.getCSPlayer(player);
			if(!csPlayers.contains(csplayer)) {
				csPlayers.add(csplayer);
			}
		}
	}
	
	
	@Override
	public void run() {
		if(timeToStart <= 0) {
			plugin.startGame();
			this.cancel();
			return;
		}
		for(Player player : Bukkit.getOnlinePlayers()) {
			CSPlayer csplayer = CounterStrike.i.getCSPlayer(player);
			if(!csPlayers.contains(csplayer)) {
				csPlayers.add(csplayer);
			}
		}
		String message = (timeToStart != 1) ? ChatColor.YELLOW + "The game will start in " + timeToStart + " seconds!" : ChatColor.YELLOW + "The game will start in " + timeToStart + " second!";
		Bukkit.broadcastMessage(message);
		timeToStart--;
	}

}
