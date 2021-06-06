package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.database.Mundos;
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

        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();

            String mundo = victim.getWorld().getName();

            //if has generalP loaded
            if (CounterStrike.i.HashWorlds != null) {
                Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

                if (md != null && !md.modoCs) {
                    return;
                }
            }

            if (CounterStrike.i.gameState.equals(GameState.LOBBY)) {
                event.setCancelled(true);
                return;
            }
            if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
                Player damager = (Player) event.getDamager();

                CSPlayer csShooter = CounterStrike.i.getCSPlayer(damager, false, null);
                CSPlayer csVictim = CounterStrike.i.getCSPlayer(victim, false, null);

                if (csShooter == null || csVictim == null) {
                    return;
                }

                if (csShooter.getTeam().equals(csVictim.getTeam()) && !Config.FRIENDLY_FIRE_ENABLED) {
                    event.setCancelled(true);
                    return;
                }

                if (damager.getInventory().getItemInMainHand().equals(CounterStrike.i.getKnife())) {

                    if (PlayerUtils.playerBehindPlayer(damager, victim)) {
                        event.setDamage(40);
                        System.out.println(" Knife da dano behind " + event.getDamage());
                    } else {
                        event.setDamage(event.getDamage() / 4);
                        System.out.println(" Knife da dano " + event.getDamage());
                    }
                    return;
                }
            }

            if (event.getDamager() instanceof Snowball && event.getEntity() instanceof Player) {
                Snowball bullet = (Snowball) event.getDamager();

                System.out.println("  #################  Snowball??? da dano " + event.getDamage());

                if (bullet.getShooter() instanceof Player) {
                    Player damager = (Player) bullet.getShooter();

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

}
