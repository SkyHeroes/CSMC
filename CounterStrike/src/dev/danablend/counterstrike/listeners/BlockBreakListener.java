package dev.danablend.counterstrike.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;

public class BlockBreakListener implements Listener {
	
	@EventHandler
	public void blockBreakEvent(BlockBreakEvent event) {
		if(CounterStrike.i.getGameState().equals(GameState.STARTED) || !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}
	
}
