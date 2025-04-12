package dev.danablend.counterstrike.enums;

import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Weapon {

    private static HashMap<String, Weapon> weapons = new HashMap<>();

    protected String name;
    protected String displayName;
    protected Material material;
    protected int magazineCapacity;
    protected double reloadTime;
    protected WeaponType weaponType;
    private int cost;
    private int ammunition;
    private int magazines;
    private double damage;
    private TeamEnum team;
    private ItemStack item;
    private ItemStack shopItem;
    private String id;

    /**
     * Use only for grenades
     *
     * @param id
     * @param name
     * @param displayName
     * @param material
     * @param cost
     * @param team
     * @param gunType
     */
    public Weapon(String id, String name, String displayName, Material material, int cost, TeamEnum team, WeaponType gunType) {
        this(id, name, displayName, material, cost, 0, 0, 0, 0, team, gunType);
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
    public Weapon(String id, String name, String displayName, Material material, int cost, int magazineCapacity, int magazines, double damage, double reloadTime, TeamEnum team, WeaponType gunType) {
        this.name = name;
        this.displayName = displayName;
        this.material = material;
        this.cost = cost;
        this.magazineCapacity = magazineCapacity > 64 ? 64 : magazineCapacity;
        this.ammunition = magazineCapacity;
        this.magazines = magazines;
        this.damage = damage;
        this.reloadTime = reloadTime;
        this.team = team;
        this.weaponType = gunType;
        this.item = loadItem();
        this.shopItem = loadShopItem();
        this.id = id;
    }

    public static void addWeapon(Weapon weapon) {
        weapons.put(weapon.getId(), weapon);
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

    public static Weapon getByName(String name) {
        return weapons.get(name);
    }

    public static Weapon getByItem(ItemStack item) {

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return null;
        }

        for (Weapon gun : weapons.values()) {
            if (item.getItemMeta().getDisplayName().equals(gun.getDisplayName())) {
                return gun;
            }
        }
        return null;
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

    public String getId() {
        return id;
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

    public int getAmmunition() {
        return ammunition;
    }

    public void setAmmunition(Integer amo) {
        ammunition = amo;
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
        return item;
    }

    public ItemStack getShopItem() {
        return shopItem;
    }

}
