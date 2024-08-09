package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.database.Worlds;
import dev.danablend.counterstrike.enums.Weapon;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author barpec12
 * created on 2020-05-20
 */
public class EntityPickupItemListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(EntityPickupItemEvent e) {

        String world = e.getEntity().getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        ItemStack item = e.getItem().getItemStack();

        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            CSPlayer cp = CounterStrike.i.getCSPlayer(p, false, null);

            if (cp == null) {
                return;
            }

            if (cp.getTeam().equals(TeamEnum.TERRORISTS) && item.getType().equals(Material.TNT)) {
                //can continue
                return;
            } else if (cp.getTeam().equals(TeamEnum.COUNTER_TERRORISTS) && item.getType().equals(Material.TNT)) {
                e.setCancelled(true);
            }

            if (Weapon.isWeapon(item)) {
                Weapon weapon = Weapon.getByItem(item);

                switch (weapon.getWeaponType()) {
                    case RIFLE:
                        if (p.getInventory().getItem(0) == null) {
                            e.getItem().remove();
                            p.getInventory().setItem(0, item);
                        }
                        e.setCancelled(true);
                        break;

                    case PISTOL:
                        if (p.getInventory().getItem(1) == null) {
                            e.getItem().remove();
                            p.getInventory().setItem(1, item);
                        }
                        e.setCancelled(true);
                        break;

                    case GRENADE:
                        break;

                    default:
                        e.setCancelled(true);
                        break;
                }

            }
            else if (item.getType() == Material.IRON_AXE) {
                return;
            }
            else e.setCancelled(true); //other type of stuff
        }
    }
}
