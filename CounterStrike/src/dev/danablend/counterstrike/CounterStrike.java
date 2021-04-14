package dev.danablend.counterstrike;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import dev.danablend.counterstrike.commands.CounterStrikeCommand;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.Team;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.listeners.AsyncPlayerPreLoginListener;
import dev.danablend.counterstrike.listeners.BlockBreakListener;
import dev.danablend.counterstrike.listeners.BlockIgniteListener;
import dev.danablend.counterstrike.listeners.BlockPlaceListener;
import dev.danablend.counterstrike.listeners.BulletHitListener;
import dev.danablend.counterstrike.listeners.CustomPlayerDeathListener;
import dev.danablend.counterstrike.listeners.EntityDamageByEntityListener;
import dev.danablend.counterstrike.listeners.EntityPickupItemListener;
import dev.danablend.counterstrike.listeners.FoodLevelChangeListener;
import dev.danablend.counterstrike.listeners.InventoryClickListener;
import dev.danablend.counterstrike.listeners.PlayerDeathListener;
import dev.danablend.counterstrike.listeners.PlayerDropItemListener;
import dev.danablend.counterstrike.listeners.PlayerInteractListener;
import dev.danablend.counterstrike.listeners.PlayerItemDamageListener;
import dev.danablend.counterstrike.listeners.PlayerJoinListener;
import dev.danablend.counterstrike.listeners.PlayerQuitListener;
import dev.danablend.counterstrike.listeners.PlayerRespawnListener;
import dev.danablend.counterstrike.listeners.WeaponFireListener;
import dev.danablend.counterstrike.recoil.RecoilUtil;
import dev.danablend.counterstrike.recoil.RecoilUtil_1_13_R2;
import dev.danablend.counterstrike.recoil.RecoilUtil_1_14_R1;
import dev.danablend.counterstrike.recoil.RecoilUtil_1_15_R1;
import dev.danablend.counterstrike.recoil.RecoilUtil_1_16_R3;
import dev.danablend.counterstrike.recoil.RecoilUtil_Legacy;
import dev.danablend.counterstrike.runnables.Bomb;
import dev.danablend.counterstrike.runnables.GameCounter;
import dev.danablend.counterstrike.runnables.GameTimer;
import dev.danablend.counterstrike.runnables.PlayerUpdater;
import dev.danablend.counterstrike.runnables.PlayerUpdaterSlow;
import dev.danablend.counterstrike.runnables.ShopPhaseManager;
import dev.danablend.counterstrike.shop.Shop;
import dev.danablend.counterstrike.shop.ShopListener;
import dev.danablend.counterstrike.tests.TestCommand;
import dev.danablend.counterstrike.utils.CSUtil;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;

public class CounterStrike extends JavaPlugin {

	public GameState gameState = GameState.LOBBY;

	Collection<CSPlayer> csPlayers = new ArrayList<CSPlayer>();
	Collection<CSPlayer> counterTerrorists = new ArrayList<CSPlayer>();
	Collection<CSPlayer> terrorists = new ArrayList<CSPlayer>();
	Set<TestCommand> testCommands = new HashSet<TestCommand>();
	
	PluginManager pm = Bukkit.getPluginManager();
	
	private RecoilUtil recoilUtil;
	
	ItemStack shopItem;
	
	GameTimer timer;

	private Team counterTerroristsTeam;
	private Team terroristsTeam;

	public static CounterStrike i;

	public void onEnable() {
		i = this;
		setup();
	}

	public void onDisable() {
		
	}

