package dev.danablend.counterstrike;

import de.tontito.iacenter.IACenter;
import dev.danablend.counterstrike.commands.CounterStrikeCommand;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.Team;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.database.SQLiteConnection;
import dev.danablend.counterstrike.database.Worlds;
import dev.danablend.counterstrike.enums.GameState;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.listeners.*;
import dev.danablend.counterstrike.runnables.*;
import dev.danablend.counterstrike.shop.Shop;
import dev.danablend.counterstrike.shop.ShopListener;
import dev.danablend.counterstrike.tests.TestCommand;
import dev.danablend.counterstrike.utils.*;
import me.zombie_striker.qg.guns.Gun;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static dev.danablend.counterstrike.Config.MAX_ROUNDS;
import static dev.danablend.counterstrike.enums.GameState.*;
import static dev.danablend.counterstrike.utils.Utils.sortByValue;


public class CounterStrike extends JavaPlugin {

    public static CounterStrike i;
    private GameState gameState = LOBBY;
    public ShopPhaseManager Shop;
    private GameCounter gameCount;

    public String Map = "";
    public MyBukkit myBukkit;

    public Hashtable HashWorlds = null;
    public Hashtable ResourceHash = new Hashtable();
    public Hashtable<String, Integer> VoteHash = new Hashtable();
    Collection<CSPlayer> csPlayers = new ArrayList<>();
    public Collection<CSPlayer> counterTerrorists = new ArrayList<CSPlayer>();
    public Collection<CSPlayer> terrorists = new ArrayList<CSPlayer>();
    Collection<CSPlayer> tempPlayerList = new ArrayList<CSPlayer>();
    Set<TestCommand> testCommands = new HashSet<TestCommand>();

    PluginManager pm = Bukkit.getPluginManager();
    ItemStack shopItem;

    private GameTimer timer;
    public Team counterTerroristsTeam;
    public Team terroristsTeam;
    private SQLiteConnection sqlite = null;
    private dev.danablend.counterstrike.runnables.PlayerUpdater pUpdate;

    public static final int RIFLE_SLOT = 0;
    public static final int PISTOL_SLOT = 1;
    public static final int KNIFE_SLOT = 2;
    public static final int GRENADE_SLOT = 3;
    public static final int TNT_SLOT = 4;
    public static final int RIFLE_AMO_SLOT = 6;
    public static final int PISTOL_AMO_SLOT = 7;
    public static final int SHOP_SLOT = 8;

    private String SpawnTerrorists;
    private String SpawnCounterTerrorists;
    private String Lobby;
    private String bombSiteAString;
    private String bombSiteBString;

    private Location SpawnTerroristsLocation;
    private Location SpawnCounterTerroristsLocation;
    private Location bombSiteA;
    private Location bombSiteB;

    private Object gameCounterTask;
    public IACenter botManager;

    public boolean randomMaps = false;
    public boolean alwaysDay = false;
    public boolean quitExitGame = false;
    public boolean showGameStatusTitle = true;
    public boolean standardTeamColours = false;
    public boolean modeValorant = false;
    public boolean activated = false;


    public void onEnable() {
        i = this;
        setup();
        SetupSignConfigs();

        if (myBukkit.checkGreater("1.21.1", Bukkit.getServer().getBukkitVersion()) == -1) {
            getLogger().severe(" You are using a Minecraft Server version with possible problems of data loss and known exploits, get informed and evaluate updating to at least 1.21.1");
        }

        try {
            myBukkit.runTaskLater(null, null, null, () -> new Metrics(this, 22650), 5);
        } catch (Exception e) {
            Utils.debug(ChatColor.RED + " Failed to register into Bstats");
        }

        botManager = ((de.tontito.iacenter.IACenter) getServer().getPluginManager().getPlugin("IACenter"));

        Utils.debug("Checking for Bot system availability: " + (botManager != null));

        myBukkit.runTaskLater(null, null, null, () -> {
            if (botManager != null) {

                if (bombSiteAString != null && botManager.isEnabled()) {
                    if (botManager != null) pm.registerEvents(botManager, this);
                    Utils.debug("Activating bot system...");
                } else {
                    Utils.debug("Bot system not available or missing map configurations...");
                    botManager = null;
                }
            }
        }, 40);

        myBukkit.UpdateChecker(true);

        Utils.debug("Enabled");
    }


    public void onDisable() {
        //delete tnt and label
        Bomb.cleanUp();

        if (botManager != null) botManager.terminateBots();
    }


    public boolean usingQualityArmory() {
        return Bukkit.getPluginManager().isPluginEnabled("QualityArmory");
    }


