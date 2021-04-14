package dev.danablend.counterstrike.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {
	
	@EventHandler
	public void playerItemDropEvent(PlayerDropItemEvent event) {
		if(!event.getPlayer().isOp()) {
			int currentSlot = event.getPlayer().getInventory().getHeldItemSlot();
			if(currentSlot == 0 || currentSlot == 1)
				return;
			event.setCancelled(true);
		}
	}
	
}
