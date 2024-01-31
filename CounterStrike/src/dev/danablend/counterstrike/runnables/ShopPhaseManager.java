package dev.danablend.counterstrike.runnables;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.database.Mundos;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ShopPhaseManager implements Listener {

    int duration;
    private CounterStrike plugin;
    private Object task;

    public ShopPhaseManager(CounterStrike myplugin) {
        CounterStrike.i.gameState = GameState.SHOP;

        plugin = myplugin;
        duration = Config.SHOP_PHASE_DURATION;
        //this.runTaskTimer(CounterStrike.i, 20L, 20L);
        Location local = myplugin.getLobbyLocation();

        task = plugin.myBukkit.runTaskTimer(null, local, null, () -> run(), 20L, 20L);
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent e) {

        if (CounterStrike.i.gameState != GameState.SHOP) return;

        Player player = e.getPlayer();

        String mundo = player.getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

            if (md != null && !md.modoCs) {
                Utils.debug("Not a CS Map, aborting");
                return;
            }
        }

        CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

        if (csplayer == null) {
            Utils.debug("Not a player, aborting");
            return;
        }

        final Location from = e.getFrom();
        final Location to = e.getTo();

        if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
            e.setTo(e.getFrom());
        }
    }


    public void run() {

        int serverSize = CounterStrike.i.getCSPlayers().size();

        if (serverSize == 0) {
            Utils.debug("Aborting game, no players left..");

            CounterStrike.i.getTerroristsTeam().setLosses(0);
            CounterStrike.i.getTerroristsTeam().setWins(0);
            CounterStrike.i.getTerroristsTeam().setColour("WHITE");
            CounterStrike.i.getCounterTerroristsTeam().setLosses(0);
            CounterStrike.i.getCounterTerroristsTeam().setWins(0);
            CounterStrike.i.getCounterTerroristsTeam().setColour("WHITE");

            //plugin.StartGameCounter(0);

            CounterStrike.i.gameState = GameState.LOBBY;

//            if (plugin.myBukkit.isFolia())
//                ((ScheduledTask) task).cancel();
//            else
//                ((BukkitTask) task).cancel();

            plugin.myBukkit.cancelTask(task);

            plugin.Shop = null;
            return;
        }

        String msg = Utils.color("&6The shop phase ends in &a" + duration + " second.");
        PacketUtils.sendActionBarToInGame(msg);

        if (duration <= 0) {
            PacketUtils.sendActionBarToInGame(Utils.color("&6The shop phase has ended!"));
            CounterStrike.i.setGameTimer(new GameTimer());

            plugin.gameState = GameState.RUN;

//            if (plugin.myBukkit.isFolia())
//                ((ScheduledTask) task).cancel();
//            else
//                ((BukkitTask) task).cancel();

            plugin.myBukkit.cancelTask(task);

            plugin.Shop = null;
        }
        duration--;
    }

}
