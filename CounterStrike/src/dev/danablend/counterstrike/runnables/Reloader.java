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
		
		// Disabled the action bar text because it overlaps with the timers
		
/*		new BukkitRunnable() {
			int count = 0;
			public void run() {
				count++;
				
				switch(count) {
				case 1:
					PacketUtils.sendActionBar(player, ChatColor.GREEN + "Reloading.", 1);
					break;
				case 2:
					PacketUtils.sendActionBar(player, ChatColor.GREEN + "Reloading..", 1);
					break;
				case 3:
					PacketUtils.sendActionBar(player, ChatColor.GREEN + "Reloading...", 1);
					break;
				case 4:
					PacketUtils.sendActionBar(player, ChatColor.GREEN + "Reloading....", 1);
					break;
				case 5:
					PacketUtils.sendActionBar(player, ChatColor.GREEN + "Reloading.....", 1);
					count = 0;
					break;
				}
				if(player.getLevel() >= 1) {
					PacketUtils.sendActionBar(player, ChatColor.GREEN + "Reloaded!", 1);
					this.cancel();
				}
			}
		}.runTaskTimer(CounterStrike.i, 0L, 5L);
		*/
	}
	
	public void run() {
		player.giveExp(1);
		if(player.getLevel() >= 1) {
			item.setAmount(gun.getAmmunition());
			player.setExp(0);
			player.setLevel(0);
			this.cancel();
		}
	}
	
}
