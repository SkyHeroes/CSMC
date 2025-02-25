package dev.danablend.counterstrike.csplayer;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.runnables.Reloader;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

import static dev.danablend.counterstrike.CounterStrike.*;

public class CSPlayer {

    private Collection<CSPlayer> csPlayers;
    private Collection<CSPlayer> terrorists;
    private Collection<CSPlayer> counterTerrorists;

    private Player player;
    private int money;
    private int kills;
    private int chickenKills;
    private int deaths;
    private int assists;
    private int mvp;
    private int tempmvp;
    private TeamEnum team;
    private String colour;
    private String opponentColour;
    private boolean status = false;
    private FastBoard board;
    private boolean isNPC = false;
private CounterStrike plugin;

    public CSPlayer(CounterStrike plugin, Player player, String colour) {
        Utils.debug("#### New CSPlayer " + player.getName() + " with requested colour " + colour);

        csPlayers = plugin.getCSPlayers();
        terrorists = plugin.getTerrorists();
        counterTerrorists = plugin.getCounterTerrorists();

        this.player = player;
        this.money = Config.STARTING_MONEY;
        this.kills = 0;
        this.deaths = 0;
        this.plugin=plugin;

        if (plugin.standardTeamColours) {

            if (colour.equals("GOLD") || colour.equals("RED")) {
                this.team = TeamEnum.TERRORISTS;
                plugin.getTerroristsTeam().setColour(colour);
                Utils.debug(player.getName() + " selected team terr");

            } else {
                this.team = TeamEnum.COUNTER_TERRORISTS;
                colour = "AQUA";
                plugin.getCounterTerroristsTeam().setColour(colour);
                Utils.debug(player.getName() + " selected team counter terr");
            }

        } else {

            if (colour.equals(plugin.getTerroristsTeam().getColour()) && terrorists.size() <= (counterTerrorists.size() + 1)) {
                this.team = TeamEnum.TERRORISTS;
                Utils.debug(player.getName() + " got terr1");

            } else if (colour.equals(plugin.getCounterTerroristsTeam().getColour()) && counterTerrorists.size() <= (terrorists.size() + 1)) {
                this.team = TeamEnum.COUNTER_TERRORISTS;
                Utils.debug(player.getName() + " got CT1");

            } else if (plugin.getTerroristsTeam().getColour().equals("WHITE")) {
                this.team = TeamEnum.TERRORISTS;

                if (colour.equals(plugin.getCounterTerroristsTeam().getColour()))
                    colour = "GOLD";

                Utils.debug(player.getName() + " got terr2 and colour " + colour);

                plugin.getTerroristsTeam().setColour(colour);

            } else if (plugin.getCounterTerroristsTeam().getColour().equals("WHITE")) {
                this.team = TeamEnum.COUNTER_TERRORISTS;

                if (colour.equals(plugin.getCounterTerroristsTeam().getColour()))
                    colour = "AQUA";

                Utils.debug(player.getName() + " got CT2 and colour " + colour);

                plugin.getCounterTerroristsTeam().setColour(colour);

            } else if (terrorists.size() <= counterTerrorists.size()) {
                this.team = TeamEnum.TERRORISTS;
                this.colour = plugin.getTerroristsTeam().getColour();

                Utils.debug(player.getName() + " got terr3 and colour " + this.colour);

            } else if (counterTerrorists.size() <= terrorists.size()) {
                this.team = TeamEnum.COUNTER_TERRORISTS;
                this.colour = plugin.getCounterTerroristsTeam().getColour();

                Utils.debug(player.getName() + " got CT3 and colour " + this.colour);

            } else {
                Utils.debug(player.getName() + " salta return " + colour);
                return;
            }
        }

        if (team.equals(TeamEnum.TERRORISTS)) {
            terrorists.add(this);
        } else if (team.equals(TeamEnum.COUNTER_TERRORISTS)) {
            counterTerrorists.add(this);
        }
        csPlayers.add(this);

        if (getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
            PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "You are a " + ChatColor.BLUE + "Counter Terrorist", ChatColor.BLUE + "Defend the sites from terrorists, defuse the bomb.", 1, 4, 1);
        } else if (getTeam().equals(TeamEnum.TERRORISTS)) {
            PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "You are a " + ChatColor.RED + "Terrorist", ChatColor.RED + "Plant the bomb on the sites, have it explode.", 1, 4, 1);
        }

        this.colour = colour;
        status = true;

        manageSpeed();
    }

    public void manageSpeed() {
        if (player == null) return;

        boolean hasAxe = (player.getInventory().getItemInMainHand().getType().equals(Material.IRON_AXE));

        if (hasAxe && player.getWalkSpeed() == 0.2f) {
            //  CounterStrike.i.myBukkit.runTask(player, null, null, () -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, (int) (player.getWalkSpeed() * 0.7))));
            player.setWalkSpeed(0.234f);

        } else if (!hasAxe && player.getWalkSpeed() == 0.234f) {
            //  CounterStrike.i.myBukkit.runTask(player, null, null, () -> player.removePotionEffect(PotionEffectType.SPEED));
            player.setWalkSpeed(0.2f);
        }

    }

    public Location getSpawnLocation() {

        if (getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
            return CounterStrike.i.getCounterTerroristSpawn(false);
        } else if (getTeam().equals(TeamEnum.TERRORISTS)) {
            return CounterStrike.i.getTerroristSpawn(false);
        } else {
            return null;
        }
    }

    public void clear() {
        boolean test1 = csPlayers.remove(this);
        boolean test2 = terrorists.remove(this);
        boolean test3 = counterTerrorists.remove(this);
        money = 0;
        kills = 0;
        deaths = 0;

        if (player != null) {
            Utils.debug("Clearing CSPlayer data for " + player.getName() + "   " + test1 + "    " + test2 + "    " + test3);
            player.setWalkSpeed(0.2f);
        }

        //uhm?  player = null;
        team = null;
    }

    public void reload(ItemStack item) {
        Weapon gun = Weapon.getByItem(item);
        if (gun == null) {
            return;
        }
        new Reloader(player, gun);
    }

    public Weapon getHelmet() {
        Utils.debug("Checking if CSPlayer " + player.getName() + " has any helmet...");
        return Weapon.getByItem(player.getInventory().getHelmet());
    }

    public Weapon getArmor() {
        Utils.debug("Checking if CSPlayer " + player.getName() + " has any armor...");
        return Weapon.getByItem(player.getInventory().getChestplate());
    }

    public Weapon getRifle() {
        Utils.debug("Checking if CSPlayer " + player.getName() + " has any rifles...");
        return Weapon.getByItem(player.getInventory().getItem(RIFLE_SLOT));
    }

    public Weapon getPistol() {
        Utils.debug("Checking if CSPlayer " + player.getName() + " has any pistols...");
        return Weapon.getByItem(player.getInventory().getItem(PISTOL_SLOT));
    }

    public Weapon getGrenade() {
        Utils.debug("Checking if CSPlayer " + player.getName() + " has any grenades..." + (player.getInventory().getItem(GRENADE_SLOT) != null));
        return Weapon.getByItem(player.getInventory().getItem(GRENADE_SLOT));
    }

    public ItemStack getBomb() {
        Utils.debug("Checking if CSPlayer " + player.getName() + " has any Bombs... " + (player.getInventory().getItem(TNT_SLOT) != null));
        return player.getInventory().getItem(TNT_SLOT);
    }

    public ItemStack getKnife() {
        Utils.debug("Checking if CSPlayer " + player.getName() + " has any Knifes... " + (player.getInventory().getItem(KNIFE_SLOT) != null));
        return player.getInventory().getItem(KNIFE_SLOT);
    }


    public void setBoard(FastBoard board) {
        this.board = board;
    }

    public FastBoard returnBoard() {
        return board;
    }

    public Player getPlayer() {
        //  Utils.debug("Getting player object from CSPlayer " + player.getName());
        return player;
    }

    public void setPlayer(Player player) {
        //  Utils.debug("Getting player object from CSPlayer " + player.getName());
        this.player = player;
    }

    public int getKills() {
        //  Utils.debug("Getting kills for CSPlayer " + player.getName());
        return kills;
    }

    public void setKills(int kills) {
        //   Utils.debug("Setting kills for CSPlayer " + player.getName());
        this.kills = kills;
    }

    public int getChickenKills() {
        //  Utils.debug("Getting kills for CSPlayer " + player.getName());
        return chickenKills;
    }

    public void setChickenKills(int kills) {
        //   Utils.debug("Setting kills for CSPlayer " + player.getName());
        this.chickenKills = kills;
    }


    public int getDeaths() {
        //  Utils.debug("Getting deaths for CSPlayer " + player.getName());
        return deaths;
    }

    public void setDeaths(int deaths) {
        //  Utils.debug("Setting deaths for CSPlayer " + player.getName());
        this.deaths = deaths;
    }

    public int getAssists() {
        return assists;
    }

    public void setAssists(int assist) {
        this.assists = assist;
    }

    public int getMVP() {
        return mvp;
    }

    public void setMVP(int mvp) {
        this.mvp = mvp;
    }


    public int gettempMVP() {
        return tempmvp;
    }

    public void settempMVP(int tempmvp) {
        this.tempmvp = tempmvp;
    }


    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        if (!plugin.modeValorant && money > 16000) money = 16000;
        if (plugin.modeValorant && money > 9000) money = 9000;

        this.money = money;
    }

    public void setColourOpponent(String opponentcolour) {
        this.opponentColour = opponentcolour;
    }

    public String getColour() {
        return colour;
    }

    public String getOpponentColour() {
        return opponentColour;
    }

    public TeamEnum getTeam() {
        return team;
    }

    public void setTeam(TeamEnum team) {
        Utils.debug("Setting team for CSPlayer " + player.getName());
        this.team = team;
    }

    public boolean isTerrorist() {

        if (getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
            return false;
        } else if (getTeam().equals(TeamEnum.TERRORISTS)) {
            return true;
        } else {
            return false;
        }
    }

    public void setNPC() {
        isNPC = true;
    }

    public boolean isNPC() {
        return isNPC;
    }

    public boolean returStatus() {
        return status;
    }

}
