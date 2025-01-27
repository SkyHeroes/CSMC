package dev.danablend.counterstrike.utils.compatibility;

import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import static org.bukkit.event.player.PlayerResourcePackStatusEvent.Status.DOWNLOADED;

public class MyBukkitPaperLegacy {

    public boolean isDownloded(PlayerResourcePackStatusEvent event) {

        if (!event.getStatus().equals(DOWNLOADED)) {
            return false;
        }
        return true;
    }
}