    public void setup() {

        Utils.debug("Preparing maps for game...");

        if (!CounterStrike.i.usingQualityArmory()) {
            Utils.debug("#####  QualityArmory not loaded, aborting... ");
            return;
        }

        myBukkit = new MyBukkit(this);

        setupConfig();

        sqlite = new SQLiteConnection(getDataFolder(), "CSMC.db");

        try (Connection conn = sqlite.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select id, nome, ifnull(modoCs,false) modoCs from mundos;")) {
            HashWorlds = new Hashtable();

            while (rs.next()) {
                String world = rs.getString("nome");
                Worlds md = new Worlds(rs.getInt("id"), world, Boolean.parseBoolean(rs.getString("modoCs")));
                HashWorlds.put(world, md);
                Utils.debug("Preloaded map " + world + "  " + md.modoCs);
            }
            Utils.debug("Loaded maps! ");
        } catch (SQLException e) {
            Utils.debug("Exception loading maps " + e.getMessage());
        }

        this.terroristsTeam = new Team(TeamEnum.TERRORISTS, terrorists);
        this.counterTerroristsTeam = new Team(TeamEnum.COUNTER_TERRORISTS, counterTerrorists);
        saveDefaultConfig();

        pUpdate = new PlayerUpdater(this);
        this.myBukkit.runTaskTimer(null, null, null, () -> pUpdate.run(), 20L, 20L);

        pm.registerEvents(new BlockPlaceListener(), this);
        pm.registerEvents(new BlockBreakListener(), this);
        pm.registerEvents(new FoodLevelChangeListener(), this);
        pm.registerEvents(new PlayerInteractListener(), this);
        pm.registerEvents(new PlayerItemDamageListener(), this);
        pm.registerEvents(new PlayerDropItemListener(), this);
        pm.registerEvents(new PlayerJoinListener(), this);
        pm.registerEvents(new PlayerDeathListener(), this);
        pm.registerEvents(new CustomPlayerDeathListener(), this);
        pm.registerEvents(new EntityDamageByEntityListener(), this);
        pm.registerEvents(new WeaponFireListener(), this);
        pm.registerEvents(new ShopListener(this), this);
        pm.registerEvents(new BlockIgniteListener(), this);
        pm.registerEvents(new EntityPickupItemListener(), this);
        pm.registerEvents(new InventoryClickListener(), this);
        pm.registerEvents(new PlayerChatControlListener(), this);

        getCommand("csmc").setExecutor(new CounterStrikeCommand(this));

        Utils.debug("Events loaded ");

        for (World w : Bukkit.getWorlds()) {

            if (HashWorlds != null) {
                Object obj = HashWorlds.get(w.getName());

                if (obj != null) {
                    Worlds md = (Worlds) obj;

                    if (!md.modoCs) {
                        this.myBukkit.runTask(null, null, null, () -> {
                            w.setGameRule(GameRule.NATURAL_REGENERATION, true);
                            w.setGameRule(GameRule.DO_MOB_SPAWNING, true);
                        });
                        continue;
                    }
                }
            }

            this.myBukkit.runTask(null, null, null, () -> {
                w.setGameRule(GameRule.NATURAL_REGENERATION, false);
                w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                w.setGameRule(GameRule.KEEP_INVENTORY, false);
            });
        }

        Utils.debug("Creating shop item...");
        shopItem = new ItemStack(Material.CHEST);
        ItemMeta meta = shopItem.getItemMeta();

        myBukkit.setMeta(meta, ChatColor.YELLOW + "(Right click to open shop)");

        shopItem.setItemMeta(meta);

        Config c = new Config();
        c.loadWeapons();

        //loads default configs first
        loadConfigs();

        new Shop();

        LoadDBRandomMaps(0);
        Map = ""; //no map really selected
        Utils.debug("Finished setting up...");
    }


    public void SaveDBCOnfig(String Map, String Conf, String location) {
        String result;

        //if has generalP loaded
        if (sqlite != null) {
            result = sqlite.select("select id from CSMaps where descr = '" + Map + "'");

            if (result == null || result.equals("")) {
                sqlite.checkLock("update mundos set modocs = 'true' where nome = '" + location.split(",")[0] + "'");

                Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(location.split(",")[0]);

                if (md != null && !md.modoCs) {
                    md.modoCs = true;
                }

                sqlite.checkLock("insert into CSMaps (descr) values ('" + Map + "')");
                result = sqlite.select("select id from CSMaps where descr = '" + Map + "'");
            }

            if (Conf.equals("Lobby")) {
                sqlite.checkLock("update csMaps set SpawnLobby = '" + location + "' where id = " + result);

            } else if (Conf.equals("Terrorist")) {
                sqlite.checkLock("update csMaps set SpawnTerrorists = '" + location + "' where id = " + result);

            } else if (Conf.equals("Counter")) {
                sqlite.checkLock("update csMaps set SpawnCounter = '" + location + "' where id = " + result);

            } else if (Conf.equals("A")) {
                sqlite.checkLock("update csMaps set A = '" + location + "' where id = " + result);

            } else if (Conf.equals("B")) {
                sqlite.checkLock("update csMaps set B = '" + location + "' where id = " + result);
            }
        }
    }


