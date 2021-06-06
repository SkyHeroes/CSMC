package dev.danablend.counterstrike.runnables;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.enums.Weapon;

public class Reloader extends BukkitRunnable {
	
	private Player player;
	private Weapon gun;
	private double duration;
	private int ticksTilExperience;
	private ItemStack item;
	
	public Reloader(Player player, Weapon gun) {
		this.player = player;
		this.gun = gun;
		this.duration = gun.getReloadTime();
		this.ticksTilExperience = (int) Math.round((duration * 20) / 7);
		this.item = player.getInventory().getItemInMainHand();
		this.runTaskTimer(CounterStrike.i, ticksTilExperience, ticksTilExperience);
	}
	
	public void run() {
		player.giveExp(1);
		if(player.getLevel() >= 1) {
			item.setAmount(gun.getAmmunition());

			System.out.println(gun.getAmmunition() + " reload gun " + gun.getMagazineCapacity());

			player.setExp(0);
			player.setLevel(0);
			this.cancel();
		}
	}
	
}
