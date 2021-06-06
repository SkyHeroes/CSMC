package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.database.Mundos;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;

public class PlayerRespawnListener implements Listener {
	
	@EventHandler
	public void playerRespawnEvent(PlayerRespawnEvent event) {

		String mundo = event.getPlayer().getWorld().getName();

		if (CounterStrike.i.HashWorlds != null) {
			Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

			if (md != null && !md.modoCs) {
				return;
			}
		}

		for(CSPlayer csplayer : CounterStrike.i.getCSPlayers()) {
			if(csplayer.getPlayer().getUniqueId().equals(event.getPlayer().getUniqueId())) {
				event.setRespawnLocation(csplayer.getSpawnLocation());
			}
		}
	}
	
}
