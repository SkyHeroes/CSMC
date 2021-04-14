package dev.danablend.counterstrike.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;

public class PlayerQuitListener implements Listener {
	
	CounterStrike plugin;
	
	public PlayerQuitListener() {
		this.plugin = CounterStrike.i;
	}
	
	@EventHandler
	public void playerQuitEvent(PlayerQuitEvent event) {
		CSPlayer csplayer = plugin.getCSPlayer(event.getPlayer());
		csplayer.clear();
	}
	
}
