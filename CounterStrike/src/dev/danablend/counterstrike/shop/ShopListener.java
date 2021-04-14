package dev.danablend.counterstrike.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.enums.Weapon;

public class ShopListener implements Listener {

	@EventHandler
	public void inventoryClickEvent(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			ItemStack clicked = event.getCurrentItem();
			if (event.getView().getTitle().equals(Config.terroristShopName)) {
				event.setCancelled(true);
				Player player = (Player) event.getWhoClicked();
				Weapon gun = Weapon.getByItem(clicked);
				if (gun != null) {
					Shop.getShop().purchaseShopItem(player, gun);
				}
			} else if (event.getView().getTitle().equals(Config.counterTerroristShopName)) {
				event.setCancelled(true);
				Player player = (Player) event.getWhoClicked();
				Weapon gun = Weapon.getByItem(clicked);
				if (gun != null) {
					Shop.getShop().purchaseShopItem(player, gun);
				}
			}
		}
	}
}
