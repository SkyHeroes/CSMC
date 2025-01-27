package dev.danablend.counterstrike.utils.compatibility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public class MyBukkitPaper {

    public MyBukkitPaper() {
    }

    public void hologramCustomName(ArmorStand hologram, String message) {
        hologram.customName(Component.text(message));
    }

    public void metaDisplayName(ItemMeta meta, String text) {
        meta.displayName(Component.text(text));
    }

    public void playerListName(Player player, String message) {
        player.playerListName(Component.text(message));
    }

    public void playerSendActionBar(Player player, String message) {
        player.sendActionBar(Component.text(message));
    }

    public void consoleSendMessage(String text, String textComponent, NamedTextColor color) {
        TextComponent component = Component.text(textComponent, color);

        NamedTextColor intColor = NamedTextColor.GRAY;

        Bukkit.getConsoleSender().sendMessage(net.kyori.adventure.text.Component
                .text(text)
                .color(intColor)
                .append(component)
        );
    }

}
