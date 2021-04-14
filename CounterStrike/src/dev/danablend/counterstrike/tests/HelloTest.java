package dev.danablend.counterstrike.tests;

import org.bukkit.command.CommandSender;

public class HelloTest extends TestCommand {

	@Override
	public void run(CommandSender sender, String[] args) {
		sender.sendMessage("Hello.");
	}

	@Override
	public String getName() {
		return "hello";
	}

	@Override
	public boolean requiresPlayer() {
		return false;
	}

	@Override
	public int getMinArgs() {
		return 0;
	}

}
