package dev.danablend.counterstrike.runnables;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.events.WeaponFireEvent;
import dev.danablend.counterstrike.utils.PlayerUtils;

public class Gunfire extends BukkitRunnable {

	private HashMap<UUID, Long> firing;
	private Player player;
	private Weapon gun;

	public Gunfire(HashMap<UUID, Long> firing, Player player, Weapon gun) {
		this.firing = firing;
		this.player = player;
		this.gun = gun;
	}

	@Override
	public void run() {
		if(gun.getAmmunition() <= 0) {
			this.cancel();
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
		if (!firing.containsKey(player.getUniqueId())) {
			this.cancel();
		}
	}
}
