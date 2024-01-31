package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.database.Mundos;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {

    @EventHandler
    public void playerItemDropEvent(PlayerDropItemEvent event) {

        String mundo = event.getPlayer().getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        if (!event.getPlayer().isOp()) {
            int currentSlot = event.getPlayer().getInventory().getHeldItemSlot();

            if (currentSlot == 0 || currentSlot == 1) {
                return;
            }

            event.setCancelled(true);
        }
    }

}
