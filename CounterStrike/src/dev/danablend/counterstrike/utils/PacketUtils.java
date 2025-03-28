package dev.danablend.counterstrike.utils;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class PacketUtils {

    /**
     * @param player
     * @param titleText
     * @param subtitleText
     * @param fadeInDuration  - in seconds
     * @param duration        - in seconds
     * @param fadeOutDuration - in seconds
     */
    public static void sendTitleAndSubtitle(Player player, String titleText, String subtitleText, int fadeInDuration, int duration, int fadeOutDuration) {
        if (!CounterStrike.i.showGameStatusTitle) return;

        player.sendTitle(titleText, subtitleText, 20 * fadeInDuration, 20 * duration, 20 * fadeOutDuration);
    }

    /**
     * @param titleText
     * @param subtitleText
     * @param fadeInDuration  - in seconds
     * @param duration        - in seconds
     * @param fadeOutDuration - in seconds
     */

    public static void sendTitleAndSubtitleToInGame(String titleText, String subtitleText, int fadeInDuration, int duration, int fadeOutDuration) {
        for (CSPlayer csplayer : CounterStrike.i.getCSPlayers()) {
            if (!csplayer.isNPC())
                sendTitleAndSubtitle(csplayer.getPlayer(), titleText, subtitleText, fadeInDuration, duration, fadeOutDuration);
        }
    }

    public static void sendTitleAndSubtitleToWaitingInLobby(String titleText, String subtitleText, int fadeInDuration, int duration, int fadeOutDuration) {
        for (Player player : Bukkit.getOnlinePlayers()) {

            CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

            if (csplayer == null && isInLobbyLocation(player)) {
                sendTitleAndSubtitle(player, titleText, subtitleText, fadeInDuration, duration, fadeOutDuration);
            }
        }
    }

    public static void sendTitleAndSubtitleToAll(String titleText, String subtitleText, int fadeInDuration, int duration, int fadeOutDuration) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitleAndSubtitle(player, titleText, subtitleText, fadeInDuration, duration, fadeOutDuration);
        }
    }


    public static void sendActionBarToInGame(String text) {
        for (CSPlayer csplayer : CounterStrike.i.getCSPlayers()) {
            if (!csplayer.isNPC()) sendActionBar(csplayer.getPlayer(), text);
        }
    }

    public static void sendActionBarToWaitingInLobby(String text) {
        for (Player player : Bukkit.getOnlinePlayers()) {

            CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

            if (csplayer == null && isInLobbyLocation(player)) {
                sendActionBar(player, text);
            }
        }
    }

    public static void sendActionBarToAll(String text) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBar(player, text);
        }
    }

    public static void sendActionBar(Player player, String text1) {
        CounterStrike.i.myBukkit.sendActionBar(player, text1);
    }


    private static Boolean isInLobbyLocation(Player p) {
        if (CounterStrike.i.getLobbyLocation() == null) return false;

        Location locRaw = CounterStrike.i.getLobbyLocation();

        Integer xx = locRaw.getBlockX();
        Integer zz = locRaw.getBlockZ();
        Location loc = p.getLocation();
        Integer x = loc.getBlockX();
        Integer z = loc.getBlockZ();

        if (x > (xx - 20) && x < (xx + 20)) {
            if (z > (zz - 20) && z < (zz + 20)) {
                return true;
            }
        }

        return false;
    }

}
