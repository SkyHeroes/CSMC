package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.database.Worlds;
import dev.danablend.counterstrike.enums.GameState;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.utils.PlayerUtils;
import dev.danablend.counterstrike.utils.Utils;
import me.zombie_striker.qg.guns.Gun;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import static me.zombie_striker.qg.api.QualityArmory.getGunInHand;

public class EntityDamageByEntityListener implements Listener {


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            String world = damager.getWorld().getName();

            if (CounterStrike.i.HashWorlds != null) {
                Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

                if (md != null && !md.modoCs) {
                    return;
                }
            }

            if (CounterStrike.i.getGameState().equals(GameState.LOBBY)) {
                event.setCancelled(true);
                return;
            }

            if (event.getEntity() instanceof Player) {
                Player victim = (Player) event.getEntity();

                CSPlayer csShooter = CounterStrike.i.getCSPlayer(damager, false, null);
                CSPlayer csVictim = CounterStrike.i.getCSPlayer(victim, false, null);

                if (csShooter == null || csVictim == null) {
                    return;
                }

                if (csShooter.getTeam().equals(csVictim.getTeam()) && !Config.FRIENDLY_FIRE_ENABLED) {
                    event.setCancelled(true);
                    return;
                }

                //headshot values
                if (event.getDamage() >= 100d) {
                    return;
                }

                ItemStack gunItem = damager.getInventory().getItemInMainHand();

                if (gunItem == null) return;

                if (gunItem.equals(CounterStrike.i.getKnife())) {

                    //player stabded in the back, gives sure death, so using 40 case he has armour
                    if (PlayerUtils.playerBehindPlayer(damager, victim)) {
                        event.setDamage(40);
                    } else {
                        event.setDamage(event.getDamage() / 2);
                    }
                } else {

                    if (!CounterStrike.i.usingQualityArmory() || CounterStrike.i.modeRealms) return;

                    Weapon gun = Weapon.getByItem(gunItem);

                    //no IronSights
                    if (gun != null) {
                        event.setDamage(gun.getDamage());
                        Utils.debug("  NORMAL   >>>>>>>>>>>>  " + event.getDamage() + "     "+gun.getName());
                    } else {

                        Gun mygun = getGunInHand((HumanEntity) event.getDamager());

                        if (mygun != null && mygun.hasIronSights()) {

                            gunItem = damager.getInventory().getItemInOffHand();

                            if (gunItem != null) {
                                Weapon mygun1 = Weapon.getByName(gunItem.getItemMeta().getDisplayName());

                                Utils.debug(mygun1.getDisplayName() + "    "+ mygun1.getName() + "  debug shift damage   >>>>>>>>>>>>  " + event.getDamage() + "   to: " + mygun1.getDamage());
                                event.setDamage(mygun1.getDamage());
                            }

                        } else {
                            Utils.debug("  WTF   >>>>>>>>>>>>  " + event.getDamage() + "   with no gun1 or gun2");
                        }
                    }
                }
            }

            if (event.getEntity() instanceof Chicken && event.getEntity().isDead()) {
                CSPlayer csShooter = CounterStrike.i.getCSPlayer(damager, false, null);
                csShooter.setChickenKills(csShooter.getKills() + 1);
            }
        }

    }

}
