package dev.danablend.counterstrike.listeners;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.GameState;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.database.Mundos;
import dev.danablend.counterstrike.runnables.Bomb;
import dev.danablend.counterstrike.utils.PacketUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static dev.danablend.counterstrike.Config.MAX_PLAYERS;
import static dev.danablend.counterstrike.Config.MAX_ROUNDS;

public class PlayerInteractListener implements Listener {

    CounterStrike plugin;

    public PlayerInteractListener() {
        this.plugin = CounterStrike.i;
    }

    @EventHandler
    public void playerDefuseEvent(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        String mundo = player.getWorld().getName();

        if (CounterStrike.i.HashWorlds != null) {
            Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

            if (md != null && !md.modoCs) {
                return;
            }
        }

        CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

        if (CounterStrike.i.getCSPlayers().size() >= MAX_PLAYERS && csplayer == null) {
            PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "We are sorry", ChatColor.GREEN + "The game is full, please try again later.", 1, 4, 1);
            return;
        }

        if (csplayer == null && event.getAction() == Action.LEFT_CLICK_BLOCK) {

            if (CounterStrike.i.getGameState().equals(GameState.RUN)) {
                dev.danablend.counterstrike.csplayer.Team myTeam = CounterStrike.i.getTerroristsTeam();

                PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "Wait for the end of the current round to join", ChatColor.GREEN + "Current round: " + (myTeam.getLosses() + myTeam.getWins() + 1) + " of " + MAX_ROUNDS + ". Estimated time for new " + CounterStrike.i.getGameTimer().returnTimetoEnd() + "secs", 1, 4, 1);
                return;
            }

            Block blockUnder = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

            String materialColour = blockUnder.getBlockData().getMaterial().toString();

            if (materialColour.contains("CYAN") || materialColour.contains("BLUE")) {
                materialColour = "BLUE";
            } else if (materialColour.contains("RED") || materialColour.contains("PINK")) {
                materialColour = "RED";
            } else if (materialColour.contains("GREEN") || materialColour.contains("LIME")) {
                materialColour = "GREEN";
            } else if (materialColour.contains("YELLOW")) {
                materialColour = "YELLOW";
            } else {
                player.sendMessage("You have to choose one of the floors with colour");
                return;
            }

            csplayer = CounterStrike.i.getCSPlayer(player, true, materialColour);

            String corAdversaria;

            if (!csplayer.returStatus()) {
                player.sendMessage("You have to choose another colour/team");
                csplayer.clear();
                return;
            }

            if (csplayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
                corAdversaria = CounterStrike.i.getTerroristsTeam().getColour();
            } else {
                corAdversaria = CounterStrike.i.getCounterTerroristsTeam().getColour();
            }

            csplayer.setColourOpponent(corAdversaria);

              plugin.StartGameCounter(0);

            return;
        }

        if (Bomb.bomb == null) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (csplayer == null || csplayer.getTeam() != TeamEnum.COUNTER_TERRORISTS) {
            return;
        }

        Bomb bomb = Bomb.bomb;

        bomb.defuse(csplayer);
    }


}
