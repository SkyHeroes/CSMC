package dev.danablend.counterstrike.enums;

import dev.danablend.counterstrike.csplayer.TeamEnum;
import me.zombie_striker.qg.guns.Gun;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Weapon {

    private static HashMap<String, Weapon> weapons = new HashMap<>();
    private static HashMap<String, Weapon> auxweapons = new HashMap<>();

    protected String name;
    protected String displayName;
    protected Material material;
    protected int magazineCapacity;
    protected double reloadTime;
    protected WeaponType weaponType;
    private int cost;
    private int magazines;
    private double damage;
    protected TeamEnum team;
    private ItemStack item;
    private ItemStack shopItem;
    protected String keyName;
    protected Gun myGun;

    /**
     * Use only for grenades
     *
     * @param keyName
     * @param name
     * @param displayName
     * @param material
     * @param cost
     * @param team
     * @param gunType
     */
    public Weapon(String keyName, String name, String displayName, Material material, int cost, TeamEnum team, WeaponType gunType) {
        this(keyName, name, displayName, material, cost, 0, 0, 0, 0, team, gunType);
        addWeapon(this); //will only work for manually added itens
    }

    /**
     * @param name
     * @param displayName
     * @param material
     * @param cost
     * @param magazineCapacity
     * @param magazines
     * @param damage
     * @param reloadTime
     * @param team
     * @param gunType
     */
    public Weapon(String keyName, String name, String displayName, Material material, int cost, int magazineCapacity, int magazines, double damage, double reloadTime, TeamEnum team, WeaponType gunType) {
        this.name = name;
        this.displayName = displayName+"("+team.name().toLowerCase().substring(0,1)+")";
        this.material = material;
        this.cost = cost;
        this.magazineCapacity = magazineCapacity > 64 ? 64 : magazineCapacity;
        this.magazines = magazines;
        this.damage = damage;
        this.reloadTime = reloadTime;
        this.team = team;
        this.weaponType = gunType;
        this.item = loadItem();
        this.shopItem = loadShopItem();
        this.keyName = keyName;
    }

    public static void addWeapon(Weapon weapon)    {
        weapons.put(weapon.getKeyName(), weapon);
        auxweapons.put(weapon.getDisplayName(), weapon);
    }

    public static boolean isWeapon(ItemStack item) {
        return getByItem(item) != null;
    }

    public static Collection<Weapon> getAllWeaponsByTeam(TeamEnum team) {
        Collection<Weapon> guns = new ArrayList<>();
        for (Weapon gun : weapons.values()) {
            if (gun.getTeam() == team) guns.add(gun);
        }
        return guns;
    }

    public static Weapon getBykeyName(String keyname) {
        return weapons.get(keyname );
    }

    public static Weapon getByName(String Name) {
        return auxweapons.get(Name);
    }

    public static Weapon getByItem(ItemStack item) {

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return null;
        }

       return auxweapons.get(item.getItemMeta().getDisplayName());

    }

    public ItemStack loadItem() {
        ItemStack armoryItem;
        armoryItem = new ItemStack(material);

        if (magazineCapacity == 0) {
            magazineCapacity = 1;
        }

        armoryItem.setAmount(magazineCapacity);
        ItemMeta gunMeta = armoryItem.getItemMeta();
        gunMeta.setDisplayName(displayName);
        armoryItem.setItemMeta(gunMeta);

        return armoryItem;
    }

    private ItemStack loadShopItem() {
        ItemStack item = this.item.clone();
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.GREEN + "$" + getCost());
        lore.add(ChatColor.YELLOW + "Damage: " + ChatColor.GREEN + getDamage());
        lore.add(ChatColor.YELLOW + "Ammunition: " + ChatColor.GREEN + getMagazineCapacity());
        lore.add(ChatColor.YELLOW + "Reload Speed: " + ChatColor.GREEN + getReloadTime() + "s");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public String getName() {
        return name;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCost() {
        return cost;
    }

    public int getMagazineCapacity() {
        return magazineCapacity;
    }

    public int getMagazines() {
        return magazines;
    }

    public double getDamage() {
        return damage;
    }

    public double getReloadTime() {
        return reloadTime;
    }

    public TeamEnum getTeam() {
        return team;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public ItemStack getShopItem() {
        return shopItem;
    }

}
