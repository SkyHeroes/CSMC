package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.database.Worlds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class PlayerItemDamageListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void playerItemDamageEvent(PlayerItemDamageEvent event) {

        String world = event.getPlayer().getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        event.setCancelled(true);
//        Damageable meta = (Damageable) event.getItem().getItemMeta();
//        meta.setDamage(meta.getDamage() - 2);
//        event.getItem().setItemMeta((ItemMeta) meta);
    }

}
