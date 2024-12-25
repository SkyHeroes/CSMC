package dev.danablend.counterstrike.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public class MyBukkitPaper {

     MyBukkitPaper() {
    }

    public void hologramCustomName(ArmorStand hologram, String message) {
        hologram.customName( Component.text(message));
    }

    public void metaDisplayName(ItemMeta meta, String text) {
        meta.displayName( Component.text(text));
    }

    public void playerListName(Player player, String message) {
        player.playerListName( Component.text(message));
    }

    public void playerSendActionBar(Player player, String message) {
        player.sendActionBar(Component.text(message));
    }

}
