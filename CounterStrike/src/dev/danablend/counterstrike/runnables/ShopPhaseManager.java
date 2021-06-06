package dev.danablend.counterstrike.runnables;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;
import dev.danablend.counterstrike.database.Mundos;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ShopPhaseManager extends BukkitRunnable implements Listener {

    int duration;
    private CounterStrike plugin;

    public ShopPhaseManager(CounterStrike myplugin) {
        CounterStrike.i.gameState = GameState.SHOP;

        plugin = myplugin;
        duration = Config.SHOP_PHASE_DURATION;
        this.runTaskTimer(CounterStrike.i, 20L, 20L);
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        String mundo = player.getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

        if (csplayer == null) {
            return;
        }

        final Location from = e.getFrom();
        final Location to = e.getTo();

        if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
            e.setTo(e.getFrom());
        }
    }

    @Override
    public void run() {

        int serverSize = CounterStrike.i.getCSPlayers().size();

        if (serverSize == 0) {
            System.out.println("Aborting game, no players left..");

            CounterStrike.i.getTerroristsTeam().setLosses(0);
            CounterStrike.i.getTerroristsTeam().setWins(0);
            CounterStrike.i.getTerroristsTeam().setColour("WHITE");
            CounterStrike.i.getCounterTerroristsTeam().setLosses(0);
            CounterStrike.i.getCounterTerroristsTeam().setWins(0);
            CounterStrike.i.getCounterTerroristsTeam().setColour("WHITE");

            if (CounterStrike.i.gameCount == null) {
                CounterStrike.i.gameCount = new GameCounter(CounterStrike.i);
                CounterStrike.i.gameCount.runTaskTimer(CounterStrike.i, 40L, 200L);
            }

            CounterStrike.i.gameState = GameState.LOBBY;
            this.cancel();
            plugin.Shop = null;
            return;
        }

        String msg = Utils.color("&6The shop phase ends in &a" + duration + " second.");
        PacketUtils.sendActionBarToInGame(msg, 1);

        if (duration <= 0) {
            PacketUtils.sendActionBarToInGame(Utils.color("&6The shop phase has ended!"), 1);
            CounterStrike.i.setGameTimer(new GameTimer());

            plugin.gameState = GameState.RUN;
            this.cancel();
            plugin.Shop = null;
        }
        duration--;
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        HandlerList.unregisterAll(this);
    }
}
