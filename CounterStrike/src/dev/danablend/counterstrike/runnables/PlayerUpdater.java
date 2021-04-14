package dev.danablend.counterstrike.runnables;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.utils.CSUtil;
import dev.danablend.counterstrike.utils.PacketUtils;

public class PlayerUpdater extends BukkitRunnable {

	CounterStrike plugin;
	Collection<Player> playersWithScoreboard;
	

	public PlayerUpdater(CounterStrike plugin) {
		this.plugin = plugin;
		this.playersWithScoreboard = new ArrayList<>();
	}

	public void run() {
		if(!Config.GAME_ENABLED) {
			this.cancel();
			return;
		}
		for (CSPlayer csplayer : plugin.getCSPlayers()) {
			csplayer.update();
			if(!playersWithScoreboard.contains(csplayer.getPlayer())) {
				setScoreBoard(csplayer.getPlayer());
				playersWithScoreboard.add(csplayer.getPlayer());
			}
			updateScoreBoard(csplayer.getPlayer());
		}
		if (plugin.getGameState() == GameState.STARTED) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if(CSUtil.isOutOfShopZone(player)) {
					player.getInventory().remove(CounterStrike.i.getShopItem());
				}
				else {
					if(player.getInventory().getItem(8) == null)
					player.getInventory().setItem(8, CounterStrike.i.getShopItem());
				}
				if (player.getGameMode().equals(GameMode.SPECTATOR)) {
					if (player.getSpectatorTarget() != null) {
						PacketUtils.sendActionBar(player, ChatColor.YELLOW + "Spectating: " + ChatColor.GREEN + player.getSpectatorTarget().getName(), 1);
					}
				}
			}
		}
	}
	
	public void setScoreBoard(Player player){
		CSPlayer csplayer = CounterStrike.i.getCSPlayer(player);
		
		Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = board.registerNewObjective("counterStrike", "dummy", "counterStrike");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.YELLOW + "---Counter Strike v" + CounterStrike.i.getDescription().getVersion() + "---");
		
		// Teams to hide name tags
//		ct.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
		Team t = board.registerNewTeam("t");
		Team ct = board.registerNewTeam("ct");
		t.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
		ct.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);

		for(CSPlayer p : CounterStrike.i.getTerroristsTeam().getCsPlayers()) {
			t.addEntry(p.getPlayer().getName());
		}
		for(CSPlayer p : CounterStrike.i.getCounterTerroristsTeam().getCsPlayers()) {
			ct.addEntry(p.getPlayer().getName());
		}
		
		// Display the money
		Team moneyCounter = board.registerNewTeam("moneyCounter");
		moneyCounter.addEntry(ChatColor.AQUA.toString());
		moneyCounter.setPrefix(ChatColor.YELLOW + "Money: ");
		moneyCounter.setSuffix(ChatColor.GREEN + "$" + csplayer.getMoney());
		obj.getScore(ChatColor.AQUA.toString()).setScore(15);
		
		// Display the kills
		Team killCounter = board.registerNewTeam("killCounter");
		killCounter.addEntry(ChatColor.BLACK.toString());
		killCounter.setPrefix(ChatColor.YELLOW + "Kills: ");
		killCounter.setSuffix(ChatColor.GREEN + "" + csplayer.getKills());
		obj.getScore(ChatColor.BLACK.toString()).setScore(14);
		
		// Display the deaths
		Team deathCounter = board.registerNewTeam("deathCounter");
		deathCounter.addEntry(ChatColor.BLUE.toString());
		deathCounter.setPrefix(ChatColor.YELLOW + "Deaths: ");
		deathCounter.setSuffix(ChatColor.GREEN + "" + csplayer.getDeaths());
		obj.getScore(ChatColor.BLUE.toString()).setScore(13);
		
		// Display the Counter Terrorist wins
		Team ctWinCounter = board.registerNewTeam("ctWinCounter");
		ctWinCounter.addEntry(ChatColor.BOLD.toString());
		ctWinCounter.setPrefix(ChatColor.YELLOW + "CT total wins");
		ctWinCounter.setSuffix(ChatColor.GREEN + "" + plugin.getCounterTerroristsTeam().getWins());
		obj.getScore(ChatColor.BOLD.toString()).setScore(12);
		
		// Display the Terrorist wins
		Team tWinCounter = board.registerNewTeam("tWinCounter");
		tWinCounter.addEntry(ChatColor.DARK_AQUA.toString());
		tWinCounter.setPrefix(ChatColor.YELLOW + "T total wins");
		tWinCounter.setSuffix(ChatColor.GREEN + "" + plugin.getTerroristsTeam().getWins());
		obj.getScore(ChatColor.DARK_AQUA.toString()).setScore(11);
		
		player.setScoreboard(board);
	}

	public void updateScoreBoard(Player player){
		CSPlayer csplayer = CounterStrike.i.getCSPlayer(player);
		
		Scoreboard board = player.getScoreboard();
		
		board.resetScores(player.getName());

		Team t = board.getTeam("t");
		Team ct = board.getTeam("ct");
		
//		t.getEntries().clear();
//		ct.getEntries().clear();
		
		for(CSPlayer p : CounterStrike.i.getTerroristsTeam().getCsPlayers()) {
			t.addEntry(p.getPlayer().getName());
		}
		for(CSPlayer p : CounterStrike.i.getCounterTerroristsTeam().getCsPlayers()) {
			ct.addEntry(p.getPlayer().getName());
		}
		
		// Display the money
		Team moneyCounter = board.getTeam("moneyCounter");
		moneyCounter.setPrefix(ChatColor.YELLOW + "Money: ");
		moneyCounter.setSuffix(ChatColor.GREEN + "$" + csplayer.getMoney());
		
		// Display the Kills
		Team killCounter = board.getTeam("killCounter");
		killCounter.setPrefix(ChatColor.YELLOW + "Kills: ");
		killCounter.setSuffix(ChatColor.GREEN + "" + csplayer.getKills());
		
		// Display the Deaths
		Team deathCounter = board.getTeam("deathCounter");
		deathCounter.setPrefix(ChatColor.YELLOW + "Deaths: ");
		deathCounter.setSuffix(ChatColor.GREEN + "" + csplayer.getDeaths());
		
		// Display the Counter Terrorist wins
		Team ctWinCounter = board.getTeam("ctWinCounter");
		ctWinCounter.setPrefix(ChatColor.YELLOW + "CT wins: ");
		ctWinCounter.setSuffix(ChatColor.GREEN + "" + plugin.getCounterTerroristsTeam().getWins());
		
		// Display the Terrorist wins
		Team tWinCounter = board.getTeam("tWinCounter");
		tWinCounter.setPrefix(ChatColor.YELLOW + "T wins: ");
		tWinCounter.setSuffix(ChatColor.GREEN + "" + plugin.getTerroristsTeam().getWins());
	}
}