    public void LoadDBRandomMaps(int mapId) {

        if (sqlite != null) {

            if (mapId == 0) {
                String result = sqlite.select("select max(id) from CSMaps");

                if (result == null || result.equals("")) {
                    Utils.debug("#####  " + ChatColor.RED + "No maps loaded");
                    broadcastMessage(ChatColor.RED + "No maps loaded");
                    return;
                }

                Long Rand;
                String result1;

                while (true) {
                    Rand = Math.round(1 + Math.random() * Integer.parseInt(result));

                    result1 = sqlite.select("select descr from CSMaps where id = " + Rand);

                    if (result1 != null && !result1.equals("")) {
                        Map = result1;
                        Utils.debug("#####  " + ChatColor.WHITE + "Map " + result1 + " was randomly chosen");
                        broadcastMessage(ChatColor.WHITE + "Map " + result1 + " was randomly chosen");
                        break;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }

                mapId = Integer.parseInt(sqlite.select("select id from CSMaps where descr = '" + Map + "'"));
            } else {
                Map = sqlite.select("select descr from CSMaps where id = " + mapId);
                Utils.debug(">>>>>>>>>>>>>>>>>   Preparing voted map..." + Map);
            }

            Lobby = sqlite.select("select SpawnLobby from CSMaps where id = " + mapId);
            SpawnTerrorists = sqlite.select("select SpawnTerrorists from CSMaps where id = " + mapId);
            SpawnCounterTerrorists = sqlite.select("select SpawnCounter from CSMaps where id = " + mapId);

            bombSiteAString = sqlite.select("select A from CSMaps where id = " + mapId);
            bombSiteBString = sqlite.select("select B from CSMaps where id = " + mapId);

            SpawnCounterTerroristsLocation = null;
            SpawnTerroristsLocation = null;
            bombSiteA = null;
            bombSiteB = null;

            if (mapId < 0 || SpawnTerrorists == null || getTerroristSpawn(false) == null) {
                Utils.debug("Cant load map or spawn...");
                return;
            }

        } else {

            loadConfigs();
        }

    }


    private void Preparemap() {

        if (getTerroristsTeam().getWins() + getTerroristsTeam().getLosses() == 0) {
            //first round
            if (VoteHash.size() > 0) {
                LoadDBRandomMaps(sortByValue(VoteHash));

                VoteHash.clear();
            } else {
                LoadDBRandomMaps(0);
            }

        } else {
            if (randomMaps) {
                LoadDBRandomMaps(0);
            }
        }

        Location baseLocation = getCounterTerroristSpawn(false);
        World world = baseLocation.getWorld();

        if (HashWorlds != null) {
            Object obj = HashWorlds.get(world.getName());

            if (obj != null) {
                Worlds md = (Worlds) obj;

                if (!md.modoCs) {
                    return;
                }
            }
        }

        this.myBukkit.runTask(null, null, null, () -> {
            if (alwaysDay) {
                Utils.debug("No day cicle...");
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                world.setTime(0);
            } else {
                Utils.debug("With day cicle...");
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            }
        });


        myBukkit.runTask(null, baseLocation, null, () -> {

            for (Entity ent : world.getEntities()) {
                if (!(ent instanceof Player) && !ent.isDead()) {
                    ent.remove();
                }
            }

        });

    }

    //for config load/reload
    public void loadConfigs() {
        this.getConfig().options().copyDefaults(true);

        //alrealy loaded map
        Utils.debug("Loading static map and locations...");
        Lobby = getConfig().getString("lobby-location");
        SpawnTerrorists = getConfig().getString("spawn-locations.terrorist");
        SpawnCounterTerrorists = getConfig().getString("spawn-locations.counterterrorist");
        bombSiteAString = getConfig().getString("bomb-locations.A");
        bombSiteBString = getConfig().getString("bomb-locations.B");

        SpawnCounterTerroristsLocation = null;
        SpawnTerroristsLocation = null;
        bombSiteA = null;
        bombSiteB = null;

        randomMaps = getConfig().getBoolean("randomMaps", false);
        alwaysDay = getConfig().getBoolean("alwaysDay", true);
        quitExitGame = getConfig().getBoolean("quitExitGame", false);
        showGameStatusTitle = getConfig().getBoolean("showGameStatusTitle", true);
        standardTeamColours = getConfig().getBoolean("standardTeamColours", false);
        modeValorant = getConfig().getBoolean("modeValorant", false);

        String key = getConfig().getString("upgradeKey");
        String decodedString = new String(Base64.decodeBase64("ZVc5MUlHdHVNSGNnZEdobElIUnlkWFJvUFE9PT0"));
        decodedString = new String(Base64.decodeBase64(decodedString.substring(0, decodedString.length() - 2)));
        decodedString = decodedString.substring(0, decodedString.length() - 1);
        if (key.equals(decodedString)) activated = true;

        Utils.debug(activated + " trueTeamColours:" + standardTeamColours + "  informGameStatus:" + showGameStatusTitle);
    }

    //more customizable configs
    private void setupConfig() {
        FileConfiguration config = getConfig();
        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        } else {
            config.addDefault("upgradeKey", "xpto");
            config.options().copyDefaults(true);
            saveConfig();
            return;
        }


        config.addDefault("randomMaps", false);
        config.addDefault("alwaysDay", true);
        config.addDefault("quitExitGame", false);
        config.addDefault("showGameStatusTitle", true);
        config.addDefault("standardTeamColours", false);
        config.addDefault("modeValorant", false);

        config.options().copyDefaults(true);
        saveConfig();
    }


    public ItemStack getKnife() {
        ItemStack knife = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = knife.getItemMeta();

        myBukkit.setMeta(meta, ChatColor.GRAY + "Standard Knife");

        knife.setItemMeta(meta);
        return knife;
    }


    public ItemStack getShopItem() {
        return shopItem;
    }


    public Set<TestCommand> getTestCommands() {
        Utils.debug("Getting test commands...");
        return testCommands;
    }


