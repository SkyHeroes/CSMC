package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.database.Mundos;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;


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

        if (event.getBlock().getType() == Material.TNT && event.getPlayer().isOp()) {
            CounterStrike.i.myBukkit.runTaskLater(null, event.getBlock().getLocation(), null, () -> {
                event.getBlock().setType(Material.AIR);
            }, 1);
        }

        event.setCancelled(true);
    }

}
