package dev.danablend.counterstrike;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.enums.WeaponQA;
import dev.danablend.counterstrike.enums.WeaponType;

public class Config {
	
	private static final FileConfiguration config = CounterStrike.i.getConfig();
	
	
	public static final String counterTerroristShopName = "Buy Menu - Counter Terrorist";
	public static final String terroristShopName = "Buy Menu - Terrorist";
	
	public static final boolean GAME_ENABLED = config.getBoolean("enabled", true);
	public static final boolean DEBUGGING_ENABLED = config.getBoolean("debug", false);
	
	public static final int MIN_PLAYERS = config.getInt("min-players", 5);
	public static final int MAX_PLAYERS = config.getInt("max-players", 10);
	
	public static final int ROUNDS_TO_WIN = config.getInt("rounds-to-win", 16);
	public static final int MAX_ROUNDS = config.getInt("max-rounds", 30);
	
	public static final int STARTING_MONEY = config.getInt("starting-money", 800);
	public static final int MONEY_ON_VICTORY = config.getInt("money-on-win-reward", 3000);
	public static final int MONEY_ON_LOSS = config.getInt("money-on-loss-reward", 2000);
	public static final int BOMB_TIMER = config.getInt("bomb-timer", 45);
	public static final int SHOP_PHASE_DURATION = config.getInt("shop-phase-duration", 15);
	public static final int MATCH_DURATION = config.getInt("match-duration", 120);
	
	public static final int KNIFE_SPEED = config.getInt("knife-speed", 2);

	public static final Material BOMB_MATERIAL = Material.getMaterial(config.getString("bomb-block", "BEDROCK"));
	public static final float BOMB_DEFUSE_TIME = (float) config.getDouble("bomb-defuse-time", 5f);
	
	public static final double SPAWN_RADIUS_X = 7.0;
	public static final double SPAWN_RADIUS_Y = 3.0;
	public static final double SPAWN_RADIUS_Z = 7.0;
	
