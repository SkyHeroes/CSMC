package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.database.Mundos;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerItemDamageListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void playerItemDamageEvent(PlayerItemDamageEvent event) {

        String mundo = event.getPlayer().getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

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
