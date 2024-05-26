package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.database.Mundos;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @author barpec12
 * created on 2020-05-24
 */
public class InventoryClickListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        String mundo = p.getPlayer().getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        e.setCancelled(true);
    }
}
