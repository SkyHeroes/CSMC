package dev.danablend.counterstrike.recoil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_15_R1.PacketPlayOutPosition;
import net.minecraft.server.v1_15_R1.PacketPlayOutPosition.EnumPlayerTeleportFlags;

/**
 * @author barpec12
 * created on 2020-05-21
 */
public class RecoilUtil_1_15_R1 implements RecoilUtil{
    private Set<EnumPlayerTeleportFlags> teleportFlags = new HashSet<>(Arrays.asList(EnumPlayerTeleportFlags.X, EnumPlayerTeleportFlags.Y, EnumPlayerTeleportFlags.Z, EnumPlayerTeleportFlags.X_ROT, EnumPlayerTeleportFlags.Y_ROT));

	@Override
	public void rotateScreen(Player player, float yaw, float pitch) {
		PacketPlayOutPosition packet = new PacketPlayOutPosition(0.0, 0.0, 0.0, yaw, pitch, teleportFlags, 0);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

}
