package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.database.Worlds;
import dev.danablend.counterstrike.enums.GameState;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.utils.CSUtil;
import dev.danablend.counterstrike.utils.PlayerUtils;
import dev.danablend.counterstrike.utils.Utils;
import me.zombie_striker.qg.guns.Gun;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import static me.zombie_striker.qg.api.QualityArmory.getGunInHand;

public class EntityDamageByEntityListener implements Listener {
    CounterStrike main = CounterStrike.i;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            String world = damager.getWorld().getName();

            if (main.HashWorlds != null) {
                Worlds md = (Worlds) main.HashWorlds.get(world);

                if (md != null && !md.modoCs) {
                    return;
                }
            }

            if (main.getGameState().equals(GameState.LOBBY)) {
                event.setCancelled(true);
                return;
            }

            if (event.getEntity() instanceof Player) {
                Player victim = (Player) event.getEntity();

                CSPlayer csShooter = main.getCSPlayer(damager, false, null);
                CSPlayer csVictim = main.getCSPlayer(victim, false, null);

                //envolvidos jodagores de fora do CS
                if (csShooter == null || csVictim == null) return;

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

                if (gunItem.equals(main.getKnife())) {

                    //player stabded in the back, gives sure death, so using 40 case he has armour
                    if (PlayerUtils.playerBehindPlayer(damager, victim)) {
                        event.setDamage(40);
                    } else {
                        event.setDamage(event.getDamage() / 2);
                    }
                } else {

                    if (!main.usingQualityArmory() || main.modeRealms) return;

                    Weapon gun = Weapon.getByItem(gunItem);

                    //no IronSights
                    if (gun != null) {
                        event.setDamage(gun.getDamage());
                        Utils.debug("  NORMAL   >>>>>>>>>>>>  " + event.getDamage() + "     " + gun.getName());
                    } else {

                        Gun mygun = getGunInHand((HumanEntity) event.getDamager());

                        if (mygun != null && mygun.hasIronSights()) {

                            gunItem = damager.getInventory().getItemInOffHand();

                            if (gunItem != null) {
                                Weapon mygun1 = Weapon.getByName(gunItem.getItemMeta().getDisplayName());

                                Utils.debug(mygun1.getDisplayName() + "    " + mygun1.getName() + "  debug shift damage   >>>>>>>>>>>>  " + event.getDamage() + "   to: " + mygun1.getDamage());
                                event.setDamage(mygun1.getDamage());
                            }

                        } else {
                            Utils.debug("  WTF   >>>>>>>>>>>>  " + event.getDamage() + "   with no gun1 or gun2");
                        }
                    }
                }
            }

            if (event.getEntity() instanceof Chicken && event.getEntity().isDead()) {
                CSPlayer csShooter = main.getCSPlayer(damager, false, null);
                csShooter.setChickenKills(csShooter.getKills() + 1);
            }

        } else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();

            if (event.getEntity() instanceof Player && proj.getShooter() instanceof Player) {
                Player attacker = (Player) proj.getShooter();
                Player player = (Player) event.getEntity();

                CSPlayer csShooter = main.getCSPlayer(attacker, false, null);
                CSPlayer csVictim = main.getCSPlayer(player, false, null);

                //envolvidos jodagores de fora do CS
                if (csShooter == null || csVictim == null) return;

                if (csShooter.getTeam().equals(csVictim.getTeam()) && !Config.FRIENDLY_FIRE_ENABLED) {
                    event.setCancelled(true);
                    return;
                }

                boolean isHeadShot = CSUtil.isHeadShot(proj, player);

                if (isHeadShot) {
                    event.setDamage(40);
                    attacker.playSound(attacker.getLocation(), Sound.BLOCK_LAVA_POP, 6, 1);
                }
            }
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void ProcessaAtaqueFlecha(EntityShootBowEvent event) {

        if (main.modeRealms || !main.usingQualityArmory()) {

            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();

                String world = player.getWorld().getName();

                if (main.HashWorlds != null) {
                    Worlds md = (Worlds) main.HashWorlds.get(world);

                    if (md != null && !md.modoCs) {
                        return;
                    }
                }

                String nome = main.myBukkit.getNameFromItemStack(event.getBow());

                if (main.getGameState().equals(GameState.LOBBY) || nome == null) {
                    return;
                }

                if (nome.contains("Laser Bow")) {
                    Entity seta = event.getProjectile();
                    Vector velocidade = seta.getVelocity();
                    velocidade.multiply(2.5);
                    seta.setVelocity(velocidade);
                }
            }
        }
    }
}


