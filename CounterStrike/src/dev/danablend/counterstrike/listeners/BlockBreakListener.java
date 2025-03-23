package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.enums.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.database.Worlds;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;


public class BlockBreakListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void blockBreakEvent(BlockBreakEvent event) {

        String world = event.getPlayer().getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        CSPlayer csplayer = CounterStrike.i.getCSPlayer(event.getPlayer(), false, null);

        //if game is not in bomb planted state, any player can break tnt from the maps
        if (csplayer != null && !CounterStrike.i.getGameState().equals(GameState.PLANTED) && event.getBlock().getType().equals(Material.TNT)) {
            CounterStrike.i.myBukkit.runTaskLater(null, event.getBlock().getLocation(), null, () -> {
                event.getBlock().setType(Material.AIR);
            }, 1);
        }

        event.setCancelled(true);
    }

}
