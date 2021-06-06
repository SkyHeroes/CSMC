package dev.danablend.counterstrike;

import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.enums.WeaponQA;
import dev.danablend.counterstrike.enums.WeaponType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

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
    public static final double SPAWN_RADIUS_Z = 7.0;

    public static final boolean FRIENDLY_FIRE_ENABLED = config.getBoolean("friendly-fire-enabled", false);


    public void loadWeapons() {
        ConfigurationSection weapons = config.getConfigurationSection("weapons");

        for (String weaponId : weapons.getValues(false).keySet()) {
            ConfigurationSection weapon = weapons.getConfigurationSection(weaponId);
            WeaponType weaponType = WeaponType.valueOf(weapon.getString("weapon-type"));
            String name = weapon.getString("name");
            String displayName = weapon.isSet("display-name") ? weapon.getString("display-name") : name;
            displayName = ChatColor.translateAlternateColorCodes('&', displayName);
            TeamEnum team = TeamEnum.valueOf(weapon.getString("team"));
            int cost = weapon.getInt("cost");
            Material material = Material.valueOf(weapon.getString("material"));
            Weapon weaponObj;

            int damage = weapon.getInt("damage");
            int magazines = weapon.getInt("magazines");
            int magazineCapacity = weapon.getInt("magazine-capacity");
            double reloadSpeed = weapon.getDouble("reload-speed");

            weaponObj = new WeaponQA(weaponId, name, displayName, material, cost, magazineCapacity, magazines, damage, reloadSpeed, team, weaponType);
            Weapon.addWeapon(weaponObj);
        }
    }
}
