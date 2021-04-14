package dev.danablend.counterstrike.shop;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.enums.WeaponType;
import dev.danablend.counterstrike.utils.Utils;

public class Shop {

	private static Shop shop;
	
	private Collection<Weapon> terroristGuns;
	private Collection<Weapon> counterTerroristGuns;

	public Shop() {
		shop = this;
		this.terroristGuns = Weapon.getAllWeaponsByTeam(TeamEnum.TERRORISTS);
		this.counterTerroristGuns = Weapon.getAllWeaponsByTeam(TeamEnum.COUNTER_TERRORISTS);
	}

	public void openCounterTerroristShop(Player player) {
		Utils.debug("Opening Counter Terrorist Shop for " + player.getName());
		Inventory inv = Bukkit.createInventory(null, getInventorySize(counterTerroristGuns.size()), Config.counterTerroristShopName);
		for (Weapon gun : counterTerroristGuns) {
			inv.addItem(gun.getShopItem());
		}
		player.openInventory(inv);
	}

	public void openTerroristShop(Player player) {
		Utils.debug("Opening Terrorist Shop for " + player.getName());
		Inventory inv = Bukkit.createInventory(null, getInventorySize(terroristGuns.size()), Config.terroristShopName);
		for (Weapon gun : terroristGuns) {
			inv.addItem(gun.getShopItem());
		}
		player.openInventory(inv);
	}

	public void purchaseShopItem(Player player, Weapon gun) {
		Utils.debug("Purchase of an item has been initiated...");
		CSPlayer csplayer = CounterStrike.i.getCSPlayer(player);
		int money = csplayer.getMoney();
		int slot = 0;
		WeaponType type = gun.getWeaponType();
		if (gun.getCost() > money) {
			player.sendMessage(ChatColor.RED + "Sorry, but you cannot afford this item.");
			return;
		}
		switch(type) {
			case RIFLE:
				if (csplayer.getRifle() != null) {
					player.sendMessage(ChatColor.RED + "Sorry, you cannot have two rifles at the same time.");
					return;
				}
				slot = 0;
				break;
			case PISTOL:
				if (csplayer.getPistol() != null) {
					player.sendMessage(ChatColor.RED + "Sorry, you cannot have two pistols at the same time.");
					return;
				}
				slot = 1;
				break;
			case GRENADE:
				if (csplayer.getGrenade() != null) {
					player.sendMessage(ChatColor.RED + "Sorry, you cannot have two grenades at the same time.");
					return;
				}
				slot = 3;
				break;
		}
		
		csplayer.setMoney(money - gun.getCost());
		player.getInventory().setItem(slot, gun.getItem());
		
		if(CounterStrike.i.usingQualityArmory() && type != WeaponType.GRENADE) {
			ItemStack ammo = me.zombie_striker.qg.api.QualityArmory.getGunByName(gun.getName()).getAmmoType().getItemStack().clone();
			ammo.setAmount((gun.getMagazines()-1)*gun.getMagazineCapacity());
			if(type == WeaponType.RIFLE) {
				player.getInventory().setItem(6, ammo);
			}else player.getInventory().setItem(7, ammo);
		}
		
		player.sendMessage(ChatColor.GREEN + "You have purchased " + gun.getDisplayName());
		Utils.debug("Purchase of an item has been completed...");
	}

	public static Shop getShop() {
		Utils.debug("Getting Shop...");
		return shop;
	}

	public int getInventorySize(int amountOfItems) {
		Utils.debug("Getting inventory size for shops...");
		int temp = amountOfItems;
		while (temp != 0 && temp % 9 != 0) {
			temp++;
		}
		return temp <= 54 ? temp : 54;
	}

}
