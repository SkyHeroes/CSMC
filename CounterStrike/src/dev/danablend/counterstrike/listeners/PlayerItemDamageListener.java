package dev.danablend.counterstrike.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerItemDamageListener implements Listener {
	
	@EventHandler
	public void playerItemDamageEvent(PlayerItemDamageEvent event) {
		event.setCancelled(true);
		Damageable meta = (Damageable) event.getItem().getItemMeta();
		meta.setDamage(meta.getDamage() - 1);
		event.getItem().setItemMeta((ItemMeta) meta);
//		event.getPlayer().getInventory().getItemInHand().setDurability((short) (event.getPlayer().getInventory().getItemInHand().getDurability() - 1));
	}
	
}
