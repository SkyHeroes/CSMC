package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.database.Worlds;
import dev.danablend.counterstrike.runnables.Bomb;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void blockPlaceEvent(BlockPlaceEvent event) {

        String world = event.getPlayer().getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        Block block = event.getBlock();
        Block blockUnder = block.getLocation().add(0, -1, 0).getBlock();
        Material bombMat = Config.BOMB_MATERIAL == null ? Material.BEDROCK : Config.BOMB_MATERIAL;

        if (block.getType().equals(Material.TNT) && blockUnder.getType().equals(bombMat)) {
            if (block.getLocation().distance(event.getPlayer().getLocation()) > 3) {
                event.setCancelled(true);
                return;
            }

            CSPlayer csplayer = CounterStrike.i.getCSPlayer(event.getPlayer(), false, null);

            if (csplayer != null) {
                //mvp
                csplayer.setMoney(csplayer.getMoney() + 300);
                event.getPlayer().sendMessage(ChatColor.GREEN + "+ $300");
                csplayer.settempMVP(csplayer.gettempMVP() + 2);
            }

            event.getPlayer().getInventory().getItemInMainHand().setType(Material.AIR);

            Object task = null;
            Bomb bomb = new Bomb(Config.BOMB_TIMER, block.getLocation());
            task = CounterStrike.i.myBukkit.runTaskTimer(null, null, null, () -> bomb.run(), 20L, 20L);
            bomb.setScheduledTask(task);

            return;
        }

        //so it doesn't let place any blocks except tnt at right place
        event.setCancelled(true);
    }

}
