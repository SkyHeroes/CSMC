package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.events.WeaponFireEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class WeaponFireListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void weaponFireEvent(WeaponFireEvent event) {

        Player player = event.getPlayer();
        ItemStack gunItem = player.getInventory().getItemInMainHand();

        if (gunItem.getAmount() - 1 <= 0) {
            CSPlayer csplayer = CounterStrike.i.getCSPlayer(event.getPlayer(), false, null);

            if (csplayer == null) {
                return;
            }
            csplayer.reload(event.getPlayer().getInventory().getItemInMainHand());
            return;
        }
        gunItem.setAmount(gunItem.getAmount() - 1);
    }

}
