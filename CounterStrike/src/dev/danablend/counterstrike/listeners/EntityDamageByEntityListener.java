package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.database.Worlds;
import dev.danablend.counterstrike.utils.PlayerUtils;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {

        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            String world = victim.getWorld().getName();

            if (CounterStrike.i.HashWorlds != null) {
                Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

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
                    } else {
                        event.setDamage(event.getDamage() / 2);
                    }
                }
            }

            if (event.getDamager() instanceof Player && event.getEntity() instanceof Chicken && event.getEntity().isDead()) {
                Player damager = (Player) event.getDamager();
                CSPlayer csShooter = CounterStrike.i.getCSPlayer(damager, false, null);
                csShooter.setChickenKills(csShooter.getKills() + 1);
            }

        }
        //Animals
        else {

        }
    }

}
