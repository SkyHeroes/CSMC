package dev.danablend.counterstrike.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;

public class PlayerJoinListener implements Listener {
	
	CounterStrike plugin;
	FileConfiguration config;
	
	public PlayerJoinListener() {
		this.plugin = CounterStrike.i;
		this.config = plugin.getConfig();
	}
	
	@EventHandler
	public void playerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.getInventory().clear();
		if(plugin.getGameState().equals(GameState.STARTED)) {
			CSPlayer csplayer = new CSPlayer(plugin, player);
			player.setGameMode(GameMode.SPECTATOR);
			if(csplayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
				csplayer.getPlayer().teleport(plugin.getCounterTerroristSpawn());
			}
			else if(csplayer.getTeam().equals(TeamEnum.TERRORISTS)) {
				csplayer.getPlayer().teleport(plugin.getTerroristSpawn());
			}
		}
		if(config.contains("lobby-location") && plugin.getGameState().equals(GameState.LOBBY)) {
			Location lobbyLoc = plugin.getLobbyLocation();
			player.teleport(lobbyLoc);
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.setFoodLevel(6);
			player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		}
	}
}
