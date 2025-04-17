package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.database.Worlds;
import dev.danablend.counterstrike.enums.GameState;
import dev.danablend.counterstrike.runnables.Bomb;
import dev.danablend.counterstrike.utils.PacketUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static dev.danablend.counterstrike.Config.MAX_PLAYERS;
import static dev.danablend.counterstrike.Config.MAX_ROUNDS;

public class PlayerInteractListener implements Listener {

    private CounterStrike plugin;

    public PlayerInteractListener() {
        this.plugin = CounterStrike.i;
    }


     @EventHandler(ignoreCancelled = true)
    public void onPlayerDefuseEvent(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        String world = player.getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

        if (csplayer != null && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

            if (Bomb.bomb == null) return;

            if (!csplayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) return;

            Bomb bomb = Bomb.bomb;

            bomb.defuse(csplayer);
        }
    }

}

