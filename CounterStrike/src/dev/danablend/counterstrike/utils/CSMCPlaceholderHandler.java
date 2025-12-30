package dev.danablend.counterstrike.utils;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.enums.GameState;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static dev.danablend.counterstrike.Config.MAX_ROUNDS;

public class CSMCPlaceholderHandler extends PlaceholderExpansion {

    private final CounterStrike plugin; //


    public CSMCPlaceholderHandler(CounterStrike plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors()); //
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "CSMC";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion(); //
    }

    @Override
    public boolean persist() {
        return true; //
    }

    //Pretty much all I would need is an interface to see the currently running games and the players on each team
    public String onPlaceholderRequest(Player player, @NotNull String params) {

        CSPlayer myPlayer = null;

        if (player != null) {
            myPlayer = plugin.getCSPlayer(player, false, "");
        }

        String returnString = "N/S";

        try {
            switch (params.toLowerCase(Locale.ROOT)) {

                case "playercount_playing" -> returnString = String.valueOf(plugin.getCSPlayers().size());
                case "playerlist_playing" -> returnString = myPlayer == null ? "N/A" : returnPlayerList();
                case "map_set" -> returnString = plugin.Map;
                case "game_running" -> returnString = gameRunning();
                case "current_round" -> returnString = myPlayer == null ? "N/A" :(plugin.getTerroristsTeam().getLosses() + plugin.getCounterTerroristsTeam().getWins() + 1) + " of " + MAX_ROUNDS;

                case "player_team_color" -> returnString = myPlayer == null ? "N/A" : myPlayer.getColour();
                case "player_kills" ->
                        returnString = myPlayer == null ? "N/A" : String.valueOf(myPlayer.getKills());
                case "player_deaths" ->
                        returnString = myPlayer == null ? "N/A" : String.valueOf(myPlayer.getDeaths());
                case "player_money" ->
                        returnString = myPlayer == null ? "N/A" : String.valueOf(myPlayer.getMoney());
                case "player_mvp" -> returnString = myPlayer == null ? "N/A" : String.valueOf(myPlayer.getMVP());
                case "player_has_bomb" ->
                        returnString = myPlayer == null || myPlayer.getBomb() == null ? "0" : "1";
                case "player_team_list" ->
                        returnString = myPlayer == null ? "N/A" : returnTeamMembers(myPlayer.getTeam());

                case "player_opponent_team_color" ->
                        returnString = myPlayer == null ? "N/A" : myPlayer.getOpponentColour();
            }

        } catch (Exception e) {
            returnString = "Exception, plz report";
        }

        return returnString;
    }


    private String returnRound(TeamEnum team) {
        String returnString = "";

        if (team.equals(TeamEnum.TERRORISTS)) {
            for (CSPlayer csplayer : plugin.getTerrorists()) {
                if (returnString.equals((""))) returnString = csplayer.getPlayer().getName();
                else returnString = returnString + ";" + csplayer.getPlayer().getName();
            }
        } else if (team.equals(TeamEnum.COUNTER_TERRORISTS)) {
            for (CSPlayer csplayer : plugin.getCounterTerrorists()) {
                if (returnString.equals((""))) returnString = csplayer.getPlayer().getName();
                else returnString = returnString + ";" + csplayer.getPlayer().getName();
            }
        }

        return returnString;
    }

    private String returnTeamMembers(TeamEnum team) {
        String returnString = "";

        if (team.equals(TeamEnum.TERRORISTS)) {
            for (CSPlayer csplayer : plugin.getTerrorists()) {
                if (returnString.equals((""))) returnString = csplayer.getPlayer().getName();
                else returnString = returnString + ";" + csplayer.getPlayer().getName();
            }
        } else if (team.equals(TeamEnum.COUNTER_TERRORISTS)) {
            for (CSPlayer csplayer : plugin.getCounterTerrorists()) {
                if (returnString.equals((""))) returnString = csplayer.getPlayer().getName();
                else returnString = returnString + ";" + csplayer.getPlayer().getName();
            }
        }

        return returnString;
    }


    private String returnPlayerList() {
        String returnString = "";

        for (CSPlayer csplayer : plugin.getCSPlayers()) {
            if (returnString.equals((""))) returnString = csplayer.getPlayer().getName();
            else returnString = returnString + ";" + csplayer.getPlayer().getName();
        }

        return returnString;
    }


    private String gameRunning() {

        if (this.plugin.getGameState().equals(GameState.LOBBY) || this.plugin.getGameState().equals(GameState.WAITING) || this.plugin.getGameState().equals(GameState.STARTING)) {
            return "0";
        }

        return "1";
    }

}