	public static final boolean FRIENDLY_FIRE_ENABLED = config.getBoolean("friendly-fire-enabled", false);
	public static final boolean RECOIL_ANIMATION_ENABLED = config.getBoolean("recoil-animation-enabled", true);
	
//	/*
//	 * WEAPON VARIABLES - TERRORIST
//	 */
//	public static final String AK47_NAME = ChatColor.YELLOW + "" + ChatColor.BOLD + "AK-47 Rifle";
//	public static final Material AK47_MATERIAL = Material.INK_SAC;
//	public static final int AK47_DAMAGE = config.getInt("weapons.ak47.damage", 7);
//	public static final int AK47_COST = config.getInt("weapons.ak47.cost", 2700);
//	public static final int AK47_AMMUNITION = config.getInt("weapons.ak47.ammunition", 30);
//	public static final double AK47_RELOADSPEED = config.getDouble("weapons.ak47.reloadspeed", 2.5);
//	public static final TeamEnum AK47_TEAM = TeamEnum.TERRORISTS;
//	
//	public static final String GALIL_NAME = ChatColor.YELLOW + "" + ChatColor.BOLD + "Galil-AR Rifle";
//	public static final Material GALIL_MATERIAL = Material.WHEAT;
//	public static final int GALIL_DAMAGE = config.getInt("weapons.galil.damage", 5);
//	public static final int GALIL_COST = config.getInt("weapons.galil.cost", 2000);
//	public static final int GALIL_AMMUNITION = config.getInt("weapons.galil.ammunition", 35);
//	public static final double GALIL_RELOADSPEED = config.getDouble("weapons.galil.reloadspeed", 3.0);
//	public static final TeamEnum GALIL_TEAM = TeamEnum.TERRORISTS;
//	
//	public static final String GLOCK18_NAME = ChatColor.YELLOW + "" + ChatColor.BOLD + "Glock-18 Pistol";
//	public static final Material GLOCK18_MATERIAL = Material.GOLD_NUGGET;
//	public static final int GLOCK18_DAMAGE = config.getInt("weapons.glock18.damage", 6);
//	public static final int GLOCK18_COST = config.getInt("weapons.glock18.cost", 400);
//	public static final int GLOCK18_AMMUNITION = config.getInt("weapons.glock18.ammunition", 20);
//	public static final double GLOCK18_RELOADSPEED = config.getDouble("weapons.glock18.reloadspeed", 2.2);
//	public static final TeamEnum GLOCK18_TEAM = TeamEnum.TERRORISTS;
//	
//	/*
//	 * WEAPON VARIABLES - COUNTERTERRORIST
//	 */
//	public static final String M4A4_NAME = ChatColor.YELLOW + "" + ChatColor.BOLD + "M4A4 Rifle";
//	public static final Material M4A4_MATERIAL = Material.GLOWSTONE_DUST;
//	public static final int M4A4_DAMAGE = config.getInt("weapons.m4a4.damage", 6);
//	public static final int M4A4_COST = config.getInt("weapons.m4a4.cost", 3100);
//	public static final int M4A4_AMMUNITION = config.getInt("weapons.m4a4.ammunition", 30);
//	public static final double M4A4_RELOADSPEED = config.getDouble("weapons.m4a4.reloadpseed", 3.1);
//	public static final TeamEnum M4A4_TEAM = TeamEnum.COUNTER_TERRORISTS;
//	
//	public static final String FAMAS_NAME = ChatColor.YELLOW + "" + ChatColor.BOLD + "Famas Rifle";
//	public static final Material FAMAS_MATERIAL = Material.GUNPOWDER;
//	public static final int FAMAS_DAMAGE = config.getInt("weapons.famas.damage", 5);
//	public static final int FAMAS_COST = config.getInt("weapons.famas.cost", 2250);
//	public static final int FAMAS_AMMUNITION = config.getInt("weapons.famas.ammunition", 25);
//	public static final double FAMAS_RELOADSPEED = config.getDouble("weapons.famas.reloadpseed", 3.3);
//	public static final TeamEnum FAMAS_TEAM = TeamEnum.COUNTER_TERRORISTS;
//	
//	public static final String USP_NAME = ChatColor.YELLOW + "" + ChatColor.BOLD + "USP-S Pistol";
//	public static final Material USP_MATERIAL = Material.GHAST_TEAR;
//	public static final int USP_DAMAGE = config.getInt("weapons.usp.damage", 6);
//	public static final int USP_COST = config.getInt("weapons.usp.cost", 400);
//	public static final int USP_AMMUNITION = config.getInt("weapons.usp.ammunition", 20);
//	public static final double USP_RELOADSPEED = config.getDouble("weapons.usp.reloadpseed", 2.2);
//	public static final TeamEnum USP_TEAM = TeamEnum.COUNTER_TERRORISTS;
	
	public void loadWeapons() {
		ConfigurationSection weapons = config.getConfigurationSection("weapons");
		for(String weaponId : weapons.getValues(false).keySet()) {
			ConfigurationSection weapon = weapons.getConfigurationSection(weaponId);
			WeaponType weaponType = WeaponType.valueOf(weapon.getString("weapon-type"));
			String name = weapon.getString("name");
			String displayName = weapon.isSet("display-name") ? weapon.getString("display-name") : name;
			displayName = ChatColor.translateAlternateColorCodes('&', displayName);
			TeamEnum team = TeamEnum.valueOf(weapon.getString("team"));
			int cost = weapon.getInt("cost");
			Material material = Material.valueOf(weapon.getString("material"));
			Weapon weaponObj;
			if(weaponType != WeaponType.GRENADE) {
				int damage = weapon.getInt("damage");
				int magazines = weapon.getInt("magazines");
				int magazineCapacity = weapon.getInt("magazine-capacity");
				double reloadSpeed = weapon.getDouble("reload-speed");
				if(CounterStrike.i.usingQualityArmory()) {
					weaponObj = new WeaponQA(weaponId, name, displayName, material, cost, magazineCapacity, magazines, damage, reloadSpeed, team, weaponType);
				}else {
					weaponObj = new Weapon(weaponId, name, displayName, material, cost, magazineCapacity, magazines, damage, reloadSpeed, team, weaponType);
				}
			}else {
				if(!CounterStrike.i.usingQualityArmory()) {
					Bukkit.getLogger().info("Grenade cannot be loaded - QualityArmory is not enabled!");
					continue;
				}
				weaponObj = new Weapon(weaponId, name, displayName, material, cost, team, weaponType);
			}
			
			Weapon.addWeapon(weaponObj);
		}
	}
}
