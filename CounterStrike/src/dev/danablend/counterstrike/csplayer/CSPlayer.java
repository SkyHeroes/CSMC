package dev.danablend.counterstrike.csplayer;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.runnables.Reloader;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;

public class CSPlayer {
	
	private Collection<CSPlayer> csPlayers;
	private Collection<CSPlayer> terrorists;
	private Collection<CSPlayer> counterTerrorists;
	
	private Player player;
	private int money;
	private int kills;
	private int deaths;
	private TeamEnum team;

	public CSPlayer(CounterStrike plugin, Player player) {
		Utils.debug("Creating new CSPlayer for " + player.getName() + "...");
		this.csPlayers = plugin.getCSPlayers();
		this.terrorists = plugin.getTerrorists();
		this.counterTerrorists = plugin.getCounterTerrorists();
		
		this.player = player;
		this.money = Config.STARTING_MONEY;
		this.kills = 0;
		this.deaths = 0;
		this.team = (terrorists.size() < counterTerrorists.size()) ? TeamEnum.TERRORISTS : TeamEnum.COUNTER_TERRORISTS;
		if(team.equals(TeamEnum.TERRORISTS)) {
			terrorists.add(this);
		} 
		else if(team.equals(TeamEnum.COUNTER_TERRORISTS)) {
			counterTerrorists.add(this);
		}
		csPlayers.add(this);
		
		if(getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
			PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "You are a " + ChatColor.BLUE + "Counter Terrorist", ChatColor.BLUE + "Defend the sites from terrorists, defuse the bomb.", 1, 3, 1);
		} else if(getTeam().equals(TeamEnum.TERRORISTS)){
			PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "You are a " + ChatColor.RED + "Terrorist", ChatColor.RED + "Plant the bomb on the sites, have it explode.", 1, 3, 1);
		}
		update();
	}
	
	public void update() {
//		player.updateInventory();
//		for(ItemStack item : player.getInventory().getContents()) {
//			if(item == null) continue;
//			if(item.getItemMeta() instanceof Damageable) {
//				Damageable meta = (Damageable) item.getItemMeta();
//				meta.setDamage(meta.getDamage() - 20);
//				item.setItemMeta((ItemMeta) meta);
//			}
//		}
		if(player.getInventory().getItemInMainHand().getType().equals(Material.IRON_AXE)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, Config.KNIFE_SPEED-1));
		}else {
			player.removePotionEffect(PotionEffectType.SPEED);
		}
	}
	
	public Location getSpawnLocation() {
		Utils.debug("Getting spawn location for CSPlayer " + player.getName());
		if(getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
			return CounterStrike.i.getCounterTerroristSpawn();
		}
		else if(getTeam().equals(TeamEnum.TERRORISTS)) {
			return CounterStrike.i.getTerroristSpawn();
		}
		else {
			return null;
		}
	}
	
	public void clear() {
		Utils.debug("Clearing CSPlayer data for " + player.getName());
		csPlayers.remove(this);
		terrorists.remove(this);
		counterTerrorists.remove(this);
		player = null;
		money = 0;
		kills = 0;
		deaths = 0;
		team = null;
	}
	
	public void reload(ItemStack item) {
		Weapon gun = Weapon.getByItem(item);
		if(gun == null) {
			return;
		}
		new Reloader(player, gun);
	}
	
	public Weapon getRifle() {
		Utils.debug("Checking if CSPlayer " + player.getName() + " has any rifles...");
		return Weapon.getByItem(player.getInventory().getItem(0));
	}
	
	public Weapon getPistol() {
		Utils.debug("Checking if CSPlayer " + player.getName() + " has any pistols...");
		return Weapon.getByItem(player.getInventory().getItem(1));
	}
	public Weapon getGrenade() {
		Utils.debug("Checking if CSPlayer " + player.getName() + " has any grenades...");
		return Weapon.getByItem(player.getInventory().getItem(3));
	}
	public Player getPlayer() {
		Utils.debug("Getting player object from CSPlayer " + player.getName());
		return player;
	}

	public int getKills() {
		Utils.debug("Getting kills for CSPlayer " + player.getName());
		return kills;
	}

	public void setKills(int kills) {
		Utils.debug("Setting kills for CSPlayer " + player.getName());
		this.kills = kills;
	}

	public int getDeaths() {
		Utils.debug("Getting deaths for CSPlayer " + player.getName());
		return deaths;
	}

	public void setDeaths(int deaths) {
		Utils.debug("Setting deaths for CSPlayer " + player.getName());
		this.deaths = deaths;
	}

	public int getMoney() {
		Utils.debug("Getting money for CSPlayer " + player.getName());
		return money;
	}
	
	public void setMoney(int money) {
		Utils.debug("Setting money for CSPlayer " + player.getName());
		if(money>16000) money = 16000;
		this.money = money;
	}

	public TeamEnum getTeam() {
		Utils.debug("Getting team for CSPlayer " + player.getName());
		return team;
	}
	
	public void setTeam(TeamEnum team) {
		Utils.debug("Setting team for CSPlayer " + player.getName());
		this.team = team;
	}
}