	public static final CounterStrike getInstance() {
		return i;
	}
	public boolean usingQualityArmory() {
		return Bukkit.getPluginManager().isPluginEnabled("QualityArmory");
	}
	public void setup() {
		Utils.debug("Setting up...");
		this.terroristsTeam = new Team(TeamEnum.TERRORISTS, terrorists);
		this.counterTerroristsTeam = new Team(TeamEnum.COUNTER_TERRORISTS, counterTerrorists);
		saveDefaultConfig();
		new GameCounter(this).runTaskTimer(this, 200L, 200L);
		new PlayerUpdater(this).runTaskTimer(this, 0L, 5L);
		new PlayerUpdaterSlow().runTaskTimer(this, 0L, 100L);
		pm.registerEvents(new AsyncPlayerPreLoginListener(this), this);
		pm.registerEvents(new BlockPlaceListener(), this);
		pm.registerEvents(new BlockBreakListener(), this);
		pm.registerEvents(new FoodLevelChangeListener(), this);
		pm.registerEvents(new PlayerInteractListener(), this);
		pm.registerEvents(new PlayerItemDamageListener(), this);
		pm.registerEvents(new PlayerDropItemListener(), this);
		pm.registerEvents(new PlayerJoinListener(), this);
		pm.registerEvents(new PlayerQuitListener(), this);
		pm.registerEvents(new PlayerDeathListener(), this);
		pm.registerEvents(new CustomPlayerDeathListener(), this);
		pm.registerEvents(new PlayerRespawnListener(), this);
		pm.registerEvents(new EntityDamageByEntityListener(), this);
		pm.registerEvents(new WeaponFireListener(), this);
		pm.registerEvents(new BulletHitListener(), this);
		pm.registerEvents(new ShopListener(), this);
		pm.registerEvents(new BlockIgniteListener(), this);
		pm.registerEvents(new EntityPickupItemListener(), this);
		pm.registerEvents(new InventoryClickListener(), this);

		getCommand("csmc").setExecutor(new CounterStrikeCommand(this));
		
		for(World w : Bukkit.getWorlds()) {
			w.setGameRule(GameRule.NATURAL_REGENERATION, false);
			w.setGameRule(GameRule.KEEP_INVENTORY, false);
		}
		Utils.debug("Creating shop item...");
		shopItem = new ItemStack(Material.CHEST);
		ItemMeta meta = shopItem.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "(Right click to open shop)");
		shopItem.setItemMeta(meta);
		// Register test commands
//		getCommand("test").setExecutor(new TestCommandExecutor());
		//new HelloTest();
		
		Config c = new Config();
		c.loadWeapons();
		
		new Shop();
		
