package dev.danablend.counterstrike.utils;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PacketUtils {

	/**
	 * 
	 * @param player
	 * @param titleText
	 * @param subtitleText
	 * @param fadeInDuration - in seconds
	 * @param duration - in seconds
	 * @param fadeOutDuration - in seconds
	 */
	public static void sendTitleAndSubtitle(Player player, String titleText, String subtitleText, int fadeInDuration, int duration, int fadeOutDuration) {
		player.sendTitle(titleText, subtitleText, 20*fadeInDuration, 20*duration, 20*fadeOutDuration);
	}
	
	/**
	 * 
	 * @param titleText
	 * @param subtitleText
	 * @param fadeInDuration - in seconds
	 * @param duration - in seconds
	 * @param fadeOutDuration - in seconds
	 */

	public static void sendTitleAndSubtitleToInGame(String titleText, String subtitleText, int fadeInDuration, int duration, int fadeOutDuration) {
		for (CSPlayer csplayer : CounterStrike.i.getCSPlayers()) {
			sendTitleAndSubtitle(csplayer.getPlayer(), titleText, subtitleText, fadeInDuration, duration, fadeOutDuration);
		}
	}

	public static void sendTitleAndSubtitleToWaitingInLobby(String titleText, String subtitleText, int fadeInDuration, int duration, int fadeOutDuration) {
		for (Player player : Bukkit.getOnlinePlayers()) {

			CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

			if (csplayer == null && InLocation(player)) {
				sendTitleAndSubtitle(player, titleText, subtitleText, fadeInDuration, duration, fadeOutDuration);
			}
		}
	}

	public static void sendTitleAndSubtitleToAll(String titleText, String subtitleText, int fadeInDuration, int duration, int fadeOutDuration) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			sendTitleAndSubtitle(player, titleText, subtitleText, fadeInDuration, duration, fadeOutDuration);
		}
	}



	public static void sendActionBarToInGame(String text, int duration) {
		for (CSPlayer csplayer : CounterStrike.i.getCSPlayers()) {
			sendActionBar(csplayer.getPlayer(), text, duration);
		}
	}

	public static void sendActionBarToWaitingInLobby(String text, int duration) {
		for (Player player : Bukkit.getOnlinePlayers()) {

			CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

			if (csplayer == null && InLocation(player)) {
				sendActionBar(player, text, duration);
			}
		}
	}

	public static void sendActionBarToAll(String text, int duration) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			sendActionBar(player, text, duration);
		}
	}

	public static void sendActionBar(Player player, String text, int duration) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
	}


	private static Boolean InLocation(Player p) {

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
