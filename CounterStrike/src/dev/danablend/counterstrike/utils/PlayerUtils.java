package dev.danablend.counterstrike.utils;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;

public class PlayerUtils {
	
	public static boolean playerBehindPlayer(Player playerBehind, Player playerNotBehind) {
		return Math.abs(playerBehind.getLocation().getDirection().angle(playerNotBehind.getLocation().getDirection())) <= 45;
	}
	public static void shakeScreen(Player player, int randomBound) {
		
		if(!Config.RECOIL_ANIMATION_ENABLED) return;
		Random r = new Random();
		float pitch = -(r.nextFloat() * randomBound);
		float yaw = -(r.nextFloat() * randomBound);
		CounterStrike.i.getRecoilUtil().rotateScreen(player, yaw, pitch);
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
