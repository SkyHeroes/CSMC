package dev.danablend.counterstrike.events;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.utils.Utils;

public class WeaponFireEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	Player shooter;
	Weapon gun;
	Projectile bullet;
	

	public WeaponFireEvent(Player shooter, Weapon gun, Projectile bullet) {
		Utils.debug("WeaponFireEvent has been called...");
		this.shooter = shooter;
		this.gun = gun;
		this.bullet = bullet;
	}
	
	public Player getPlayer() {
		return shooter;
	}
	
	public Projectile getBullet() {
		return bullet;
	}
	
	public Weapon getGun() {
		return gun;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
