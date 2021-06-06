package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.database.Mundos;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import dev.danablend.counterstrike.CounterStrike;


public class BlockBreakListener implements Listener {

    @EventHandler
    public void blockBreakEvent(BlockBreakEvent event) {

        String mundo = event.getPlayer().getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        event.setCancelled(true);
    }

}
