package dev.danablend.counterstrike.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @author barpec12
 * created on 2020-05-24
 */
public class InventoryClickListener implements Listener{
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
	}
}
