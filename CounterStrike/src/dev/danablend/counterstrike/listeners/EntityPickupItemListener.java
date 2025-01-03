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
            Player player = (Player) e.getEntity();
            CSPlayer cp = CounterStrike.i.getCSPlayer(player, false, null);

            if (cp == null) {
                return;
            }

            if (cp.getTeam().equals(TeamEnum.TERRORISTS) && item.getType().equals(Material.TNT)) {

                e.getItem().remove();

                if (player.getInventory().getItem(4) == null) {
                    item.setAmount(1);
                    player.getInventory().setItem(4, item);
                }
                e.setCancelled(true); //to cancel event, if has bomb doesn't get another, if has none already picked it up
                return;

            } else if (cp.getTeam().equals(TeamEnum.COUNTER_TERRORISTS) && item.getType().equals(Material.TNT)) {
                e.setCancelled(true);
                return;
            }

            if (Weapon.isWeapon(item)) {
                Weapon weapon = Weapon.getByItem(item);

                switch (weapon.getWeaponType()) {
                    case RIFLE:
                        if (player.getInventory().getItem(0) == null) {
                            e.getItem().remove();
                            player.getInventory().setItem(0, item);
                        }
                        e.setCancelled(true); //to cancel event, if has Rifle doesn't get another, if has none already picked it up
                        break;

                    case PISTOL:
                        if (player.getInventory().getItem(1) == null) {
                            e.getItem().remove();
                            player.getInventory().setItem(1, item);
                        }
                        e.setCancelled(true); //to cancel event, if has PISTOL doesn't get another, if has none already picked it up
                        break;

                    case GRENADE:

                        if (player.getInventory().getItem(3) == null) {
                            e.getItem().remove();
                            player.getInventory().setItem(3, item);
                        } else {
                            ItemStack items = player.getInventory().getItem(3);

                            if (items.getItemMeta().getDisplayName().equals(item.getItemMeta().getDisplayName())) {
                                e.getItem().remove();
                                items.setAmount(item.getAmount() + item.getAmount());
                                player.getInventory().setItem(3, items);
                            } else {
                               // player.sendMessage("Unmatch grenade. Discarding...");
                            }
                        }
                        e.setCancelled(true);
                        break;

                    default:
                        e.setCancelled(true);
                        break;
                }

            } else if (item.getType() == Material.IRON_AXE) {
                if (player.getInventory().getItem(2) == null) {
                    e.getItem().remove();
                    player.getInventory().setItem(2, item);
                }
                e.setCancelled(true); //to cancel event, if has AXE doesn't get another, if has none already picked it up

            } else e.setCancelled(true); //other type of stuff
        }
    }
}
