package dev.danablend.counterstrike.events;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.utils.Utils;

public class BulletHitEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	
	private Player shooter;
	private Player victim;
	private Projectile bullet;
	private Weapon gun;
	
	public BulletHitEvent(Player shooter, Player victim, Projectile bullet, Weapon gun) {
		Utils.debug("BulletHitEvent has been called...");
		this.shooter = shooter;
		this.victim = victim;
		this.bullet = bullet;
		this.gun = gun;
	}
	
	public Player getShooter() {
		return shooter;
	}
	
	public Player getVictim() {
		return victim;
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
