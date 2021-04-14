package dev.danablend.counterstrike.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;

public class PlayerRespawnListener implements Listener {
	
	@EventHandler
	public void playerRespawnEvent(PlayerRespawnEvent event) {
		for(CSPlayer csplayer : CounterStrike.i.getCSPlayers()) {
			if(csplayer.getPlayer().getUniqueId().equals(event.getPlayer().getUniqueId())) {
				event.setRespawnLocation(csplayer.getSpawnLocation());
			}
		}
	}
	
}
