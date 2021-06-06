package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.database.Mundos;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevelChangeListener implements Listener {
	
	@EventHandler
	public void foodLevelChangeEvent(FoodLevelChangeEvent event) {

		String mundo = event.getEntity().getWorld().getName();

		if (CounterStrike.i.HashWorlds != null) {
			Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

			if (md != null && !md.modoCs) {
				return;
			}
		}

		event.setCancelled(true);
	}
	
}
