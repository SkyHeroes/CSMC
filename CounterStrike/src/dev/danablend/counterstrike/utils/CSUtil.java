package dev.danablend.counterstrike.utils;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.runnables.Bomb;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CSUtil {

    public static ItemStack getBombItem() {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Plant the bomb on a bomb site.");
        lore.add(ChatColor.YELLOW + "After planting, the bomb will");
        lore.add(ChatColor.YELLOW + "start ticking for about a minute");
        lore.add(ChatColor.YELLOW + "before it explodes for a victory.");
        ItemStack bomb = new ItemStack(Material.TNT);
        ItemMeta meta = bomb.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "C4-Explosive Bomb");
        meta.setLore(lore);
        bomb.setItemMeta(meta);
        return bomb;
    }


    public static boolean isHeadShot(Projectile bullet, Player player) {
        double projectile_height = bullet.getLocation().getY();
        double player_bodyheight = player.getLocation().getY() + 1.60;
        System.out.println("Body height = " + player_bodyheight);
        System.out.println("Projectile height = " + projectile_height);

        if (projectile_height > player_bodyheight) {
            return true;
        }
        return false;
    }


    public static void checkForDead() {
        int dead = 0;
        for (CSPlayer csplayer : CounterStrike.i.getCounterTerrorists()) {
            if (csplayer.getPlayer().isDead() || csplayer.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
                dead++;
            }
        }

        if (dead >= CounterStrike.i.getCounterTerrorists().size() && Bomb.detonated == false) { //was launching twice
            CounterStrike.i.restartGame(CounterStrike.i.getTerroristsTeam());
            return;
        }

        dead = 0;
        for (CSPlayer csplayer : CounterStrike.i.getTerrorists()) {
            if (csplayer.getPlayer().isDead() || csplayer.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
                dead++;
            }
        }
        if (dead >= CounterStrike.i.getTerrorists().size()) {
            if (Bomb.bomb == null)
                CounterStrike.i.restartGame(CounterStrike.i.getCounterTerroristsTeam());
        }
    }


    public static boolean isOutOfShopZone(Player player) {
        CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

        if (csplayer.getTeam().equals(TeamEnum.TERRORISTS)) {
            Location spawn = CounterStrike.i.getTerroristSpawn(false);
            Location pLoc = player.getLocation();
            if ((pLoc.getX() <= spawn.getX() + Config.SPAWN_RADIUS_X && pLoc.getZ() <= spawn.getZ() + Config.SPAWN_RADIUS_Z)
                    && (pLoc.getX() >= spawn.getX() - Config.SPAWN_RADIUS_X && pLoc.getZ() >= spawn.getZ() - Config.SPAWN_RADIUS_Z)) {
                return false;
            }
        } else {
            Location spawn = CounterStrike.i.getCounterTerroristSpawn(false);
            Location pLoc = player.getLocation();
            if ((pLoc.getX() <= spawn.getX() + Config.SPAWN_RADIUS_X && pLoc.getZ() <= spawn.getZ() + Config.SPAWN_RADIUS_Z)
                    && (pLoc.getX() >= spawn.getX() - Config.SPAWN_RADIUS_X && pLoc.getZ() >= spawn.getZ() - Config.SPAWN_RADIUS_Z)) {
                return false;
            }
        }
        return true;
    }


    public static boolean isBombZone(Player player) {

        Location from = CounterStrike.i.bombSiteA();
        Location to = player.getLocation();

        if (from.getBlockX() < to.getBlockX() + 4 || from.getBlockX() > to.getBlockX() - 4) {
            return true;
        }
        if (from.getBlockZ() < to.getBlockZ() + 4 || from.getBlockZ() > to.getBlockZ() - 4) {
            return true;
        }

        from = CounterStrike.i.bombSiteB();

        if (from.getBlockX() < to.getBlockX() + 4 || from.getBlockX() > to.getBlockX() - 4) {
            return true;
        }
        if (from.getBlockZ() < to.getBlockZ() + 4 || from.getBlockZ() > to.getBlockZ() - 4) {
            return true;
        }

        return false;
    }


}
