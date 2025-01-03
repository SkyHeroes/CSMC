package dev.danablend.counterstrike.runnables;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.ChatColor;

import java.util.Collection;


public class GameStarter {

    private int timeToStart;
    private CounterStrike plugin;
    private Collection<CSPlayer> csPlayers;
    private Object task;

    public GameStarter(CounterStrike plugin) {
        this.plugin = plugin;
        csPlayers = plugin.getCSPlayers();

        if (plugin.getTerroristsTeam().getWins() + plugin.getTerroristsTeam().getLosses() == 0) {
            plugin.LoadDBRandomMaps();
            timeToStart = 20;
        } else {

            if (plugin.randomMaps) {
                plugin.LoadDBRandomMaps();
            }

            timeToStart = 6;
        }
    }


    public void setScheduledTask(Object task) {
        this.task = task;
    }


    public void run() {

        int serverSize = CounterStrike.i.getCSPlayers().size();

        if ((serverSize == 0 || plugin.getServer().getOnlinePlayers().size() == 0) && plugin.quitExitGame ) {
            Utils.debug("Aborting start counter, no players left");

            plugin.myBukkit.cancelTask(task);
            CounterStrike.i.FinishGame(CounterStrike.i.getTerroristsTeam(),CounterStrike.i.getCounterTerroristsTeam());

            return;
        }

        if (timeToStart <= 0) {
            plugin.startGame();
            plugin.myBukkit.cancelTask(task);
            return;
        }

        PacketUtils.sendTitleAndSubtitleToInGame(ChatColor.YELLOW + "The game will start in " + timeToStart + " seconds!", ChatColor.YELLOW + "get ready", 0, 0, 1);
        PacketUtils.sendTitleAndSubtitleToWaitingInLobby(ChatColor.RED + "The game will start in " + timeToStart + " seconds!", ChatColor.YELLOW + "you can still join", 0, 0, 1);

        timeToStart--;
    }

}
