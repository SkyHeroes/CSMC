package dev.danablend.counterstrike.runnables;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;

public class ShopPhaseManager extends BukkitRunnable implements Listener {

	int duration;

	public ShopPhaseManager() {
		duration = Config.SHOP_PHASE_DURATION;
		this.runTaskTimer(CounterStrike.i, 20L, 20L);
	}

	@EventHandler
	public void playerMove(PlayerMoveEvent event) {
		if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ()) {
			Location loc = event.getFrom();
			event.getPlayer().teleport(loc.setDirection(event.getTo().getDirection()));
		}
	}

	@Override
	public void run() {
		String msg = (duration <= 1) ? Utils.color("&6The shop phase ends in &a" + duration + " second.") : Utils.color("&6The shop phase ends in &a" + duration + " seconds.");
		PacketUtils.sendActionBarToAll(msg, 1);
		if (duration <= 0) {
			PacketUtils.sendActionBarToAll(Utils.color("&6The shop phase has ended!"), 1);
			CounterStrike.i.setGameTimer(new GameTimer());
			this.cancel();
		}
		duration--;
	}

	@Override
	public synchronized void cancel() throws IllegalStateException {
		super.cancel();
		HandlerList.unregisterAll(this);
	}
}
