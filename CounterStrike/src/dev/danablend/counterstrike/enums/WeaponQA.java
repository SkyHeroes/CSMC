package dev.danablend.counterstrike.enums;

import dev.danablend.counterstrike.csplayer.TeamEnum;
import me.zombie_striker.qg.guns.Gun;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @author barpec12
 * created on 2021-01-09
 */
public class WeaponQA extends Weapon {

    /**
     * @param id
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
    public WeaponQA(String id, String name, String displayName, Material material, int cost, int magazineCapacity,
                    int magazines, double damage, double reloadTime, TeamEnum team, WeaponType gunType) {
        super(id, name, displayName, material, cost, magazineCapacity, magazines, damage, reloadTime, team, gunType);
    }

    @Override
    public ItemStack loadItem() {
        ItemStack armoryItem;
        me.zombie_striker.customitemmanager.CustomBaseObject item;

        if (this.weaponType == WeaponType.GRENADE) {
            item = me.zombie_striker.qg.api.QualityArmory.getMiscByName(name);

        } else if (this.weaponType == WeaponType.HELMET) {
            item = me.zombie_striker.qg.api.QualityArmory.getArmorByName(name);

        } else {
            item = me.zombie_striker.qg.api.QualityArmory.getGunByName(name);
            Gun gun = (Gun) item;
            gun.setMaxBullets(magazineCapacity);
            gun.setReloadingTimeInSeconds(reloadTime);
        }

        if (item == null) {
            Bukkit.getLogger().warning("Item " + name + " not found in QualityArmory!");
            return new ItemStack(material);
        }
        if (!(item instanceof me.zombie_striker.customitemmanager.ArmoryBaseObject)) {
            Bukkit.getLogger().warning("Item " + name + " is not a proper item!");
            return new ItemStack(material);
        }

        item.setDisplayname(displayName);

        armoryItem = ((me.zombie_striker.customitemmanager.ArmoryBaseObject) item).getItemStack();
        return armoryItem;
    }
}
