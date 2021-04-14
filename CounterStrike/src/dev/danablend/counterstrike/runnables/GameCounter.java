package dev.danablend.counterstrike.runnables;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.utils.Utils;

public class GameCounter extends BukkitRunnable {
	
	CounterStrike plugin;
	FileConfiguration config;
	
	public GameCounter(CounterStrike plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfig();
	}
	
	@Override
	public void run() {
		if(!Config.GAME_ENABLED) {
			this.cancel();
			return;
		}
		int serverSize = Bukkit.getOnlinePlayers().size();
		if(serverSize == 0)
			return;
		int minPlayers = config.getInt("min-players");
		
		if(serverSize >= minPlayers) {
			new GameStarter(plugin).runTaskTimer(plugin, 0L, 20L);
			this.cancel();
		} else {
			String msg = (minPlayers - serverSize <= 1) ? Utils.color("&6The game needs &a" + (minPlayers - serverSize) + " &6more player to start!") : Utils.color("&6The game needs &a" + (minPlayers - serverSize) + " &6more players to start!");
			Bukkit.broadcastMessage(msg);
		}
	}

}
