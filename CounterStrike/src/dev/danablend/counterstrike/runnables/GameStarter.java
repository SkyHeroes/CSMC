package dev.danablend.counterstrike.runnables;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.utils.JSONMessage;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.database.Mundos;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;


public class GameStarter extends BukkitRunnable {

    CounterStrike plugin;
    Collection<CSPlayer> csPlayers;
    public int timeToStart;

    public GameStarter(CounterStrike plugin) {
        this.plugin = plugin;
        csPlayers = plugin.getCSPlayers();

        if (plugin.getTerroristsTeam().getWins() + plugin.getTerroristsTeam().getLosses() == 0) {
            plugin.LoadDBRandomConfigs();
            timeToStart = 15;
        } else {
            timeToStart = 6;
        }

        if (timeToStart == 15) {
            for (CSPlayer csplayer : CounterStrike.i.getCSPlayers()) {
                Player player = csplayer.getPlayer();

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

                if (CounterStrike.i.HashWorlds != null) {
                    String mundo = player.getWorld().getName();
                    Mundos md = (Mundos) plugin.HashWorlds.get(mundo);

                    if (md == null || !md.modoCs) {
                        continue;
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        if (timeToStart <= 0) {
            plugin.startGame();
            this.cancel();
            return;
        }

        PacketUtils.sendTitleAndSubtitleToInGame(ChatColor.YELLOW + "The game will start in " + timeToStart + " seconds!", ChatColor.YELLOW + "get ready", 0, 0, 1);

        PacketUtils.sendTitleAndSubtitleToWaitingInLobby(ChatColor.RED + "The game will start in " + timeToStart + " seconds!", ChatColor.YELLOW + "you can still join", 0, 0, 1);

        timeToStart--;
    }

}
