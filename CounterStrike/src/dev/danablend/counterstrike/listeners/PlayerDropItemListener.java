package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.database.Worlds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import static dev.danablend.counterstrike.CounterStrike.*;

public class PlayerDropItemListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void playerItemDropEvent(PlayerDropItemEvent event) {

        String world = event.getPlayer().getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

            if (md != null && !md.modoCs) {
                return;
            }
        } else {
            return;
        }

        if (!event.getPlayer().isOp()) {
            int currentSlot = event.getPlayer().getInventory().getHeldItemSlot();

            //can't drop knife or shop if active
            if (currentSlot == KNIFE_SLOT || currentSlot == SHOP_SLOT) {
                event.setCancelled(true);
            }
        }
    }

}
