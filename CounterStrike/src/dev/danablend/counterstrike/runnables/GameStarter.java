package dev.danablend.counterstrike.runnables;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;


public class GameStarter {

    public int timeToStart;
    CounterStrike plugin;
    Collection<CSPlayer> csPlayers;
    Object task;

    public GameStarter(CounterStrike plugin) {
        this.plugin = plugin;
        csPlayers = plugin.getCSPlayers();

        //opcao se gera sempre ou nao???
        if (plugin.getTerroristsTeam().getWins() + plugin.getTerroristsTeam().getLosses() == 0) {
            plugin.LoadDBRandomMaps();
            timeToStart = 15;
        } else {

            if (plugin.randomMaps) {
                plugin.LoadDBRandomMaps();
            }
            else Utils.debug("#####  Not using random maps... ");

            timeToStart = 6;
        }

//        if (timeToStart == 15) {
//            for (CSPlayer csplayer : CounterStrike.i.getCSPlayers()) {
//                Player player = csplayer.getPlayer();
//
//                JSONMessage.create("Confirm")
//                        .color(ChatColor.GREEN)
//                        .tooltip("Click to confirm join")
//                        .runCommand("/csmc Ok")
//                        .then(" or ")
//                        .color(ChatColor.GRAY)
//                        .style(ChatColor.BOLD)
//                        .then("Abort")
//                        .color(ChatColor.RED)
//                        .tooltip("Click to leave CSMC game")
//                        .runCommand("/csmc leave")
//                        .send(player);
//            }
//        }
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