		if(!usingQualityArmory()) {
			try {
				String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
				switch(version) {
				case "v1_16_R3":
					recoilUtil = new RecoilUtil_1_16_R3();
					break;
				case "v1_15_R1":
					recoilUtil = new RecoilUtil_1_15_R1();
					break;
				case "v_14_R1":
					recoilUtil = new RecoilUtil_1_14_R1();
					break;
				case "v_13_R2":
					recoilUtil = new RecoilUtil_1_13_R2();
					break;
				default:
					recoilUtil = new RecoilUtil_Legacy();
					break;
				}
			} catch (ArrayIndexOutOfBoundsException ex) {
				recoilUtil = new RecoilUtil_Legacy();
			}
			if(recoilUtil instanceof RecoilUtil_Legacy) {
				Bukkit.getLogger().warning("This minecraft version is not fully supported by CS:MC. Switching to legacy (worse) recoil system.");
				Bukkit.getLogger().warning("You can install Quality Armory or newer version of CS:MC to fix this error.");
			}
		}
		Utils.debug("Finished setting up...");
	}

	public RecoilUtil getRecoilUtil() {
		return this.recoilUtil;
	}
	

	public ItemStack getKnife() {
		Utils.debug("Getting knife...");
		ItemStack knife = new ItemStack(Material.IRON_AXE);
		ItemMeta meta = knife.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "Standard Knife");
		knife.setItemMeta(meta);
		return knife;
	}

	public ItemStack getShopItem() {
		return shopItem;
	}

	public Set<TestCommand> getTestCommands() {
		Utils.debug("Getting test commands...");
		return testCommands;
	}

	public void startGame() {
		Utils.debug("Starting game initiated...");
		gameState = GameState.STARTED;
		this.getTerroristSpawn().getWorld().getEntities().stream().filter(Item.class::isInstance).forEach(Entity::remove); // remove tnt from ground
		for (CSPlayer csPlayer : counterTerrorists) {
			Player p = csPlayer.getPlayer();
			p.teleport(getCounterTerroristSpawn());
			if(p.getInventory().getItem(1) == null) {
				p.getInventory().setItem(1, Weapon.getByName("ct-pistol-default").getItem());
			}
			giveEquipment(csPlayer);
		}
		for (CSPlayer csPlayer : terrorists) {
			Player p = csPlayer.getPlayer();
			p.teleport(getTerroristSpawn());
			if(p.getInventory().getItem(1) == null) {
				p.getInventory().setItem(1, Weapon.getByName("t-pistol-default").getItem());
			}
			giveEquipment(csPlayer);
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.setGameMode(GameMode.SURVIVAL);
			player.setFlying(false);
			player.setAllowFlight(false);
			player.getInventory().setItem(2, getKnife());
			player.getInventory().setItem(8, getShopItem());
			player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
			player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			if(!this.usingQualityArmory()) {
				for(ItemStack it : player.getInventory().getContents()) {
					Weapon g = Weapon.getByItem(it);
					if(g != null) {
						it.setAmount(g.getMagazineCapacity());
					}
				}
			}else {
				CSPlayer cp = this.getCSPlayer(player);
				Weapon rifle = cp.getRifle();
				Weapon pistol = cp.getPistol();
				if(rifle != null) {
					ItemStack ammo = me.zombie_striker.qg.api.QualityArmory.getGunByName(rifle.getName()).getAmmoType().getItemStack().clone();
					ammo.setAmount((rifle.getMagazines()-1)*rifle.getMagazineCapacity());
					player.getInventory().setItem(6, ammo);
				}
				if(pistol != null) {
					ItemStack ammo = me.zombie_striker.qg.api.QualityArmory.getGunByName(pistol.getName()).getAmmoType().getItemStack().clone();
					ammo.setAmount((pistol.getMagazines()-1)*pistol.getMagazineCapacity());
					player.getInventory().setItem(7, ammo);
				}
//				Iterator<me.zombie_striker.qg.ammo.Ammo> it = me.zombie_striker.qg.api.QualityArmory.getAmmo();
//				while(it.hasNext()) {
//					player.getInventory().remove(it.next().getItemStack());
//				}
				
			}
			player.getInventory().remove(Material.TNT);
		}
		if (!terrorists.isEmpty()) {
			CSPlayer playerWithBomb = (CSPlayer) terrorists.toArray()[new Random().nextInt(terrorists.toArray().length)];
			playerWithBomb.getPlayer().getInventory().setItem(4, CSUtil.getBombItem());
		}
		pm.registerEvents(new ShopPhaseManager(), this);
		Utils.debug("Game successfully started...");
	}

	public void restartGame(Team winnerTeam) {
		Utils.debug("Restarting game initiated...");
		gameState = GameState.LOBBY;
		Bomb.cleanUp();
		Team loserTeam = (winnerTeam.getTeam().equals(TeamEnum.TERRORISTS)) ? getCounterTerroristsTeam() : getTerroristsTeam();
		for (CSPlayer csplayer : loserTeam.getCsPlayers()) {
			csplayer.getPlayer().setGameMode(GameMode.SPECTATOR);
			csplayer.setMoney(csplayer.getMoney() + Config.MONEY_ON_LOSS);
			csplayer.getPlayer().sendMessage(ChatColor.GREEN + "+ $" + Config.MONEY_ON_LOSS);
		}
		for (CSPlayer csplayer : winnerTeam.getCsPlayers()) {
			csplayer.getPlayer().setGameMode(GameMode.SPECTATOR);
			csplayer.setMoney(csplayer.getMoney() + Config.MONEY_ON_VICTORY);
			csplayer.getPlayer().sendMessage(ChatColor.GREEN + "+ $" + Config.MONEY_ON_VICTORY);
		}
//		for (Player player : Bukkit.getOnlinePlayers()) {
//			player.getInventory().clear();
//		}
		winnerTeam.addVictory();
		loserTeam.addLoss();
		String winnerText = (winnerTeam.getTeam().equals(TeamEnum.TERRORISTS)) ? ChatColor.RED + "Terrorists win." : ChatColor.BLUE + "Counter Terrorists win.";

		if(winnerTeam.getWins() + winnerTeam.getLosses() == 16) { //TODO swap teams
			
		}
		if(winnerTeam.getWins() == 16) {
			PacketUtils.sendTitleAndSubtitleToAll(winnerText, ChatColor.YELLOW + "They also won the whole game! Restarting.", 0, 5, 1);
			for(Player p : Bukkit.getOnlinePlayers()) {
				getCSPlayer(p).clear();
				new CSPlayer(this, p);
				p.getInventory().clear();
			}
			winnerTeam.setLosses(0);
			winnerTeam.setWins(0);
			loserTeam.setLosses(0);
			loserTeam.setWins(0);
		}else {
			PacketUtils.sendTitleAndSubtitleToAll(winnerText, ChatColor.YELLOW + "The next round will start shortly.", 0, 3, 1);
		}
		PacketUtils.sendActionBarToAll(winnerText, 2);
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.teleport(getLobbyLocation());
		}
		new GameCounter(this).runTaskTimer(this, 0L, 200L);
		Utils.debug("Game successfully restarted...");
	}

	public GameTimer getGameTimer() {
		Utils.debug("Getting game timer...");
		return timer;
	}

	public void setGameTimer(GameTimer gametimer) {
		Utils.debug("Setting game timer...");
		this.timer = gametimer;
	}

	public CSPlayer getCSPlayer(Player player) {
		Utils.debug("Getting CSPlayer...");
		for (CSPlayer csPlayer : csPlayers) {
			if (csPlayer.getPlayer().getUniqueId().equals(player.getUniqueId())) {
				Utils.debug("Returning existing CSPlayer...");
				return csPlayer;
			}
		}
		Utils.debug("Returning a new CSPlayer...");
		return new CSPlayer(this, player);
	}

	public Collection<CSPlayer> getCSPlayers() {
		Utils.debug("Getting CSPlayers...");
		return csPlayers;
	}

	public Collection<CSPlayer> getCounterTerrorists() {
		Utils.debug("Getting Counter Terrorists...");
		return counterTerrorists;
	}

	public Collection<CSPlayer> getTerrorists() {
		Utils.debug("Getting Terrorists...");
		return terrorists;
	}

	public Team getCounterTerroristsTeam() {
		Utils.debug("Getting CT Team...");
		return counterTerroristsTeam;
	}

	public Team getTerroristsTeam() {
		Utils.debug("Getting T Team...");
		return terroristsTeam;
	}

	public GameState getGameState() {
		Utils.debug("Getting GameState...");
		return gameState;
	}

	public Location getLobbyLocation() {
		Utils.debug("Getting Lobby Location...");
		String locRaw = getConfig().getString("lobby-location");
		String[] locList = locRaw.split(",");
		World world = Bukkit.getWorld(locList[0]);
		double x = Double.parseDouble(locList[1]);
		double y = Double.parseDouble(locList[2]);
		double z = Double.parseDouble(locList[3]);
		float yaw = Float.parseFloat(locList[4]);
		float pitch = Float.parseFloat(locList[5]);
		return new Location(world, x, y, z, yaw, pitch);
	}

	public Location getTerroristSpawn() {
		Utils.debug("Getting Terrorist spawn...");
		String locRaw = getConfig().getString("spawn-locations.terrorist");
		String[] locList = locRaw.split(",");
		World world = Bukkit.getWorld(locList[0]);
		double x = Double.parseDouble(locList[1]);
		double y = Double.parseDouble(locList[2]);
		double z = Double.parseDouble(locList[3]);
		float yaw = Float.parseFloat(locList[4]);
		float pitch = Float.parseFloat(locList[5]);
		return new Location(world, x, y, z, yaw, pitch);
	}

	public Location getCounterTerroristSpawn() {
		Utils.debug("Getting Counter Terrorist spawn...");
		String locRaw = getConfig().getString("spawn-locations.counterterrorist");
		String[] locList = locRaw.split(",");
		World world = Bukkit.getWorld(locList[0]);
		double x = Double.parseDouble(locList[1]);
		double y = Double.parseDouble(locList[2]);
		double z = Double.parseDouble(locList[3]);
		float yaw = Float.parseFloat(locList[4]);
		float pitch = Float.parseFloat(locList[5]);
		return new Location(world, x, y, z, yaw, pitch);
	}

	public void removePlayerFromFiring(Player player, int ticksDelay, HashMap<UUID, Long> firing) {
		Utils.debug("Started removing player from firing...");
		new BukkitRunnable() {
			@Override
			public void run() {
				firing.remove(player.getUniqueId());
				Utils.debug("Removed player from firing!");
			}
		}.runTaskLater(CounterStrike.getInstance(), ticksDelay);
	}

	public void giveEquipment(CSPlayer csPlayer) {
		Utils.debug("Giving equipment to csplayer...");
		if (csPlayer.getTeam().equals(TeamEnum.TERRORISTS)) {
			ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
			LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
			helmetMeta.setColor(Color.fromRGB(255, 0, 0));
			helmet.setItemMeta(helmetMeta);

			ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
			LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
			chestplateMeta.setColor(Color.fromRGB(255, 0, 0));
			chestplate.setItemMeta(chestplateMeta);

			ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
			LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
			leggingsMeta.setColor(Color.fromRGB(255, 0, 0));
			leggings.setItemMeta(leggingsMeta);

			ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
			LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
			bootsMeta.setColor(Color.fromRGB(255, 0, 0));
			boots.setItemMeta(bootsMeta);

			Player player = csPlayer.getPlayer();
			player.getInventory().setHelmet(helmet);
			player.getInventory().setChestplate(chestplate);
			player.getInventory().setLeggings(leggings);
			player.getInventory().setBoots(boots);
		} else if (csPlayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
			ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
			LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
			helmetMeta.setColor(Color.fromRGB(0, 0, 255));
			helmet.setItemMeta(helmetMeta);

			ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
			LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
			chestplateMeta.setColor(Color.fromRGB(0, 0, 255));
			chestplate.setItemMeta(chestplateMeta);

			ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
			LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
			leggingsMeta.setColor(Color.fromRGB(0, 0, 255));
			leggings.setItemMeta(leggingsMeta);

			ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
			LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
			bootsMeta.setColor(Color.fromRGB(0, 0, 255));
			boots.setItemMeta(bootsMeta);

			Player player = csPlayer.getPlayer();
			player.getInventory().setHelmet(helmet);
			player.getInventory().setChestplate(chestplate);
			player.getInventory().setLeggings(leggings);
			player.getInventory().setBoots(boots);
		}
	}
}
