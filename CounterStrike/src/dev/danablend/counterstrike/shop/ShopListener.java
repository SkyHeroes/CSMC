package dev.danablend.counterstrike.shop;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.database.Worlds;
import dev.danablend.counterstrike.enums.Weapon;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
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


    @EventHandler(ignoreCancelled = true)
    public void playerObjectInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        String world = player.getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

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

        ItemStack inHandItem = inv.getItemInMainHand();

        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

            // Shop Item Checker
            if (inHandItem.equals(CounterStrike.i.getShopItem())) {

                if (plugin.getGameState() != GameState.SHOP) {
                    player.sendMessage(ChatColor.RED + "Sorry, not in ShopPhase.");
                    return;
                }

                if (csplayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
                    Shop.getShop().openCounterTerroristShop(player);
                } else {
                    Shop.getShop().openTerroristShop(player);
                }

            } else {
                Action a = event.getAction();
                Block block = event.getClickedBlock();
                if (block == null) return;

                if (a.equals(Action.RIGHT_CLICK_BLOCK)) {

                    Material mat = block.getType();

                    if (mat == Material.CHEST || mat == Material.SHULKER_BOX) {
                        event.setCancelled(true);

                    } else if (mat.toString().contains("DOOR") || mat.toString().contains("BUTTON") || mat.toString().contains("PLATE") || mat == Material.LEVER) {
                        event.setCancelled(true);
                    }

                } else if (a.equals(Action.LEFT_CLICK_BLOCK) && event.getHand() != null) {
                    event.setCancelled(true);
                }
            }
        }

    }

    //chest selection
    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {

        String world = event.getWhoClicked().getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

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

            if (!event.getClickedInventory().getType().toString().equals("CHEST")) {
                event.setCancelled(true);
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
