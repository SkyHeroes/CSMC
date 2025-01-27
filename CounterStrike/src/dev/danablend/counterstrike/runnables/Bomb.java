package dev.danablend.counterstrike.runnables;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.enums.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.events.WeaponFireEvent;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

public class Bomb {

    public static Bomb bomb = null;
    public static boolean detonated = false;
    private static boolean cleaning = false;
    private Object bombGlobalTask;
    private Object bombExplodeTask;
    private Object defuseTask;
    private static Location location;
    private static ArmorStand hologram;
    private int countdown;
    private float defuseTimeLeft;
    private CSPlayer defuser;

    public Bomb(int countdown, Location location) {
        bomb = this;

        if (bombGlobalTask != null) CounterStrike.i.myBukkit.cancelTask(bombGlobalTask);

        cleaning = false;
        detonated = false;
        location.getBlock().setType(Material.TNT);

        this.countdown = countdown;
        this.defuseTimeLeft = Config.BOMB_DEFUSE_TIME;
        this.defuser = null;
        this.location = location;

        CounterStrike.i.setGameState(GameState.PLANTED);

        hologram = CounterStrike.i.myBukkit.startLabel(location);
        CounterStrike.i.myBukkit.showLabel(hologram, ChatColor.YELLOW + "Exploding in " + countdown + " seconds.", true);

        PacketUtils.sendTitleAndSubtitleToInGame(ChatColor.YELLOW + "The bomb has been planted", ChatColor.YELLOW + "It will explode in " + Config.BOMB_TIMER + " seconds", 0, 2, 0);
    }


    public static void cleanUp() {

        if (!CounterStrike.i.getGameState().equals(GameState.PLANTED)) return;

        cleaning = true;
        bomb = null;
        detonated = false;

        if (location == null) return;

        CounterStrike.i.myBukkit.showLabel(hologram, "", false);

        if (!CounterStrike.i.isEnabled()) {

            try {
                location.getBlock().setType(Material.AIR);
            } catch (Exception e) {
                Utils.debug(" CLEANUP Exception ");
            }
            return;
        }

        CounterStrike.i.myBukkit.runTaskLater(null, location, null, () -> {
            location.getBlock().setType(Material.AIR);
        }, 1);
    }


    public void run() {
        if (cleaning) {
            CounterStrike.i.myBukkit.cancelTask(bombGlobalTask);
            return;
        }

        int serverSize = CounterStrike.i.getCSPlayers().size();

        if ((serverSize == 0 || CounterStrike.i.getServer().getOnlinePlayers().size() == 0) && CounterStrike.i.quitExitGame) {
            Utils.debug("Aborting Counter, no players left");
            cleanUp();
            CounterStrike.i.myBukkit.cancelTask(bombGlobalTask);
            CounterStrike.i.StopGameCounter();
            return;
        }

        countdown--;

        if (countdown <= 0) {
            CounterStrike.i.myBukkit.cancelTask(bombGlobalTask);
            explode();
        } else {

            if (countdown <= 5) {
                CounterStrike.i.myBukkit.showLabel(hologram, ChatColor.RED + "Exploding in " + countdown + " seconds.", true);

                PacketUtils.sendTitleAndSubtitleToInGame(ChatColor.RED + "It is going to Explode!!", ChatColor.YELLOW + "Run for your lives", 0, 4, 1);
            } else {
                CounterStrike.i.myBukkit.showLabel(hologram, ChatColor.YELLOW + "Exploding in " + countdown + " seconds.", true);
            }

            location.getWorld().playSound(location, Sound.UI_BUTTON_CLICK, 2f, 1f);
            location.getWorld().playEffect(location, Effect.CLICK1, 0);
            PacketUtils.sendActionBarToInGame(ChatColor.RED + "The bomb will explode in " + countdown + " seconds.");
        }
    }


    public void setScheduledTask(Object task) {
        this.bombGlobalTask = task;
    }


    public void defuse(CSPlayer csplayer) {
        if (defuser != null) {
            return;
        }
        this.defuser = csplayer;
        DefuseChecker defuseChecker = new DefuseChecker(csplayer);
        Bukkit.getPluginManager().registerEvents(defuseChecker, CounterStrike.i);
        Player player = csplayer.getPlayer();

        defuseTask = CounterStrike.i.myBukkit.runTaskTimer(null, location, null, () -> {
            if (location == null || hologram == null) {
                cleanUp();
                CounterStrike.i.myBukkit.cancelTask(defuseTask);
                return;
            }

            if (defuser == null) {
                HandlerList.unregisterAll(defuseChecker);
                defuseTimeLeft = Config.BOMB_DEFUSE_TIME;

                CounterStrike.i.myBukkit.showLabel(hologram, ChatColor.GRAY + "(Right click to defuse)", true);

                CounterStrike.i.myBukkit.cancelTask(defuseTask);
                return;
            }

            if (player.getLocation().distance(location) > 2 || player.isDead() || player.getGameMode().equals(GameMode.SPECTATOR) || (player.getInventory().getItemInMainHand() != null && !player.getInventory().getItemInMainHand().getType().equals(Material.IRON_AXE))) {
                HandlerList.unregisterAll(defuseChecker);
                defuseTimeLeft = Config.BOMB_DEFUSE_TIME;

                CounterStrike.i.myBukkit.showLabel(hologram, ChatColor.GRAY + "(Right click to defuse)", true);

                removeDefuser();
                CounterStrike.i.myBukkit.cancelTask(defuseTask);
                return;
            }

            if (defuseTimeLeft <= 0) {
                HandlerList.unregisterAll(defuseChecker);
                csplayer.settempMVP(csplayer.gettempMVP() + 3);

                CounterStrike.i.restartGame(CounterStrike.i.getCounterTerroristsTeam());
                CounterStrike.i.myBukkit.cancelTask(defuseTask);
                return;
            }

            if (Bomb.bomb == null) {
                CounterStrike.i.myBukkit.cancelTask(defuseTask);
                return;
            }

            CounterStrike.i.myBukkit.showLabel(hologram, ChatColor.GREEN + "DEFUSING: " + (new DecimalFormat("##.##").format(defuseTimeLeft)) + " s.", true);

            defuseTimeLeft -= 5.0D / 20.0D;
        }, 1, 5);

    }


    public void explode() {

        CounterStrike.i.myBukkit.showLabel(hologram, "", false);

        CounterStrike.i.myBukkit.runTaskLater(null, location, null, () -> {
            location.getBlock().setType(Material.AIR);
        }, 1);

        AtomicInteger counter = new AtomicInteger(25);

        bombExplodeTask = CounterStrike.i.myBukkit.runTaskTimer(null, location, null, () -> {
            detonated = true;
            counter.getAndDecrement();
            location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 22, false, false);

            if (counter.get() <= 0) {
                CounterStrike.i.myBukkit.cancelTask(bombExplodeTask);
                CounterStrike.i.restartGame(CounterStrike.i.getTerroristsTeam());
            }
        }, 1L, 1L);

    }

    private void removeDefuser() {
        this.defuser = null;
    }


    private class DefuseChecker implements Listener {

        private CSPlayer defuser;

        public DefuseChecker(CSPlayer defuser) {
            this.defuser = defuser;
        }

        @EventHandler
        private void weaponFire(WeaponFireEvent event) {

            CSPlayer csplayer = CounterStrike.i.getCSPlayer(event.getPlayer(), false, null);

            if (csplayer != null && csplayer.equals(defuser)) {
                removeDefuser();
            }
        }

    }

}
