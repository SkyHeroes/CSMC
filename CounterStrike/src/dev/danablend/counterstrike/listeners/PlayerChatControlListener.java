package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.database.Worlds;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerChatControlListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(@NotNull AsyncPlayerChatEvent chat) {

        Player player = chat.getPlayer();
        String world = player.getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        //not playing so ok
        if (!CounterStrike.i.getPlayerUpdater().playersWithScoreboard.contains(player.getUniqueId())) {
            return;
        }

        //dead players don't talk
        if (player.getGameMode() == GameMode.SPECTATOR) {
            chat.setCancelled(true);
        }
    }

}