    public void startGame() {
        Utils.debug("---> Starting game initiated...");

        if (modeValorant) {
            Config.terroristShopName = "Buy Menu - Attackers";
            Config.counterTerroristShopName = "Buy Menu - Defenders";
        } else {
            Config.terroristShopName = "Buy Menu - Terrorist";
            Config.counterTerroristShopName = "Buy Menu - Counter Terrorist";
        }

        Preparemap();

        //needed to create teams and visibility
        Scoreboard board1 = Bukkit.getScoreboardManager().getMainScoreboard();
        if (board1.getTeam("team1") != null) board1.getTeam("team1").unregister();
        if (board1.getTeam("team2") != null) board1.getTeam("team2").unregister();

        gameState = GameState.STARTING;

        setupPlayers();

        if (Shop == null) {
            Shop = new ShopPhaseManager(this);
            pm.registerEvents(Shop, this);
        } else {
            Utils.debug(" ###### avoided duplication thread to Shop ");
        }

        Utils.debug("Game successfully started...");
    }


    public void restartGame(Team winnerTeam) {
        Utils.debug("Restarting game initiated...");
        Team loserTeam = (winnerTeam.getTeam().equals(TeamEnum.TERRORISTS)) ? getCounterTerroristsTeam() : getTerroristsTeam();

        Bomb.cleanUp();

        Integer MVPscore = 0;
        Integer Delay = 1;

        String MVPPlayer = "";
        CSPlayer csMVPPlayer = null;

        for (CSPlayer csplayer : loserTeam.getCsPlayers()) {
            csplayer.setMoney(csplayer.getMoney() + Config.MONEY_ON_LOSS);
            csplayer.getPlayer().sendMessage(ChatColor.GREEN + "+ $" + Config.MONEY_ON_LOSS);
        }
        for (CSPlayer csplayer : winnerTeam.getCsPlayers()) {
            csplayer.setMoney(csplayer.getMoney() + Config.MONEY_ON_VICTORY);
            csplayer.getPlayer().sendMessage(ChatColor.GREEN + "+ $" + Config.MONEY_ON_VICTORY);

            if (csplayer.gettempMVP() > MVPscore) {
                MVPscore = csplayer.gettempMVP();
                csMVPPlayer = csplayer;
            }
        }

        if (csMVPPlayer != null) {
            csMVPPlayer.setMVP(csMVPPlayer.getMVP() + 1);
            MVPPlayer = ChatColor.AQUA + " Round's MVP " + csMVPPlayer.getPlayer().getName();
        }
        winnerTeam.addVictory();
        loserTeam.addLoss();

        String winnerText = ChatColor.valueOf(winnerTeam.getColour()) + "Team " + winnerTeam.getColour() + " wins." + MVPPlayer;

        Utils.debug("Restarting game initiated..." + winnerText + "     " + winnerTeam.getWins());

        if (winnerTeam.getWins() == (MAX_ROUNDS / 2) + 1) {

            gameState = LOBBY;
            PacketUtils.sendTitleAndSubtitleToInGame(winnerText, ChatColor.AQUA + "They also won the whole game! (Left click to join new game)", 0, 10, 1);
            PacketUtils.sendActionBarToInGame(winnerText);

            myBukkit.runTaskLater(null, getLobbyLocation(), null, () -> {
                FinishGame(winnerTeam, loserTeam);
            }, 20);

            return; //stops game

        } else if (winnerTeam.getWins() + winnerTeam.getLosses() == MAX_ROUNDS) {

            gameState = LOBBY;
            PacketUtils.sendTitleAndSubtitleToInGame(winnerText, ChatColor.AQUA + "But scores are even! (Left click to join new game)", 0, 10, 1);
            PacketUtils.sendActionBarToInGame(winnerText);

            myBukkit.runTaskLater(null, getLobbyLocation(), null, () -> {
                FinishGame(winnerTeam, loserTeam);
            }, 20);

            return; //stops game

        } else {
            PacketUtils.sendTitleAndSubtitleToInGame(winnerText, ChatColor.YELLOW + "The next round will start shortly.", 0, 6, 1);
        }

        gameState = LOBBY;

        PacketUtils.sendActionBarToInGame(winnerText);

        if (winnerTeam.getWins() + winnerTeam.getLosses() == (Math.round(MAX_ROUNDS / 2))) {

            Delay = 5;

            Utils.debug("################################# swap teams ##################################");

            if (winnerTeam.getTeam().equals(TeamEnum.TERRORISTS)) {
                winnerTeam.setTeam(TeamEnum.COUNTER_TERRORISTS);
                loserTeam.setTeam(TeamEnum.TERRORISTS);

                this.counterTerroristsTeam = winnerTeam;
                this.terroristsTeam = loserTeam;

                for (CSPlayer csplayer : loserTeam.getCsPlayers()) {
                    csplayer.setTeam(TeamEnum.TERRORISTS);
                }

                for (CSPlayer csplayer : winnerTeam.getCsPlayers()) {
                    csplayer.setTeam(TeamEnum.COUNTER_TERRORISTS);
                }

            } else {
                winnerTeam.setTeam(TeamEnum.TERRORISTS);
                loserTeam.setTeam(TeamEnum.COUNTER_TERRORISTS);

                this.counterTerroristsTeam = loserTeam;
                this.terroristsTeam = winnerTeam;

                for (CSPlayer csplayer : loserTeam.getCsPlayers()) {
                    csplayer.setTeam(TeamEnum.COUNTER_TERRORISTS);
                }

                for (CSPlayer csplayer : winnerTeam.getCsPlayers()) {
                    csplayer.setTeam(TeamEnum.TERRORISTS);
                }
            }

            tempPlayerList = counterTerrorists;
            counterTerrorists = terrorists;
            terrorists = tempPlayerList;

            for (CSPlayer csplayer : loserTeam.getCsPlayers()) {
                csplayer.setMoney(Config.STARTING_MONEY);

                if (csplayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
                    PacketUtils.sendTitleAndSubtitle(csplayer.getPlayer(), ChatColor.BLUE + "You are NOW a Counter Terrorist", ChatColor.BLUE + "Defend the sites from terrorists, defuse the bomb.", 1, 5, 1);
                } else {
                    PacketUtils.sendTitleAndSubtitle(csplayer.getPlayer(), ChatColor.RED + "You are NOW a Terrorist", ChatColor.RED + "Plant the bomb on the sites, have it explode.", 1, 5, 1);
                }
            }

            for (CSPlayer csplayer : winnerTeam.getCsPlayers()) {
                csplayer.setMoney(Config.STARTING_MONEY);

                if (csplayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
                    PacketUtils.sendTitleAndSubtitle(csplayer.getPlayer(), ChatColor.BLUE + "You are NOW a Counter Terrorist", ChatColor.BLUE + "Defend the sites from terrorists, defuse the bomb.", 1, 5, 1);
                } else {
                    PacketUtils.sendTitleAndSubtitle(csplayer.getPlayer(), ChatColor.RED + "You are NOW a Terrorist", ChatColor.RED + "Plant the bomb on the sites, have it explode.", 1, 5, 1);
                }
            }

            for (CSPlayer csplayer : getCSPlayers()) {
                Player player = csplayer.getPlayer();
                player.getInventory().clear();
            }
        }

        for (CSPlayer csplayer : getCSPlayers()) { //who ever is playing is teleported

            Player player = csplayer.getPlayer();

            if (player.isOnline() || csplayer.isNPC()) {
                String world = player.getWorld().getName();

                if (CounterStrike.i.HashWorlds != null) {
                    Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

                    if (md != null && !md.modoCs) {
                        continue;
                    }
                }

                myBukkit.playerTeleport(player, getLobbyLocation());
            }
        }

        //use delay
        StartGameCounter(Delay);

        Utils.debug("Game successfully restarted...");
    }


