package dev.danablend.counterstrike.runnables;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.events.WeaponFireEvent;
import dev.danablend.counterstrike.utils.PacketUtils;

public class Bomb extends BukkitRunnable {

    public static Bomb bomb = null;
    public static boolean detonated = false;

    private Location location;
    private Hologram hologram;
    private int countdown;
    private float defuseTimeLeft;
    private CSPlayer defuser;
    private static boolean cleaning = false;

    public Bomb(GameTimer timer, int countdown, Location location) {
        bomb = this;
        timer.cancel();
        cleaning = false;
        detonated = false;
        location.getBlock().setType(Material.TNT);
        this.countdown = countdown;
        this.defuseTimeLeft = Config.BOMB_DEFUSE_TIME;
        this.defuser = null;
        this.location = location;
        this.hologram = HologramsAPI.createHologram(CounterStrike.i, location.clone().add(0.5, 2.25, 0.5));
        hologram.appendTextLine(ChatColor.RED + "Bomb");
        hologram.appendTextLine(ChatColor.YELLOW + "Exploding in " + countdown + " seconds.");
        hologram.appendTextLine(ChatColor.GRAY + "(Right click to defuse)");
        PacketUtils.sendTitleAndSubtitleToInGame(ChatColor.YELLOW + "The bomb has been planted", ChatColor.YELLOW + "It will explode in " + Config.BOMB_TIMER + " seconds", 0, 2, 0);
    }

    @Override
    public void run() {
        if (cleaning) {
            this.cancel();
            hologram.delete();
            location.getBlock().setType(Material.AIR);
            return;
        }
        countdown--;
        ((TextLine) hologram.getLine(1)).setText(ChatColor.YELLOW + "Exploding in " + countdown + " seconds.");

        if (countdown <= 0) {
            this.cancel();
            explode();
        } else {

            if (countdown == 5) {
                PacketUtils.sendTitleAndSubtitleToInGame(ChatColor.RED + "It is going to Explode!!", ChatColor.YELLOW + "Run for your lives", 0, 4, 1);
            }

            location.getWorld().playSound(location, Sound.UI_BUTTON_CLICK, 2f, 1f);
            location.getWorld().playEffect(location, Effect.CLICK1, 0);
            PacketUtils.sendActionBarToInGame(ChatColor.RED + "The bomb will explode in " + countdown + " seconds.", 1);
        }
    }


    public void defuse(CSPlayer csplayer) {
        if (defuser != null) {
            return;
        }
        this.defuser = csplayer;
        DefuseChecker defuseChecker = new DefuseChecker(csplayer);
        Bukkit.getPluginManager().registerEvents(defuseChecker, CounterStrike.i);
        Player player = csplayer.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {

                if (location == null || hologram == null) {
                    System.out.println("#################  contingency bomb cleaning");
                    cleanUp();
                    this.cancel();
                    return;
                }

                if (defuser == null) {
                    HandlerList.unregisterAll(defuseChecker);
                    defuseTimeLeft = Config.BOMB_DEFUSE_TIME;
                    ((TextLine) hologram.getLine(2)).setText(ChatColor.GRAY + "(Right click to defuse)");
                    this.cancel();
                    return;
                }

                if (player.getLocation().distance(location) > 2 || player.isDead() || player.getGameMode().equals(GameMode.SPECTATOR) || (player.getInventory().getItemInMainHand() != null && !player.getInventory().getItemInMainHand().getType().equals(Material.IRON_AXE))) {
                    HandlerList.unregisterAll(defuseChecker);
                    defuseTimeLeft = Config.BOMB_DEFUSE_TIME;

                    try { //contigency
                        ((TextLine) hologram.getLine(2)).setText(ChatColor.GRAY + "(Right click to defuse)");
                    } catch (Exception e) {
                    }

                    removeDefuser();
                    this.cancel();
                    return;
                }

                if (defuseTimeLeft <= 0) {
                    HandlerList.unregisterAll(defuseChecker);
                    csplayer.settempMVP(csplayer.gettempMVP() + 3);

                    CounterStrike.i.restartGame(CounterStrike.i.getCounterTerroristsTeam());
                    this.cancel();
                    return;
                }

                if (Bomb.bomb == null) {
                    this.cancel();
                    return;
                }
                ((TextLine) hologram.getLine(2)).setText(ChatColor.GREEN + "DEFUSING: " + (new DecimalFormat("##.##").format(defuseTimeLeft)) + " s.");

                defuseTimeLeft -= 5.0D / 20.0D;
            }
        }.runTaskTimer(CounterStrike.i, 0, 5);
    }


    public void explode() {
        location.getBlock().setType(Material.AIR);
        hologram.delete();

        new BukkitRunnable() {
            int counter = 25;

            public void run() {
                detonated = true;
                counter--;
                location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 22, false, false);

                if (counter <= 0) {
                    this.cancel();
                    CounterStrike.i.restartGame(CounterStrike.i.getTerroristsTeam());
                }
            }
        }.runTaskTimer(CounterStrike.i, 1L, 1L);
    }


    public static void cleanUp() {
        cleaning = true;
        bomb = null;
        detonated = false;
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
            System.out.println("defusing weapon fire event");

            CSPlayer csplayer = CounterStrike.i.getCSPlayer(event.getPlayer(), false, null);

            if (csplayer != null && csplayer.equals(defuser)) {
                removeDefuser();
            }
        }

    }

}
