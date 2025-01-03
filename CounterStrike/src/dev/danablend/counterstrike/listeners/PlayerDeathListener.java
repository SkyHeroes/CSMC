package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.database.Worlds;
import dev.danablend.counterstrike.utils.CSUtil;
import dev.danablend.counterstrike.utils.PacketUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDeathListener implements Listener {
    CounterStrike plugin = CounterStrike.i;

    @EventHandler(ignoreCancelled = true)
    public void playerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        String world = player.getWorld().getName();

        if (plugin.HashWorlds != null) {
            Worlds md = (Worlds) plugin.HashWorlds.get(world);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        for (ItemStack it : event.getDrops()) {
            if (!it.getType().equals(Material.TNT)) {
                it.setType(Material.AIR);
            }
        }

        if (!(event.getEntity() instanceof Player)) return;

        Player victim = event.getEntity();

        CSPlayer csplayerVictim = plugin.getCSPlayer(victim, false, null);
        //if not playing
        if (csplayerVictim == null) return;

        String deadPlayerName = (csplayerVictim.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) ? ChatColor.BLUE + victim.getName() : ChatColor.RED + victim.getName();

        plugin.myBukkit.runTask(victim, null, null, () -> {
            victim.spigot().respawn();
            victim.setHealth(victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            victim.setGameMode(GameMode.SPECTATOR);
        });

        victim.sendMessage(ChatColor.RED + "Wait until next round for a respawn.");
        PacketUtils.sendTitleAndSubtitle(victim, ChatColor.RED + "You are dead.", ChatColor.YELLOW + "You will respawn in the next round.", 0, 3, 1);

        try {
            Player killer = victim.getKiller();

            if (killer == null) {
                csplayerVictim.setDeaths(csplayerVictim.getDeaths() + 1);
                event.setDeathMessage(deadPlayerName + ChatColor.YELLOW + " was killed...");
            } else {

                CSPlayer csplayerKiller = CounterStrike.i.getCSPlayer(killer, false, null);

                String killerName = (csplayerKiller.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) ? ChatColor.BLUE + killer.getName() : ChatColor.RED + killer.getName();
                csplayerKiller.setMoney(csplayerKiller.getMoney() + 300);
                killer.sendMessage(ChatColor.GREEN + "+ $300");

                csplayerKiller.setKills(csplayerKiller.getKills() + 1);
                csplayerVictim.setDeaths(csplayerVictim.getDeaths() + 1);
                csplayerKiller.settempMVP(csplayerKiller.gettempMVP() + 1);

                victim.setSpectatorTarget(killer);

                event.setDeathMessage(ChatColor.valueOf(csplayerVictim.getColour()) + deadPlayerName + ChatColor.GRAY + " was killed by " + ChatColor.valueOf(csplayerKiller.getColour()) + killerName);
            }
        } catch (NullPointerException e) {
            csplayerVictim.setDeaths(csplayerVictim.getDeaths() + 1);
            event.setDeathMessage(deadPlayerName + ChatColor.YELLOW + " was killed..");
        }

        // Check if every player on dead player team is dead
        CSUtil.checkForDead();
    }

}
