package dev.danablend.counterstrike.tests;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommandExecutor implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        for (TestCommand command : CounterStrike.i.getTestCommands()) {
            if (args.length < 1) {
                sender.sendMessage("Please enter the test command to run.");
                sender.sendMessage("You can chose between the following test commands: ");
                String msg = "";
                for (TestCommand commmand : CounterStrike.i.getTestCommands()) {
                    msg += "\"" + commmand.getName() + "\", ";
                }
                msg = msg.substring(0, msg.lastIndexOf(" ") - 1);
                msg += ".";
                sender.sendMessage(msg);
                return true;
            }
            if (args[0].equalsIgnoreCase(command.getName())) {
                if (command.requiresPlayer() && !(sender instanceof Player)) {
                    sender.sendMessage(Utils.color("&4You need to be a player to use this command."));
                    return true;
                }
                if (args.length < command.getMinArgs() + 1) {
                    sender.sendMessage("Please enter a minimum of " + (command.getMinArgs() + 1) + " args.");
                    sender.sendMessage("/test <arg1> <arg2> <arg3> and so on...");
                    return true;
                }
                command.run(sender, args);
                return true;
            }
        }
        return false;
    }
}
