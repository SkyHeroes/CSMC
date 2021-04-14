package dev.danablend.counterstrike.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.events.BulletHitEvent;
import dev.danablend.counterstrike.utils.PlayerUtils;

public class EntityDamageByEntityListener implements Listener {

	@EventHandler
	public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if(CounterStrike.i.gameState.equals(GameState.LOBBY)) {
			event.setCancelled(true);
			return;
		}
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			Player damager = (Player) event.getDamager();
			Player victim = (Player) event.getEntity();
			
			CSPlayer csShooter = CounterStrike.i.getCSPlayer(damager);
			CSPlayer csVictim = CounterStrike.i.getCSPlayer(victim);
			
			if(csShooter.getTeam().equals(csVictim.getTeam()) && !Config.FRIENDLY_FIRE_ENABLED) {
				event.setCancelled(true);
				return;
			}
			
			if (damager.getInventory().getItemInMainHand().equals(CounterStrike.i.getKnife())) {
				if (PlayerUtils.playerBehindPlayer(damager, victim)) {
//					victim.damage(20);
					event.setDamage(40);
				}
			}
		}
		if (event.getDamager() instanceof Snowball && event.getEntity() instanceof Player) {
			Snowball bullet = (Snowball) event.getDamager();
			if (bullet.getShooter() instanceof Player) {
				Player damager = (Player) bullet.getShooter();
				Player victim = (Player) event.getEntity();
				if (Weapon.isWeapon(damager.getInventory().getItemInMainHand())) {
					Weapon gun = Weapon.getByItem(damager.getInventory().getItemInMainHand());
					BulletHitEvent calledEvent = new BulletHitEvent(damager, victim, bullet, gun);
					Bukkit.getPluginManager().callEvent(calledEvent);
					victim.setNoDamageTicks(0);
					event.setCancelled(true);
				}
			}
		}
	}

}
