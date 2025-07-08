package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.events.CustomPlayerDeathEvent;
import dev.danablend.counterstrike.utils.CSUtil;
import dev.danablend.counterstrike.utils.PacketUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class CustomPlayerDeathListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void playerDeathEvent(CustomPlayerDeathEvent event) {
        Player victim = event.getVictim();
        Player killer = event.getKiller();
        CSPlayer csKiller = CounterStrike.i.getCSPlayer(killer, false, null);

        if (csKiller == null) {
            return;
        }

        csKiller.setMoney(csKiller.getMoney() + 300);
        killer.sendMessage(ChatColor.GREEN + "+ $300");
        victim.setHealth(victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        victim.setGameMode(GameMode.SPECTATOR);
        victim.sendMessage(ChatColor.RED + "Wait until next round for a respawn.");
        victim.setSpectatorTarget(killer);
        PacketUtils.sendTitleAndSubtitle(victim, ChatColor.RED + "You are eliminated.", ChatColor.YELLOW + "You will respawn in the next round.", 0, 3, 1);
        CSUtil.checkForAllTeamDead();
    }
}
