package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.database.Mundos;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

/**
 * @author barpec12
 * created on 2020-05-19
 */
public class BlockIgniteListener implements Listener {

    @EventHandler
    public void onBlockIgniteEvent(BlockIgniteEvent e) {
        Player player = e.getPlayer();

        if (player != null) { //might have other origins
            String mundo = player.getWorld().getName();

            if (CounterStrike.i.HashWorlds != null) {
                Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

                if (md != null && !md.modoCs) {
                    return;
                }
            }

            if (e.getBlock().getType().equals(Material.TNT)) {
                e.setCancelled(true);
            }
        }
    }
}
