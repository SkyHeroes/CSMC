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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;

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
    private Date date = Date.from(Instant.now());
    private FastBoard board;

    public CSPlayer(CounterStrike plugin, Player player, String colour) {
        Utils.debug("#### New CSPlayer " + player.getName() + " with requested colour " + colour);

        csPlayers = plugin.getCSPlayers();
        terrorists = plugin.getTerrorists();
        counterTerrorists = plugin.getCounterTerrorists();

        this.colour = colour;
        this.player = player;
        this.money = Config.STARTING_MONEY;
        this.kills = 0;
        this.deaths = 0;

        if (colour.equals(plugin.getTerroristsTeam().getColour()) && terrorists.size() <= (counterTerrorists.size() + 1)) {
            this.team = TeamEnum.TERRORISTS;
            Utils.debug(player.getName() + " got terr1");

        } else if (colour.equals(plugin.getCounterTerroristsTeam().getColour()) && counterTerrorists.size() <= (terrorists.size() + 1)) {
            this.team = TeamEnum.COUNTER_TERRORISTS;
            Utils.debug(player.getName() + " got CT1");

        } else if (plugin.getTerroristsTeam().getColour().equals("WHITE")) {
            this.team = TeamEnum.TERRORISTS;

            if (colour.equals(plugin.getCounterTerroristsTeam().getColour()))
                colour = "AQUA";

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

        if (team.equals(TeamEnum.TERRORISTS)) {
            terrorists.add(this);
        } else if (team.equals(TeamEnum.COUNTER_TERRORISTS)) {
            counterTerrorists.add(this);
        }
        csPlayers.add(this);

        Utils.debug(player.getName() + " resource " + plugin.ResourseHash.get(player.getName() + "RES"));

        if (plugin.ResourseHash.get(player.getName() + "RES") == null || plugin.ResourseHash.get(player.getName() + "RES") == "DEFAULT") {
            plugin.ResourseHash.remove(player.getName() + "RES");
            plugin.ResourseHash.put(player.getName() + "RES", "QUALITY");

            plugin.loadResourcePack(player, "https://github.com/ZombieStriker/QualityArmory-Resourcepack/releases/download/latest/QualityArmory.zip", "3a34fc09dcc6f009aa05741f8ab487dd17b13eaf");
        }

        if (getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
            PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "You are a " + ChatColor.BLUE + "Counter Terrorist", ChatColor.BLUE + "Defend the sites from terrorists, defuse the bomb.", 1, 4, 1);
        } else if (getTeam().equals(TeamEnum.TERRORISTS)) {
            PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "You are a " + ChatColor.RED + "Terrorist", ChatColor.RED + "Plant the bomb on the sites, have it explode.", 1, 4, 1);
        }

        status = true;

        update();
    }

    public void update() {
        if (player == null) return;

        if (player.getInventory().getItemInMainHand().getType().equals(Material.IRON_AXE)) {
            CounterStrike.i.myBukkit.runTask(player,null,null, () -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, (int) (player.getWalkSpeed() * 0.7))));
        } else {
            CounterStrike.i.myBukkit.runTask(player,null,null, () -> player.removePotionEffect(PotionEffectType.SPEED));
        }
    }

    public Location getSpawnLocation() {

        if (getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
            return CounterStrike.i.getCounterTerroristSpawn(true);
        } else if (getTeam().equals(TeamEnum.TERRORISTS)) {
            return CounterStrike.i.getTerroristSpawn(true);
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
        Utils.debug("Clearing CSPlayer data for " + player.getName() + "   " + test1 + "    " + test2 + "    " + test3);
        player = null;
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
        return Weapon.getByItem(player.getInventory().getItem(0));
    }

    public Weapon getPistol() {
        Utils.debug("Checking if CSPlayer " + player.getName() + " has any pistols...");
        return Weapon.getByItem(player.getInventory().getItem(1));
    }

    public Weapon getGrenade() {
        Utils.debug("Checking if CSPlayer " + player.getName() + " has any grenades..."+ (player.getInventory().getItem(3) != null));
        return Weapon.getByItem(player.getInventory().getItem(3));
    }

    public ItemStack getBomb() {
        Utils.debug("Checking if CSPlayer " + player.getName() + " has any Bombs... "+ (player.getInventory().getItem(4) != null));
        return player.getInventory().getItem(4);
    }

    public ItemStack getKnife() {
        Utils.debug("Checking if CSPlayer " + player.getName() + " has any Knifes... "+ (player.getInventory().getItem(2) != null));
        return player.getInventory().getItem(2);
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
        // Utils.debug("Getting money for CSPlayer " + player.getName());
        return money;
    }

    public void setMoney(int money) {
        //  Utils.debug("Setting money for CSPlayer " + player.getName());
        if (money > 16000) money = 16000;
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
        // Utils.debug("Getting team for CSPlayer " + player.getName());
        return team;
    }

    public void setTeam(TeamEnum team) {
        Utils.debug("Setting team for CSPlayer " + player.getName());
        this.team = team;
    }

    public boolean returStatus() {
        return status;
    }

    public Date getDate() {
        return date;
    }
}
