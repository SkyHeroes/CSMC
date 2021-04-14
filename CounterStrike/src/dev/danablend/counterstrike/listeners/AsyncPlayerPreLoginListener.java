package dev.danablend.counterstrike.listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import dev.danablend.counterstrike.CounterStrike;

public class AsyncPlayerPreLoginListener implements Listener {
	
	CounterStrike plugin;
	FileConfiguration config;
	
	public AsyncPlayerPreLoginListener(CounterStrike plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfig();	
	}
	
	@EventHandler
	public void playerJoinEvent(AsyncPlayerPreLoginEvent event) {
		if(Bukkit.getOnlinePlayers().size() >= config.getInt("max-players")) {
			event.disallow(Result.KICK_FULL, "The game is full, please try again later.");
		}
	}

}
