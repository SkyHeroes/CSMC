package dev.danablend.counterstrike.shop;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.enums.WeaponType;
import dev.danablend.counterstrike.utils.Utils;
import me.zombie_striker.qg.guns.Gun;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Collection;

import static dev.danablend.counterstrike.CounterStrike.*;

public class Shop {

    private static Shop shop;

    private Collection<Weapon> terroristGuns;
    private Collection<Weapon> counterTerroristGuns;

    public Shop() {
        shop = this;
        this.terroristGuns = Weapon.getAllWeaponsByTeam(TeamEnum.TERRORISTS);
        this.counterTerroristGuns = Weapon.getAllWeaponsByTeam(TeamEnum.COUNTER_TERRORISTS);
    }

    public static Shop getShop() {
        Utils.debug("Getting Shop...");
        return shop;
    }

    public void openCounterTerroristShop(Player player) {
        Utils.debug("Opening Counter Terrorist Shop for " + player.getName());
        Inventory inv = Bukkit.createInventory(null, getInventorySize(counterTerroristGuns.size() + 2), Config.counterTerroristShopName);

        for (Weapon gun : counterTerroristGuns) {
            inv.addItem(gun.getShopItem());
        }

        inv.addItem((new Weapon("Helmet1", "LHelmet1", "Light Helmet", Material.LEATHER_HELMET, 400, TeamEnum.COUNTER_TERRORISTS, WeaponType.HELMET)).getShopItem());
        inv.addItem((new Weapon("Kevlar1", "Kevlar1", "Kevlar", Material.LEATHER_CHESTPLATE, 650, TeamEnum.COUNTER_TERRORISTS, WeaponType.ARMOUR)).getShopItem());

        player.openInventory(inv);
    }

    public void openTerroristShop(Player player) {
        Utils.debug("Opening Terrorist Shop for " + player.getName());
        Inventory inv = Bukkit.createInventory(null, getInventorySize(terroristGuns.size() + 2), Config.terroristShopName);

        for (Weapon gun : terroristGuns) {
            inv.addItem(gun.getShopItem());
        }

        inv.addItem((new Weapon("Helmet2", "LHelmet2", "Light Helmet", Material.LEATHER_HELMET, 400, TeamEnum.TERRORISTS, WeaponType.HELMET)).getShopItem());
        inv.addItem((new Weapon("Kevlar2", "Kevlar2", "Kevlar", Material.LEATHER_CHESTPLATE, 650, TeamEnum.TERRORISTS, WeaponType.ARMOUR)).getShopItem());

        player.openInventory(inv);
    }

    public void purchaseShopItem(Player player, ItemStack item) {
        CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

        if (csplayer == null) return;
        if (player == null) return;

        Color mycolor;

        if (csplayer.getColour().equals("RED")) {
            mycolor = Color.RED;
        } else if (csplayer.getColour().equals("BLUE")) {
            mycolor = Color.BLUE;
        } else if (csplayer.getColour().equals("GREEN")) {
            mycolor = Color.GREEN;
        } else if (csplayer.getColour().equals("AQUA")) {
            mycolor = Color.AQUA;
        } else {
            mycolor = Color.YELLOW;
        }

        int money = csplayer.getMoney();
        int slot = -1;

        Weapon gun = Weapon.getByItem(item);

        if (gun == null) {
            Utils.debug(" --> " + ChatColor.RED + "Sorry, no gun selected");
            player.sendMessage(ChatColor.RED + "Sorry, no gun selected");
            return;
        }

        WeaponType type = gun.getWeaponType();

        if (gun.getCost() > money) {
            Utils.debug(" --> " + ChatColor.RED + "Sorry, but you cannot afford this item.");
            player.sendMessage(ChatColor.RED + "Sorry, but you cannot afford this item.");
            return;
        }

        switch (type) {
            case RIFLE:
                if (csplayer.getRifle() != null) {
                    player.sendMessage(ChatColor.RED + "Sorry, you cannot have two rifles at the same time.");
                    return;
                }
                slot = RIFLE_SLOT;
                break;
            case PISTOL:
                if (csplayer.getPistol() != null) {
                    player.sendMessage(ChatColor.RED + "Sorry, you cannot have two pistols at the same time.");
                    return;
                }
                slot = PISTOL_SLOT;
                break;
            case GRENADE:
                if (csplayer.getGrenade() != null) {
                    player.sendMessage(ChatColor.RED + "Sorry, you cannot have two grenades at the same time.");
                    return;
                }
                slot = GRENADE_SLOT;
                break;
            case HELMET:
                if (csplayer.getHelmet() != null) {
                    player.sendMessage(ChatColor.RED + "Sorry, you cannot have two custom helmets at the same time.");
                    return;
                }

                try {
                    LeatherArmorMeta helmetMeta = (LeatherArmorMeta) item.getItemMeta();
                    try {
                        helmetMeta.setColor(mycolor);
                    } catch (Exception e) {
                    }
                    ItemStack helm = gun.loadItem();
                    helm.setItemMeta(helmetMeta);
                    player.getInventory().setHelmet(helm);
                } catch (Exception e) {
                }
                break;

            case ARMOUR:
                if (csplayer.getArmor() != null) {
                    player.sendMessage(ChatColor.RED + "Sorry, you cannot have two custom armours at the same time.");
                    return;
                }

                LeatherArmorMeta ArmorMeta = (LeatherArmorMeta) item.getItemMeta();
                ArmorMeta.setColor(mycolor);
                ItemStack Arm = gun.loadItem();
                Arm.setItemMeta(ArmorMeta);

                player.getInventory().setChestplate(Arm);
                break;
        }

        csplayer.setMoney(money - gun.getCost());

        if (slot > -1) {
            player.getInventory().setItem(slot, item);
        }

        if (CounterStrike.i.usingQualityArmory() && type != WeaponType.GRENADE && type != WeaponType.HELMET && type != WeaponType.ARMOUR) {
            Gun gun1 = me.zombie_striker.qg.api.QualityArmory.getGunByName(gun.getName());

            if (gun1 == null) return;

            if (type == WeaponType.RIFLE) {
                Gun.updateAmmo(gun1, player.getInventory().getItem(RIFLE_SLOT), gun.getMagazineCapacity());
            } else if (type == WeaponType.PISTOL) {
                Gun.updateAmmo(gun1, player.getInventory().getItem(PISTOL_SLOT), gun.getMagazineCapacity());
            }

            ItemStack ammo = me.zombie_striker.qg.api.QualityArmory.getGunByName(gun.getName()).getAmmoType().getItemStack().clone();
            ammo.setAmount((gun.getMagazines() - 1) * gun.getMagazineCapacity());

            if (type == WeaponType.RIFLE) {
                player.getInventory().setItem(RIFLE_AMO_SLOT, ammo);
            } else if (type == WeaponType.PISTOL) {
                player.getInventory().setItem(PISTOL_AMO_SLOT, ammo);
            }
        }

        player.sendMessage(ChatColor.GREEN + "You have purchased " + gun.getDisplayName());
        // Utils.debug("Purchase of an item has been completed...");
    }

    public int getInventorySize(int amountOfItems) {
        Utils.debug("Getting inventory size for shops...");
        int temp = amountOfItems;
        while (temp != 0 && temp % 9 != 0) {
            temp++;
        }
        return temp <= 54 ? temp : 54;
    }

}
