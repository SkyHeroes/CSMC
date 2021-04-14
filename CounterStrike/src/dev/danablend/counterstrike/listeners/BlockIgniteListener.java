package dev.danablend.counterstrike.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

/**
 * @author barpec12
 * created on 2020-05-19
 */
public class BlockIgniteListener implements Listener{
	@EventHandler
	public void onBlockIgniteEvent(BlockIgniteEvent e) {
		if(e.getBlock().getType().equals(Material.TNT)) {
			e.setCancelled(true);
		}
	}
}
