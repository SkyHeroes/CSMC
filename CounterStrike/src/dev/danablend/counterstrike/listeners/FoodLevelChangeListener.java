package dev.danablend.counterstrike.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevelChangeListener implements Listener {
	
	@EventHandler
	public void foodLevelChangeEvent(FoodLevelChangeEvent event) {
		event.setCancelled(true);
	}
	
}