    public void FinishGame(Team winnerTeam, Team loserTeam) {

        gameState = LOBBY;

        winnerTeam.setLosses(0);
        winnerTeam.setWins(0);
        winnerTeam.setColour("WHITE");
        loserTeam.setLosses(0);
        loserTeam.setWins(0);
        loserTeam.setColour("WHITE");

        for (CSPlayer csPlayer : csPlayers) {
            Player player = csPlayer.getPlayer();

            if (csPlayer != null) {
                getPlayerUpdater().deleteScoreBoards(player);

                //needs delay or will give arraylist exception
                myBukkit.runTaskLater(player, null, null, () -> {
                    csPlayer.clear();
                }, 20);
            }

            if (player.isOnline() || csPlayer.isNPC()) {
                player.getInventory().clear();

                //Clears colors
                myBukkit.setPlayerListName(player, ChatColor.WHITE + player.getName());

                String world = player.getWorld().getName();

                if (CounterStrike.i.HashWorlds != null) {
                    Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);
                    if (md != null && !md.modoCs) continue;
                }

                myBukkit.playerTeleport(player, getLobbyLocation());
                player.setGameMode(GameMode.SURVIVAL);
            }
        }

        csPlayers.clear();
        getPlayerUpdater().playersWithScoreboard = new ArrayList<>(); //list reset

        if (botManager != null) botManager.terminateBots();

