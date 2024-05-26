package dev.danablend.counterstrike.utils;

import dev.danablend.counterstrike.CounterStrike;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import static org.bukkit.Bukkit.getServer;
import static org.bukkit.Bukkit.isOwnedByCurrentRegion;

public class MyBukkit {
    private CounterStrike main;
    private boolean folia ;

    public MyBukkit(CounterStrike main) {
        this.main = main;

        String version = Bukkit.getVersion().toUpperCase();
        folia = (version.contains("-FOLIA-"));

        //server type name
        String minecraftVersion2 = Bukkit.getServer().getName();
        folia = (!folia && (minecraftVersion2.toUpperCase().contains("FOLIA")));
    }

    public boolean isFolia() {
        return folia;
    }


    public Object runTask(Player player, Location local, Entity entity, Runnable myrun) {
        if (isFolia()) {
            if (player != null) return player.getScheduler().run(main, st -> myrun.run(), null);
            else if (local != null) return getServer().getRegionScheduler().run(main, local, st -> myrun.run());
            else if (entity != null) return entity.getScheduler().run(main, st -> myrun.run(), null);
            else return getServer().getGlobalRegionScheduler().run(main, st -> myrun.run());
        } else {
            return org.bukkit.Bukkit.getScheduler().runTask(main, myrun);
        }
    }

    public Object runTaskLater(Player player, Location local, Entity entity, Runnable myrun, long delay) {
        if (isFolia()) {
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
        if (isFolia()) {
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
        if (isFolia())
            ((ScheduledTask) task).cancel();
        else
            ((BukkitTask) task).cancel();
    }

    public boolean isCancelled(Object task) {
        if (folia)
            return ((ScheduledTask) task).isCancelled();
        else
            return ((BukkitTask) task).isCancelled();
    }

    public boolean isOwnedby(Entity entity, Location local, Block block) {

        if (isFolia()) {
            if (entity != null) return isOwnedByCurrentRegion(entity);
            else if (local != null) return isOwnedByCurrentRegion(local);
            else if (block != null) return isOwnedByCurrentRegion(block);
        }
        return true;
    }

}
