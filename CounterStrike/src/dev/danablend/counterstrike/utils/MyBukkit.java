package dev.danablend.counterstrike.utils;

import dev.danablend.counterstrike.CounterStrike;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import static org.bukkit.Bukkit.getServer;
import static org.bukkit.Bukkit.isOwnedByCurrentRegion;

public class MyBukkit {
    private CounterStrike main;
    private boolean isFoliaBased;
    private boolean isPaperBased;
    private MyBukkitPaper myBukkitPaper;

    public MyBukkit(CounterStrike main) {
        this.main = main;

        Class classCheck = null;

        try {
            classCheck = Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
        } catch (Exception e) {
        }

        isFoliaBased = (classCheck != null);

        try {
            classCheck = Class.forName("net.kyori.adventure.text.Component");
        } catch (Exception e) {
        }

        isPaperBased = (classCheck != null);

        if (isPaperBased) myBukkitPaper = new MyBukkitPaper();
    }


    public boolean isFolia() {
        return isFoliaBased;
    }


    public Object runTask(Player player, Location local, Entity entity, Runnable myrun) {
        if (isFoliaBased) {
            if (player != null) return player.getScheduler().run(main, st -> myrun.run(), null);
            else if (local != null) return getServer().getRegionScheduler().run(main, local, st -> myrun.run());
            else if (entity != null) return entity.getScheduler().run(main, st -> myrun.run(), null);
            else return getServer().getGlobalRegionScheduler().run(main, st -> myrun.run());
        } else {
            return org.bukkit.Bukkit.getScheduler().runTask(main, myrun);
        }
    }


    public Object runTaskLater(Player player, Location local, Entity entity, Runnable myrun, long delay) {
        if (isFoliaBased) {
            if (player != null) return player.getScheduler().runDelayed(main, st -> myrun.run(), null, delay);
            else if (local != null)
                return getServer().getRegionScheduler().runDelayed(main, local, st -> myrun.run(), delay);
            else if (entity != null) return entity.getScheduler().runDelayed(main, st -> myrun.run(), null, delay);
            else return getServer().getGlobalRegionScheduler().runDelayed(main, st -> myrun.run(), delay);
        } else {
            return getServer().getScheduler().runTaskLater(main, myrun, delay);
        }
    }


    public Object runTaskTimer(Player player, Location local, Entity entity, Runnable myrun, long delay, long period) {
        if (isFoliaBased) {
            if (player != null)
                return player.getScheduler().runAtFixedRate(main, st -> myrun.run(), null, delay, period);
            else if (local != null)
                return getServer().getRegionScheduler().runAtFixedRate(main, local, st -> myrun.run(), delay, period);
            else if (entity != null)
                return entity.getScheduler().runAtFixedRate(main, st -> myrun.run(), null, delay, period);
            else return getServer().getGlobalRegionScheduler().runAtFixedRate(main, st -> myrun.run(), delay, period);
        } else {
            return getServer().getScheduler().runTaskTimer(main, myrun, delay, period);
        }
    }


    public void cancelTask(Object task) {

        if (task == null) return;

        if (isFoliaBased)
            ((ScheduledTask) task).cancel();
        else
            ((BukkitTask) task).cancel();
    }


    public boolean isCancelled(Object task) {

        if (task == null) return true;

        if (isFoliaBased)
            return ((ScheduledTask) task).isCancelled();
        else
            return ((BukkitTask) task).isCancelled();
    }


    public boolean isOwnedby(Entity entity, Location local, Block block) {

        if (isFoliaBased) {
            if (entity != null) return isOwnedByCurrentRegion(entity);
            else if (local != null) return isOwnedByCurrentRegion(local);
            else if (block != null) return isOwnedByCurrentRegion(block);
        }
        return true;
    }


    public static ArmorStand startLabel(Location location) {

        Location loc = location.clone();
        ArmorStand hologram = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        hologram.setVisible(false);
        hologram.setCustomNameVisible(true);
        hologram.setGravity(false);
        loc.add(0, 2, 0);

        return hologram;
    }


    public void showLabel(ArmorStand hologram, String message, boolean visible) {

        if (hologram == null) return;

        if (visible) {

            if (isPaperBased) {
                myBukkitPaper.hologramCustomName(hologram, message);
            } else {
                hologram.setCustomName(message);
            }
        } else {
            if (hologram != null) {

                Utils.debug("    Limpa ArmorStand ");

                if (!CounterStrike.i.isEnabled()) {
                    hologram.remove();
                    hologram = null;
                    return;
                }

                ArmorStand finalA = hologram;
                CounterStrike.i.myBukkit.runTaskLater(null, null, hologram, () -> {
                    finalA.remove();
                }, 1);

                hologram = null;
            }
        }
    }


    public void setMeta(ItemMeta meta, String text) {
        if (isPaperBased) {
            myBukkitPaper.metaDisplayName(meta, text);
        } else {
            meta.setDisplayName(text);
        }
    }


    public void setPlayerListName(Player player, String text) {
        if (isPaperBased) {
            myBukkitPaper.playerListName(player, text);
        } else {
            player.setPlayerListName(ChatColor.WHITE + player.getName());
        }
    }


    public void sendActionBar(Player player, String text) {
        if (isPaperBased) {
            myBukkitPaper.playerSendActionBar(player, text);
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
        }
    }


    public void teamAddEntity(Team myTeam, Player player) {
        if (isPaperBased) {
            myTeam.addEntity(player);
        } else {
            myTeam.addPlayer(player);
        }
    }


    public void playerTeleport(Player player, Location loc) {
        if (isPaperBased) {
            runTask(player, null, null, () -> player.teleportAsync(loc));
        } else {
            player.teleport(loc);
        }
    }


    public void playerSetResourcePack(Player player, String resourse, String hash) {
        if (isPaperBased) {
            runTaskLater(player, null, null, () -> player.setResourcePack(resourse, hash), 2 * 20);
        } else {
            player.setResourcePack(resourse);
        }
    }

}
