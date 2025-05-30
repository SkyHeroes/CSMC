package dev.danablend.counterstrike.utils;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PlayerUtils {

    public static boolean playerBehindPlayer(Player playerBehind, Player playerNotBehind) {
        double ang = Math.toDegrees(playerBehind.getLocation().getDirection().angle(playerNotBehind.getLocation().getDirection()));
        return (ang > 18 && ang < 40);
    }

    public static Location getRightHeadLocation(LivingEntity entity) {
        Location eyeLocation = entity.getEyeLocation();
        Location newLocation = eyeLocation.add(rotateRight90Degrees(eyeLocation.getDirection()).multiply(0.2));
        return newLocation;
    }

    public static Vector rotateRight90Degrees(Vector vector) {
        double x = vector.getX();
        vector.setX(-vector.getZ()).setZ(x);
        return vector;
    }

    public static Boolean isInLobbyLocation(Player player) {
        if (CounterStrike.i.getLobbyLocation() == null) return false;

        Location locRaw = CounterStrike.i.getLobbyLocation();

        Integer xx = locRaw.getBlockX();
        Integer zz = locRaw.getBlockZ();
        Location loc = player.getLocation();
        Integer x = loc.getBlockX();
        Integer z = loc.getBlockZ();

        if (x > (xx - 20) && x < (xx + 20)) {
            if (z > (zz - 20) && z < (zz + 20)) {
                return true;
            }
        }

        return false;
    }

    public static Boolean isInSpawn(CSPlayer csplayer) {

        if (csplayer == null) {
            return false;
        }

        Player player = csplayer.getPlayer();

        final Location spawn = csplayer.getSpawnLocation();
        final Location current = player.getLocation();

        if (spawn.getBlockX() > current.getBlockX() + 4 || spawn.getBlockX() < current.getBlockX() - 4) {
          return false;
        }
        if (spawn.getBlockZ() > current.getBlockZ() + 4 || spawn.getBlockZ() < current.getBlockZ() - 4) {
            return false;
        }

        return true;
    }

}
