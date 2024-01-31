package dev.danablend.counterstrike.utils;

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

}
