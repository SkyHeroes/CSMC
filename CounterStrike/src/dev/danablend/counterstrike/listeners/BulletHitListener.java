package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.database.Mundos;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.events.BulletHitEvent;
import dev.danablend.counterstrike.events.CustomPlayerDeathEvent;
import dev.danablend.counterstrike.utils.CSUtil;

public class BulletHitListener implements Listener {
	
	@EventHandler
	public void bulletHitEvent(BulletHitEvent event) {

		String mundo = event.getVictim().getWorld().getName();

		if (CounterStrike.i.HashWorlds != null) {
			Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

			if (md != null && !md.modoCs) {
				return;
			}
		}

		System.out.println(event);

		CSPlayer csShooter = CounterStrike.i.getCSPlayer(event.getShooter(),false, null);
		CSPlayer csVictim = CounterStrike.i.getCSPlayer(event.getVictim(),false, null);

		if (csShooter == null || csVictim == null) {
			return;
		}

		double damageToDo = event.getGun().getDamage();

		if(CSUtil.isHeadShot(event.getBullet(), csVictim.getPlayer())) {
			damageToDo *= 2;
		}
		if(csShooter.getTeam().equals(csVictim.getTeam()) && !Config.FRIENDLY_FIRE_ENABLED) {
			return;
		}
		event.getVictim().damage(damageToDo, event.getShooter());

		if(event.getVictim().getHealth() <= 0 && event.getVictim().isDead()) {
			CustomPlayerDeathEvent calledEvent = new CustomPlayerDeathEvent(event.getVictim(), event.getShooter(), event.getBullet(), event.getGun());
			Bukkit.getPluginManager().callEvent(calledEvent);
		}
	}
	
}
