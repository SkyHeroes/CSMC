package dev.danablend.counterstrike.runnables;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.database.Worlds;
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
        CounterStrike.i.setGameState( GameState.SHOP);

        plugin = myplugin;
        duration = Config.SHOP_PHASE_DURATION;
        Location local = myplugin.getLobbyLocation();

        task = plugin.myBukkit.runTaskTimer(null, local, null, () -> run(), 20L, 20L);
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

            CounterStrike.i.setGameState(GameState.LOBBY);
            plugin.myBukkit.cancelTask(task);

            plugin.Shop = null;
            return;
        }

        String msg = Utils.color("&6The shop phase ends in &a" + duration + " second.");
        PacketUtils.sendActionBarToInGame(msg);

        if (duration <= 0) {
            PacketUtils.sendActionBarToInGame(Utils.color("&6The shop phase has ended!"));
            CounterStrike.i.setGameTimer(new GameTimer());

            plugin.setGameState(GameState.RUN);
            plugin.myBukkit.cancelTask(task);
            plugin.Shop = null;
        }
        duration--;
    }

}
