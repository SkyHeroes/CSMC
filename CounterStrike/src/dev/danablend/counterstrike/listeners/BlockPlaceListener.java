package dev.danablend.counterstrike.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.runnables.Bomb;

public class BlockPlaceListener implements Listener {
	
	@EventHandler
	public void blockPlaceEvent(BlockPlaceEvent event) {
		Block block = event.getBlock();
		Block blockUnder = block.getLocation().add(0, -1, 0).getBlock();
		Material bombMat = Config.BOMB_MATERIAL == null ? Material.BEDROCK : Config.BOMB_MATERIAL;
		if(block.getType().equals(Material.TNT) && blockUnder.getType().equals(bombMat)) {
			if(block.getLocation().distance(event.getPlayer().getLocation())>2) {
				event.setCancelled(true);
				return;
			}
			event.getPlayer().getInventory().getItemInMainHand().setType(Material.AIR);
			new Bomb(CounterStrike.i.getGameTimer(), Config.BOMB_TIMER, block.getLocation()).runTaskTimer(CounterStrike.i, 20, 20);
			return;
		}
		if(CounterStrike.i.getGameState().equals(GameState.STARTED) || !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}
	
}
