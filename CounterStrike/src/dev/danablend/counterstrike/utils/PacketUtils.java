package dev.danablend.counterstrike.utils;

import org.bukkit.Bukkit;
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
	public static void sendTitleAndSubtitleToAll(String titleText, String subtitleText, int fadeInDuration, int duration, int fadeOutDuration) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			sendTitleAndSubtitle(player, titleText, subtitleText, fadeInDuration, duration, fadeOutDuration);
		}
	}

	public static void sendActionBar(Player player, String text, int duration) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
	}

	public static void sendActionBarToAll(String text, int duration) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			sendActionBar(player, text, duration);
		}
	}

}
