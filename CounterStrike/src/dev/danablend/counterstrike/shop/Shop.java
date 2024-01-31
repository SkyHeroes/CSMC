package dev.danablend.counterstrike.shop;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.enums.WeaponType;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Collection;

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

    public void purchaseShopItem(Player player, Weapon gun) {
        Utils.debug("Purchase of an item has been initiated...");
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
        WeaponType type = gun.getWeaponType();

        if (gun.getCost() > money) {
            player.sendMessage(ChatColor.RED + "Sorry, but you cannot afford this item.");
            return;
        }

        switch (type) {
            case RIFLE:
                if (csplayer.getRifle() != null) {
                    player.sendMessage(ChatColor.RED + "Sorry, you cannot have two rifles at the same time.");
                    return;
                }
                slot = 0;
                break;
            case PISTOL:
                if (csplayer.getPistol() != null) {
                    player.sendMessage(ChatColor.RED + "Sorry, you cannot have two pistols at the same time.");
                    return;
                }
                slot = 1;
                break;
            case GRENADE:
                if (csplayer.getGrenade() != null) {
                    player.sendMessage(ChatColor.RED + "Sorry, you cannot have two grenades at the same time.");
                    return;
                }
                slot = 3;
                break;
            case HELMET:
                if (csplayer.getHelmet() != null) {
                    player.sendMessage(ChatColor.RED + "Sorry, you cannot have two custom helmets at the same time.");
                    return;
                }

                try {
                    LeatherArmorMeta helmetMeta = (LeatherArmorMeta) gun.loadItem().getItemMeta();
                    try {
                        helmetMeta.setColor(mycolor);
                    } catch (Exception e) {
                        System.out.println(e.getMessage() + " ########### Erro em Item111 " + gun.loadItem());
                    }
                    ItemStack helm = gun.loadItem();
                    helm.setItemMeta(helmetMeta);
                    player.getInventory().setHelmet(helm);
                } catch (Exception e) {
                    System.out.println(e.getMessage() + " ########### Erro em Item " + gun.loadItem());
                }
                break;

            case ARMOUR:
                if (csplayer.getArmor() != null) {
                    player.sendMessage(ChatColor.RED + "Sorry, you cannot have two custom armours at the same time.");
                    return;
                }

                LeatherArmorMeta ArmorMeta = (LeatherArmorMeta) gun.loadItem().getItemMeta();
                ArmorMeta.setColor(mycolor);
                ItemStack Arm = gun.loadItem();
                Arm.setItemMeta(ArmorMeta);

                player.getInventory().setChestplate(Arm);
                break;
        }

        csplayer.setMoney(money - gun.getCost());

        if (slot > -1) {
            player.getInventory().setItem(slot, gun.getItem());
        }

        if (CounterStrike.i.usingQualityArmory() && type != WeaponType.GRENADE && type != WeaponType.HELMET && type != WeaponType.ARMOUR) {
            ItemStack ammo = me.zombie_striker.qg.api.QualityArmory.getGunByName(gun.getName()).getAmmoType().getItemStack().clone();
            ammo.setAmount((gun.getMagazines() - 1) * gun.getMagazineCapacity());

            if (type == WeaponType.RIFLE) {
                player.getInventory().setItem(6, ammo);
            } else player.getInventory().setItem(7, ammo);
        }

        player.sendMessage(ChatColor.GREEN + "You have purchased " + gun.getDisplayName());
        Utils.debug("Purchase of an item has been completed...");
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
