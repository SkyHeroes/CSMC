package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.database.Mundos;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.utils.CSUtil;
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

        String mundo = e.getEntity().getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        ItemStack item = e.getItem().getItemStack();
        e.setCancelled(true);

        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            CSPlayer cp = CounterStrike.i.getCSPlayer(p, false, null);

            if (cp == null) {
                return;
            }

            if (cp.getTeam().equals(TeamEnum.TERRORISTS) && item.getType().equals(Material.TNT)) {
                e.getItem().remove();
                p.getInventory().setItem(4, CSUtil.getBombItem());
                return;
            }

            if (Weapon.isWeapon(item)) {
                Weapon weapon = Weapon.getByItem(item);
                switch (weapon.getWeaponType()) {
                    case RIFLE:
                        if (p.getInventory().getItem(0) == null) {
                            p.getInventory().setItem(0, item);
                            e.getItem().remove();
                        }
                        e.setCancelled(true);
                        break;
                    case PISTOL:
                        if (p.getInventory().getItem(1) == null) {
                            p.getInventory().setItem(1, item);
                            e.getItem().remove();
                        }
                        e.setCancelled(true);
                        break;
                    case GRENADE:
                        break;
                }

            }
        }
    }
}
