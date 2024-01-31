package dev.danablend.counterstrike.shop;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.database.Mundos;
import dev.danablend.counterstrike.enums.Weapon;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ShopListener implements Listener {

    private CounterStrike plugin;

    public ShopListener(CounterStrike myplugin) {
        plugin = myplugin;
    }


    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        String mundo = player.getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

            if (md == null || !md.modoCs) {
                return;
            }
        }

        CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

        if (csplayer == null) {
            return;
        }

        PlayerInventory inv = player.getInventory();

        if (player.getGameMode().equals(GameMode.SPECTATOR)) {
            event.setCancelled(true);
            return;
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

            // Gun checker
            ItemStack gunItem = inv.getItemInMainHand();

            // Shop Item Checker
            if (inv.getItemInMainHand().equals(CounterStrike.i.getShopItem())) {

                if (plugin.getGameState() != GameState.SHOP) {
                    player.sendMessage(ChatColor.RED + "Sorry, not in ShopPhase.");
                    return;
                }

                if (csplayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
                    Shop.getShop().openCounterTerroristShop(player);
                } else {
                    Shop.getShop().openTerroristShop(player);
                }
            }
        }
        if (!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.TNT))
            event.setCancelled(true);
    }

    //chest selection
    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {

        String mundo = event.getWhoClicked().getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

            if (md == null || !md.modoCs) {
                return;
            }
        }

        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();

            if (plugin.getGameState() != GameState.SHOP) {
                player.sendMessage(ChatColor.RED + "Sorry, not in ShopPhase.");
                return;
            }

            ItemStack clicked = event.getCurrentItem();

            if (event.getView().getTitle().equals(Config.terroristShopName)) {
                event.setCancelled(true);

                Weapon gun = Weapon.getByItem(clicked);

                if (gun != null) {
                    Shop.getShop().purchaseShopItem(player, gun);
                }
            } else if (event.getView().getTitle().equals(Config.counterTerroristShopName)) {
                event.setCancelled(true);
                Weapon gun = Weapon.getByItem(clicked);
                if (gun != null) {
                    Shop.getShop().purchaseShopItem(player, gun);
                }
            }
        }
    }
}
