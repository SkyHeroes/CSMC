package dev.danablend.counterstrike.tests;

import org.bukkit.command.CommandSender;

import dev.danablend.counterstrike.CounterStrike;

public abstract class TestCommand {
	
	public TestCommand() {
		CounterStrike.i.getTestCommands().add(this);
	}
	
	public abstract void run(CommandSender sender, String[] args);

	public abstract String getName();
	
	public abstract int getMinArgs();

	public abstract boolean requiresPlayer();

}