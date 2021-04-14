package dev.danablend.counterstrike.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.enums.WeaponType;
import dev.danablend.counterstrike.events.WeaponFireEvent;
import dev.danablend.counterstrike.runnables.Bomb;
import dev.danablend.counterstrike.runnables.Gunfire;
import dev.danablend.counterstrike.shop.Shop;
import dev.danablend.counterstrike.utils.PlayerUtils;

public class PlayerInteractListener implements Listener {

	private HashMap<UUID, Long> firing;
	private Set<UUID> pistolCD;

	public PlayerInteractListener() {
		this.firing = new HashMap<UUID, Long>();
		this.pistolCD = new HashSet<UUID>();
	}
	
	@EventHandler
	public void playerDefuseEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		CSPlayer csplayer = CounterStrike.i.getCSPlayer(player);
		if(Bomb.bomb == null) {
			return;
		}
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if(csplayer.getTeam() != TeamEnum.COUNTER_TERRORISTS) {
			return;
		}
		
		Bomb bomb = Bomb.bomb;
		
		bomb.defuse(csplayer);
		
	}

	@EventHandler
	public void playerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		PlayerInventory inv = player.getInventory();
		if(player.getGameMode().equals(GameMode.SPECTATOR)) {
			event.setCancelled(true);
			return;
		}
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			CSPlayer csplayer = CounterStrike.i.getCSPlayer(player);
			// Gun checker
			ItemStack gunItem = inv.getItemInMainHand();
			if (Weapon.isWeapon(gunItem)) {
				Weapon gun = Weapon.getByItem(inv.getItemInMainHand());
				if (gun.getWeaponType() == WeaponType.RIFLE) {
					CounterStrike.i.removePlayerFromFiring(player, 5, firing);
					if (gunItem.getAmount() - 1 <= 0) {
						return;
					}
					firing.put(player.getUniqueId(), System.currentTimeMillis());
					new Gunfire(firing, player, gun).runTaskTimer(CounterStrike.getInstance(), 0L, 6L);
				}
				else {
					shoot(player, gun);
				}
			}
			// Shop Item Checker
			if (inv.getItemInMainHand().equals(CounterStrike.i.getShopItem())) {
				if (csplayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
					Shop.getShop().openCounterTerroristShop(player);
				} else {
					Shop.getShop().openTerroristShop(player);
				}
			}
		}
		if(!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.TNT))
			event.setCancelled(true);
	}

	private void shoot(Player player, Weapon gun) {
		if(pistolCD.contains(player.getUniqueId())) {
			return;
		}
		ItemStack gunItem = player.getInventory().getItemInMainHand();
		if(gunItem.getAmount() - 1 <= 0) {
			return;
		}
		Vector playerDir = player.getLocation().getDirection();
		Vector dirVel = playerDir.multiply(5);
		Snowball bullet = (Snowball) player.getWorld().spawnEntity(PlayerUtils.getRightHeadLocation(player), EntityType.SNOWBALL);
		bullet.setVelocity(dirVel);
		bullet.setShooter(player);
		bullet.setBounce(false);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1, 1);
		PlayerUtils.shakeScreen(player, 2);
		WeaponFireEvent calledEvent = new WeaponFireEvent(player, gun, bullet);
		Bukkit.getPluginManager().callEvent(calledEvent);
		pistolCD.add(player.getUniqueId());
		new BukkitRunnable() {
			public void run() {
				pistolCD.remove(player.getUniqueId());
			}
		}.runTaskLater(CounterStrike.i, 5L);
	}
}