        Map = "";
    }


    public void setupPlayers() {

        for (CSPlayer csPlayer : terrorists) {
            Player player = csPlayer.getPlayer();

            setupTeams(player, "team1");

            csPlayer.setColourOpponent(counterTerroristsTeam.getColour());

            //put in base
            myBukkit.playerTeleport(player, getTerroristSpawn(true));

            if (player.getInventory().getItem(PISTOL_SLOT) == null || csPlayer.getPistol() == null) {
                player.getInventory().setItem(PISTOL_SLOT, Weapon.getByName("t-pistol-default").getItem());
            }
            giveEquipment(csPlayer);
            csPlayer.settempMVP(0);
        }

        for (CSPlayer csPlayer : counterTerrorists) {
            Player player = csPlayer.getPlayer();

            setupTeams(player, "team2");

            csPlayer.setColourOpponent(terroristsTeam.getColour());

            //put in base
            myBukkit.playerTeleport(player, getCounterTerroristSpawn(true));

            if (player.getInventory().getItem(PISTOL_SLOT) == null || csPlayer.getPistol() == null) {
                player.getInventory().setItem(PISTOL_SLOT, Weapon.getByName("ct-pistol-default").getItem());
            }
            giveEquipment(csPlayer);
            csPlayer.settempMVP(0);
        }

        //for everyone
        for (CSPlayer csplayer : getCSPlayers()) {
            Player player = csplayer.getPlayer();

            player.setGameMode(GameMode.SURVIVAL);
            player.setFlying(false);
            player.setAllowFlight(false);

            player.getInventory().setItem(KNIFE_SLOT, getKnife());
            //  player.getInventory().setItem(TNT_SLOT, getShopItem()); //check PlayerUpdater

            if (modeValorant) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
            else player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);

            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.setFoodLevel(8); //was 6
            player.setGameMode(GameMode.SURVIVAL);
            player.setFallDistance(1);


            CSPlayer cp = this.getCSPlayer(player, false, null);
            Weapon rifle = cp.getRifle();
            Weapon pistol = cp.getPistol();

            if (rifle != null) {
                Gun gun = me.zombie_striker.qg.api.QualityArmory.getGunByName(rifle.getName());
                Gun.updateAmmo(gun, player.getInventory().getItem(RIFLE_SLOT), rifle.getMagazineCapacity());

                Utils.debug(rifle.getMagazineCapacity() + " ###### rifle " + rifle.getName());

                ItemStack ammo = gun.getAmmoType().getItemStack().clone();
                ammo.setAmount((rifle.getMagazines() - 1) * rifle.getMagazineCapacity());
                player.getInventory().setItem(RIFLE_AMO_SLOT, ammo);
            }

            if (pistol != null) {
                Gun gun = me.zombie_striker.qg.api.QualityArmory.getGunByName(pistol.getName());
                Gun.updateAmmo(gun, player.getInventory().getItem(PISTOL_SLOT), pistol.getMagazineCapacity());

                ItemStack ammo = gun.getAmmoType().getItemStack().clone();
                ammo.setAmount((pistol.getMagazines() - 1) * pistol.getMagazineCapacity());
                player.getInventory().setItem(PISTOL_AMO_SLOT, ammo);
            }

            player.getInventory().remove(Material.TNT);
        }

        if (!terrorists.isEmpty()) {
            int rand = new Random().nextInt(terrorists.toArray().length);

            CSPlayer playerWithBomb = (CSPlayer) terrorists.toArray()[rand];
            playerWithBomb.getPlayer().getInventory().setItem(TNT_SLOT, CSUtil.getBombItem());
            Utils.debug("------> Bomb with player " + playerWithBomb.getPlayer().getName() + "  rand:" + rand);
        }

    }


    public void setupTeams(Player player, String team) {
        //it is needed for teams and visibility
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        org.bukkit.scoreboard.Team myTeam = board.getTeam(team);

        if (!myBukkit.isFolia() && board != null) {
            if (myTeam == null) {
                board.registerNewTeam(team);
                myTeam = board.getTeam(team);
                myTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.FOR_OTHER_TEAMS); //the command is hideforotherteams in vanilla
            }

            myBukkit.teamAddEntity(myTeam, player);
        }
    }


    public GameTimer getGameTimer() {
        return timer;
    }


    public void setGameTimer(GameTimer gametimer) {
        Utils.debug("Setting game timer...");
        this.timer = gametimer;
    }


    public CSPlayer getCSPlayer(Player player, boolean create, String colour) {

        for (CSPlayer csPlayer : csPlayers) {
            if (csPlayer.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                return csPlayer;
            }
        }

        if (create) {
            CSPlayer csp = new CSPlayer(this, player, colour);
            String resPackName = player.getName() + "RES";

            //online filters NPCs
            if (player.isOnline() && (ResourceHash.get(resPackName) == null || ResourceHash.get(resPackName).equals("DEFAULT"))) {
                ResourceHash.put(resPackName, "QUALITY");

                loadResourcePack(player, "https://github.com/ZombieStriker/QualityArmory-Resourcepack/releases/download/latest/QualityArmory.zip", "3a34fc09dcc6f009aa05741f8ab487dd17b13eaf");
            }

            Utils.debug("Returning a new CSPlayer...");
            return csp;
        } else {
            return null;
        }
    }


    public Collection<CSPlayer> getCSPlayers() {
        return csPlayers;
    }


    public Collection<CSPlayer> getCounterTerrorists() {
        Utils.debug("Getting Counter Terrorists...");
        return counterTerrorists;
    }


    public Collection<CSPlayer> getTerrorists() {
        Utils.debug("Getting Terrorists...");
        return terrorists;
    }


    public Team getCounterTerroristsTeam() {
        //Utils.debug("Getting CT Team...");
        return counterTerroristsTeam;
    }


    public Team getTerroristsTeam() {
        //Utils.debug("Getting T Team...");
        return terroristsTeam;
    }


    public GameState getGameState() {
        //  Utils.debug("Getting GameState...");
        return gameState;
    }


    public void setGameState(GameState newGameState) {
        gameState = newGameState;
    }


    public boolean isTNTDropped() {
        Location loc = this.getTerroristSpawn(false);

        for (Entity ent : loc.getWorld().getEntities()) {
            if (!(ent instanceof Player) && (ent.getType().equals(Material.TNT) || ent.getName().equals("TNT"))) {
                return true;
            }
        }

        return false;
    }


    public Location getLobbyLocation() {
        if (Lobby == null) return null;

        String locRaw = Lobby;
        String[] locList = locRaw.split(",");
        World world = Bukkit.getWorld(locList[0]);
        double x = Double.parseDouble(locList[1]);
        double y = Double.parseDouble(locList[2]);
        double z = Double.parseDouble(locList[3]);
        float yaw = Float.parseFloat(locList[4]);
        float pitch = Float.parseFloat(locList[5]);

        Double init = Math.random();

        if (init > 0.5) {
            init = -1.0;
        } else {
            init = 1.0;
        }

        x = x + (init * 2.0 * Math.random());
        z = z + (init * 2.0 * Math.random());

        return new Location(world, x, y, z, yaw, pitch);
    }


    public Location getTerroristSpawn(boolean rand) {

        if (SpawnTerroristsLocation != null && !rand) return SpawnTerroristsLocation;
        //cache...

        String locRaw = SpawnTerrorists;
        String[] locList = locRaw.split(",");
        World world = Bukkit.getWorld(locList[0]);
        double x = Double.parseDouble(locList[1]);
        double y = Double.parseDouble(locList[2]);
        double z = Double.parseDouble(locList[3]);
        float yaw = Float.parseFloat(locList[4]);
        float pitch = Float.parseFloat(locList[5]);

        if (rand) {
            Double init = Math.random();

            if (init > 0.5) {
                init = -1.0;
            } else {
                init = 1.0;
            }

            x = x + (init * 2.0 * Math.random());
            z = z + (init * 2.0 * Math.random());
        }

        if (world == null) {
            Utils.debug("Game world not loaded... for " + SpawnTerrorists);
            return null;
        }

        SpawnTerroristsLocation = new Location(world, x, y, z, yaw, pitch);
        return SpawnTerroristsLocation;
    }


    public Location getCounterTerroristSpawn(boolean rand) {

        if (SpawnCounterTerroristsLocation != null && !rand) return SpawnCounterTerroristsLocation;

        String locRaw = SpawnCounterTerrorists;
        String[] locList = locRaw.split(",");
        World world = Bukkit.getWorld(locList[0]);
        double x = Double.parseDouble(locList[1]);
        double y = Double.parseDouble(locList[2]);
        double z = Double.parseDouble(locList[3]);
        float yaw = Float.parseFloat(locList[4]);
        float pitch = Float.parseFloat(locList[5]);

        if (rand) {
            Double init = Math.random();

            if (init > 0.5) {
                init = -1.0;
            } else {
                init = 1.0;
            }

            x = x + (init * 2.0 * Math.random());
            z = z + (init * 2.0 * Math.random());
        }

        SpawnCounterTerroristsLocation = new Location(world, x, y, z, yaw, pitch);
        return SpawnCounterTerroristsLocation;
    }


    public Location bombSiteA() {
        if (bombSiteAString == null) return null;
        if (bombSiteA != null) return bombSiteA;

        Utils.debug("Getting bombSiteAString...");
        String locRaw = bombSiteAString;
        String[] locList = locRaw.split(",");
        World world = Bukkit.getWorld(locList[0]);
        double x = Double.parseDouble(locList[1]);
        double y = Double.parseDouble(locList[2]);
        double z = Double.parseDouble(locList[3]);
        float yaw = Float.parseFloat(locList[4]);
        float pitch = Float.parseFloat(locList[5]);

        bombSiteA = new Location(world, x, y, z, yaw, pitch);

        return bombSiteA;
    }


    public Location bombSiteB() {
        if (bombSiteBString == null) return null;
        if (bombSiteB != null) return bombSiteB;

        Utils.debug("Getting bombSiteAString...");
        String locRaw = bombSiteBString;
        String[] locList = locRaw.split(",");
        World world = Bukkit.getWorld(locList[0]);
        double x = Double.parseDouble(locList[1]);
        double y = Double.parseDouble(locList[2]);
        double z = Double.parseDouble(locList[3]);
        float yaw = Float.parseFloat(locList[4]);
        float pitch = Float.parseFloat(locList[5]);

        bombSiteB = new Location(world, x, y, z, yaw, pitch);

        return bombSiteB;
    }


    public void giveEquipment(CSPlayer csPlayer) {

        if (true) return;

        Utils.debug("Giving equipment to csplayer...");

        Color mycolor;

        if (csPlayer.getColour().equals("RED")) {
            mycolor = Color.RED;
        } else if (csPlayer.getColour().equals("BLUE")) {
            mycolor = Color.BLUE;
        } else if (csPlayer.getColour().equals("GREEN")) {
            mycolor = Color.GREEN;
        } else if (csPlayer.getColour().equals("AQUA")) {
            mycolor = Color.AQUA;
        } else {
            mycolor = Color.YELLOW;
        }

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
        helmetMeta.setColor(mycolor);
        helmet.setItemMeta(helmetMeta);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        chestplateMeta.setColor(mycolor);
        chestplate.setItemMeta(chestplateMeta);

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        leggingsMeta.setColor(mycolor);
        leggings.setItemMeta(leggingsMeta);

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(mycolor);
        boots.setItemMeta(bootsMeta);

        Player player = csPlayer.getPlayer();
        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
    }


    public dev.danablend.counterstrike.runnables.PlayerUpdater getPlayerUpdater() {
        return pUpdate;
    }

    //Broadcast to current players
    public void broadcastMessage(String message) {
        for (CSPlayer csplayer : CounterStrike.i.getCSPlayers()) {
            csplayer.getPlayer().sendMessage(message);
        }
    }


    public void loadResourcePack(Player player, String resourse, String hash) {
        Utils.debug("Change to pack " + resourse);
        myBukkit.playerSetResourcePack(player, resourse, hash);
    }


    public void returnPlayertoGame(CSPlayer csplay) {

        if (csplay == null) {
            return;
        }

        boolean running = true;

        if ((CounterStrike.i.getGameState().equals(GameState.LOBBY) || CounterStrike.i.getGameState().equals(GameState.WAITING))) {
            running = false;
        }

        Player player = csplay.getPlayer();
        String resPackName = player.getName() + "RES";

        //isOnline filters NPCs
        if (player.isOnline() && (ResourceHash.get(resPackName) == null || ResourceHash.get(resPackName).equals("DEFAULT"))) {
            ResourceHash.put(resPackName, "QUALITY");

            loadResourcePack(player, "https://github.com/ZombieStriker/QualityArmory-Resourcepack/releases/download/latest/QualityArmory.zip", "3a34fc09dcc6f009aa05741f8ab487dd17b13eaf");
        }


        if (modeValorant) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        else player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);

        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(8); //was 6
        player.setGameMode(GameMode.SURVIVAL);
        player.setFallDistance(1);


        if (terrorists.contains(csplay)) {
            csplay.setColourOpponent(counterTerroristsTeam.getColour());

            if (running) myBukkit.playerTeleport(player, getTerroristSpawn(true));

            if (CounterStrike.i.gameState == GameState.SHOP) {
                boolean noOneHasTNT = false;

                for (CSPlayer csPlayer : terrorists) {
                    if (csPlayer.getBomb() != null) {
                        noOneHasTNT = true;
                        break;
                    }
                }

                if (!noOneHasTNT && !isTNTDropped()) {
                    //Utils.debug("Backup TNT");
                    csplay.getPlayer().getInventory().setItem(TNT_SLOT, CSUtil.getBombItem());
                }
            }
        }

        if (counterTerrorists.contains(csplay)) {
            csplay.setColourOpponent(terroristsTeam.getColour());
            //Coloca na base contra
            if (running) myBukkit.playerTeleport(player, getCounterTerroristSpawn(true));
        }

        if (!csplay.isNPC()) getPlayerUpdater().setScoreBoard(csplay);

        if (!running) myBukkit.playerTeleport(player, getLobbyLocation());

    }


    public void StartGameCounter(int delay) {
        if (!myBukkit.isCancelled(gameCounterTask)) return;

        Location mylobby = getLobbyLocation();

        if (botManager != null) {
            myBukkit.runTaskLater(null, mylobby, null, () -> {
                botManager.setMain(this);
                botManager.lauchBots(3);
            }, 20);
        }

        if (delay == 0) delay = 1; //default delay
        if (gameCount == null) gameCount = new GameCounter(this);

        gameCounterTask = CounterStrike.i.myBukkit.runTaskTimer(null, null, null, () -> gameCount.run(), delay * 20L, 20L);
        gameState = GameState.WAITING;
    }


    public void StopGameCounter() {
        if (myBukkit.isCancelled(gameCounterTask)) return; //not running skip

        myBukkit.cancelTask(gameCounterTask);
        gameCounterTask = null;
    }

    //ideas todo or not todo
    public void SetupSignConfigs() {

        if (!activated) return;

        String locRaw = Lobby;
        String[] locList = locRaw.split(",");
        World world = Bukkit.getWorld(locList[0]);
        double x = Double.parseDouble(locList[1]);
        double y = Double.parseDouble(locList[2]);
        double z = Double.parseDouble(locList[3]);
        float yaw = Float.parseFloat(locList[4]);
        float pitch = Float.parseFloat(locList[5]);

        Location loc = new Location(world, x, y, z, yaw, pitch);


        String result = sqlite.select("select max(id) from CSMaps");

        if (result == null || result.equals("")) {
            Utils.debug("#####  " + ChatColor.RED + "No maps loaded");
            return;
        }

        loc.add(-Integer.parseInt(result), 2, -2);

        String result1;

        for (int i = 1; i <= Integer.parseInt(result); i++) {
            result1 = sqlite.select("select descr from CSMaps where id = " + i);

            if (result1 == null || result1.equals("")) continue;

            Block block = loc.getBlock();
            block.setType(Material.BIRCH_HANGING_SIGN);
            BlockData blockData = block.getBlockData();
            block.setBlockData(blockData);

            Sign sign = (Sign) block.getState();
            sign.setLine(0, "[CSMC]");
            sign.setLine(1, "Vote for");
            sign.setLine(2, result1);
            sign.setLine(3, "" + i);
            sign.update();
            sign.setEditable(false);

            loc.add(1, 0, 0);
            block = loc.getBlock();
            block.setType(Material.AIR);
            loc.add(1, 0, 0);
            block.setType(Material.AIR);
        }

    }


    public void leaveGame(CSPlayer csplayer) {

        if (csplayer == null) {
            return;
        }

        Player player = csplayer.getPlayer();

        if (csplayer.isNPC()) {
            getPlayerUpdater().deleteScoreBoards(player);
            csplayer.clear();
            return;
        }

        if (quitExitGame) {
            getPlayerUpdater().deleteScoreBoards(player);
            csplayer.clear();
        } else {
            //if has bomb drops it
            if (csplayer != null && csplayer.getBomb() != null && (gameState.equals(SHOP) || gameState.equals(RUN))) {

                ItemStack item = player.getInventory().getItem(TNT_SLOT);

                if (item != null) {
                    //Utils.debug("Dropping bomb ");
                    player.getInventory().remove(item);
                    Item itemDropped = player.getWorld().dropItemNaturally(player.getLocation(), item);
                    itemDropped.setPickupDelay(40);
                }
            }
        }

        int serverSize = CounterStrike.i.getCSPlayers().size();

        if ((serverSize == 0 || getServer().getOnlinePlayers().size() == 0) && quitExitGame) {

            if (botManager != null) {
                myBukkit.runTaskLater(null, getLobbyLocation(), null, () -> {
                    botManager.terminateBots();
                }, 20);
            }
        }
    }

}
