package dev.danablend.counterstrike.recoil;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author barpec12
 * created on 2020-05-21
 */
public class RecoilUtil_Legacy implements RecoilUtil{

	@Override
	public void rotateScreen(Player player, float yaw, float pitch) {
		Location shakeLocation = player.getLocation().clone();
		shakeLocation.setPitch(shakeLocation.getPitch() - pitch);
		shakeLocation.setYaw(shakeLocation.getYaw() - yaw);
		player.teleport(shakeLocation);
	}

}
