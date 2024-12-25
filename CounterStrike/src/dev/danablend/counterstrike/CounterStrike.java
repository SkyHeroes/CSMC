package dev.danablend.counterstrike;

import dev.danablend.counterstrike.commands.CounterStrikeCommand;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.Team;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.database.SQLiteConnection;
import dev.danablend.counterstrike.database.Worlds;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.listeners.*;
import dev.danablend.counterstrike.runnables.*;
import dev.danablend.counterstrike.shop.Shop;
import dev.danablend.counterstrike.shop.ShopListener;
import dev.danablend.counterstrike.tests.TestCommand;
import dev.danablend.counterstrike.utils.*;
import me.zombie_striker.qg.guns.Gun;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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


public class CounterStrike extends JavaPlugin {

    public static CounterStrike i;
    public GameState gameState = GameState.LOBBY;
    public ShopPhaseManager Shop;
    private GameCounter gameCount;
    public Hashtable HashWorlds = null;
    public Hashtable ResourseHash = new Hashtable();
    public String Map = "";
    public MyBukkit myBukkit;
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
    private String SpawnTerrorists;
    private String SpawnCounterTerrorists;
    private String Lobby;
    private Location SpawnTerroristsLocation;
    private Location SpawnCounterTerroristsLocation;
    private Object gameCounterTask;

    public boolean randomMaps = false;
    public boolean alwaysDay = false;
    public boolean quitExitGame = false;


    public void onEnable() {
        i = this;
        setup();

        try {
            myBukkit.runTaskLater(null, null, null, () -> new Metrics(this, 22650), 5);
        } catch (Exception e) {
            getLogger().info(ChatColor.RED + " Failed to register into Bstats");
        }

        Scoreboard board1 = Bukkit.getScoreboardManager().getMainScoreboard();
        if (board1.getTeam("team1") != null) board1.getTeam("team1").unregister();
        if (board1.getTeam("team2") != null) board1.getTeam("team2").unregister();
    }

    public void onDisable() {
        //delete tnt and label
        Bomb.cleanUp();
    }


    public boolean usingQualityArmory() {
        return Bukkit.getPluginManager().isPluginEnabled("QualityArmory");
    }


    public void setup() {

        Utils.debug("Preparing maps for game...");

        if (!CounterStrike.i.usingQualityArmory()) {
            Utils.debug("#####  QualityArmory not loaded... ");
            return;
        }

        myBukkit = new MyBukkit(this);

        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) {
            setupConfig();
        }

        sqlite = new SQLiteConnection(getDataFolder(), "CSMC.db");

        try (Connection conn = sqlite.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select id, nome, ifnull(modoCs,false) modoCs from mundos;")) {
            HashWorlds = new Hashtable();

            while (rs.next()) {
                String world = rs.getString("nome");
                Worlds md = new Worlds(rs.getInt("id"), world, Boolean.parseBoolean(rs.getString("modoCs")));
                HashWorlds.put(world, md);
                Utils.debug("Loaded map " + world + "  " + md.modoCs);
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
                        this.myBukkit.runTask(null, null, null, () -> w.setGameRule(GameRule.NATURAL_REGENERATION, true));
                        continue;
                    }
                }
            }

            this.myBukkit.runTask(null, null, null, () -> {
                w.setGameRule(GameRule.NATURAL_REGENERATION, false);
                w.setGameRule(GameRule.KEEP_INVENTORY, false);
            });
        }

        Utils.debug("Creating shop item...");
        shopItem = new ItemStack(Material.CHEST);
        ItemMeta meta = shopItem.getItemMeta();

        myBukkit.setMeta(meta,ChatColor.YELLOW + "(Right click to open shop)");

        shopItem.setItemMeta(meta);

        Config c = new Config();
        c.loadWeapons();

        randomMaps = getConfig().getBoolean("randomMaps", false);
        alwaysDay = getConfig().getBoolean("alwaysDay", true);
        quitExitGame = getConfig().getBoolean("quitExitGame", false);

        new Shop();

        LoadDBRandomMaps();
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
            }
        }
    }


    public void LoadDBRandomMaps() {

        if (sqlite != null) {

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

                //hardcoded force map
                //Rand = 1L;

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

            LoadDBMapConfigs(result1);
            return;
        }

        Lobby = getConfig().getString("lobby-location");
        SpawnTerrorists = getConfig().getString("spawn-locations.terrorist");
        SpawnCounterTerrorists = getConfig().getString("spawn-locations.counterterrorist");

        SpawnCounterTerroristsLocation = null;
        SpawnTerroristsLocation = null;

        myBukkit.runTaskLater(null, getTerroristSpawn(false), null, () -> Preparemap(), 20);

    }


    public void LoadDBMapConfigs(String Map) {

        Integer Rand = Integer.parseInt(sqlite.select("select id from CSMaps where descr = '" + Map + "'"));

        Lobby = sqlite.select("select SpawnLobby from CSMaps where id = " + Rand);
        SpawnTerrorists = sqlite.select("select SpawnTerrorists from CSMaps where id = " + Rand);
        SpawnCounterTerrorists = sqlite.select("select SpawnCounter from CSMaps where id = " + Rand);

        SpawnCounterTerroristsLocation = null;
        SpawnTerroristsLocation = null;

        if (Rand < 0 || SpawnTerrorists == null || getTerroristSpawn(false) == null) {
            Utils.debug("Cant load map or spawn...");
            return;
        }

        myBukkit.runTaskLater(null, getTerroristSpawn(false), null, () -> Preparemap(), 20);

    }


    private void Preparemap() {

        Utils.debug("Preparing map...");

        for (World w : Bukkit.getWorlds()) {

            if (HashWorlds != null) {
                Object obj = HashWorlds.get(w.getName());

                if (obj != null) {
                    Worlds md = (Worlds) obj;

                    if (!md.modoCs) {
                        continue;
                    }
                }
            }

            this.myBukkit.runTask(null, null, null, () -> {
                if (alwaysDay) {
                    Utils.debug("No day cicle...");
                    w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                    w.setTime(0);
                } else {
                    Utils.debug("With day cicle...");
                    w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                }
            });
        }

        myBukkit.runTask(null, getCounterTerroristSpawn(false), null, () -> {

            Location loc = this.getCounterTerroristSpawn(false);

            for (Entity ent : loc.getWorld().getEntities()) {

                if (!(ent instanceof Player) && !ent.isDead()) {
                    //   Utils.debug(ent.getName() + " bye2");
                    ent.remove();
                }
                if (!(ent instanceof Player) && ent.getType().equals(Material.TNT)) {
                    Utils.debug(ent.getName() + " removes TNT? " + ent.getType());
                    ent.remove();
                }
            }

            for (int n = 1; n <= 3; n++) {
                getCounterTerroristSpawn(true).getWorld().spawnEntity(getCounterTerroristSpawn(true), EntityType.CHICKEN);
            }
        });

        myBukkit.runTask(null, getTerroristSpawn(false), null, () -> {

            Location loc = this.getTerroristSpawn(false);

            for (Entity ent : loc.getWorld().getEntities()) {
                if (!(ent instanceof Player) && !ent.isDead()) {
                    //     Utils.debug(ent.getName() + " bye1");
                    ent.remove();
                }
                if (!(ent instanceof Player) && ent.getType().equals(Material.TNT)) {
                    Utils.debug(ent.getName() + " removes TNT? " + ent.getType());
                    ent.remove();
                }
            }

            for (int n = 1; n <= 3; n++) {
                getTerroristSpawn(true).getWorld().spawnEntity(getTerroristSpawn(true), EntityType.CHICKEN);
            }

        });

    }


    public void loadConfigs() {
        this.getConfig().options().copyDefaults(true);

        //alrealy loaded map
        Utils.debug("Loading static map and locations...");
        Lobby = getConfig().getString("lobby-location");
        SpawnTerrorists = getConfig().getString("spawn-locations.terrorist");
        SpawnCounterTerrorists = getConfig().getString("spawn-locations.counterterrorist");

        SpawnCounterTerroristsLocation = null;
        SpawnTerroristsLocation = null;

        randomMaps = getConfig().getBoolean("randomMaps", false);
        alwaysDay = getConfig().getBoolean("alwaysDay", true);
        quitExitGame = getConfig().getBoolean("quitExitGame", false);
    }


    //more customizable configs
    private void setupConfig() {
        FileConfiguration config = getConfig();
        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) dataFolder.mkdir();

        config.addDefault("randomMaps", false);
        config.addDefault("alwaysDay", true);
        config.addDefault("quitExitGame", false);

        config.options().copyDefaults(true);
        saveConfig();
    }


    public ItemStack getKnife() {
        ItemStack knife = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = knife.getItemMeta();

        myBukkit.setMeta(meta,ChatColor.GRAY + "Standard Knife");

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

        Preparemap();

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

            gameState = GameState.LOBBY;
            PacketUtils.sendTitleAndSubtitleToInGame(winnerText, ChatColor.AQUA + "They also won the whole game! (Left click to join new game)", 0, 10, 1);
            PacketUtils.sendActionBarToInGame(winnerText);

            FinishGame(winnerTeam, loserTeam);
            return; //stops game

        } else if (winnerTeam.getWins() + winnerTeam.getLosses() == MAX_ROUNDS) {

            gameState = GameState.LOBBY;
            PacketUtils.sendTitleAndSubtitleToInGame(winnerText, ChatColor.AQUA + "But scores are even! (Left click to join new game)", 0, 10, 1);
            PacketUtils.sendActionBarToInGame(winnerText);

            FinishGame(winnerTeam, loserTeam);
            return; //stops game

        } else {
            PacketUtils.sendTitleAndSubtitleToInGame(winnerText, ChatColor.YELLOW + "The next round will start shortly.", 0, 6, 1);
        }

        gameState = GameState.LOBBY;

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

        for (Player player : Bukkit.getOnlinePlayers()) {   //who ever is in a CS world is teleported

            String world = player.getWorld().getName();

            if (CounterStrike.i.HashWorlds != null) {
                Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);

                if (md != null && !md.modoCs) {
                    continue;
                }
            }

            myBukkit.playerTeleport(player,getLobbyLocation());
        }

        //use delay
        StartGameCounter(Delay);

        Utils.debug("Game successfully restarted...");
    }


    public void FinishGame(Team winnerTeam, Team loserTeam) {

        gameState = GameState.LOBBY;

        winnerTeam.setLosses(0);
        winnerTeam.setWins(0);
        winnerTeam.setColour("WHITE");
        loserTeam.setLosses(0);
        loserTeam.setWins(0);
        loserTeam.setColour("WHITE");

        for (CSPlayer csPlayer : csPlayers) {
            Player player = csPlayer.getPlayer();

            CSPlayer csplayer = getCSPlayer(player, false, null);

            if (csplayer != null) {
                getPlayerUpdater().deleteScoreBoards(player);
            }

            if (player.isOnline()) {
                player.getInventory().clear();

                //Clears colors
                myBukkit.setPlayerListName(player,ChatColor.WHITE + player.getName());

                String world = player.getWorld().getName();

                if (CounterStrike.i.HashWorlds != null) {
                    Worlds md = (Worlds) CounterStrike.i.HashWorlds.get(world);
                    if (md != null && !md.modoCs) continue;
                }

                 myBukkit.playerTeleport(player,getLobbyLocation());
                player.setGameMode(GameMode.SURVIVAL);
            }
        }

        csPlayers.clear();
        getPlayerUpdater().playersWithScoreboard = new ArrayList<>(); //list reset

    }


    public void setupPlayers() {

        for (CSPlayer csPlayer : terrorists) {
            Player player = csPlayer.getPlayer();

            setupTeams(player, "team1");

            csPlayer.setColourOpponent(counterTerroristsTeam.getColour());

            //put in base
             myBukkit.playerTeleport(player,getTerroristSpawn(true));

            if (player.getInventory().getItem(1) == null || csPlayer.getPistol() == null) {
                player.getInventory().setItem(1, Weapon.getByName("t-pistol-default").getItem());
            }
            giveEquipment(csPlayer);
            csPlayer.settempMVP(0);
        }

        for (CSPlayer csPlayer : counterTerrorists) {
            Player player = csPlayer.getPlayer();

            setupTeams(player, "team2");

            csPlayer.setColourOpponent(terroristsTeam.getColour());

            //put in base
           myBukkit.playerTeleport(player,getCounterTerroristSpawn(true));

            if (player.getInventory().getItem(1) == null || csPlayer.getPistol() == null) {
                player.getInventory().setItem(1, Weapon.getByName("ct-pistol-default").getItem());
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

            player.getInventory().setItem(2, getKnife());
          //  player.getInventory().setItem(8, getShopItem()); //check PlayerUpdater

            player.setHealth(40);

            CSPlayer cp = this.getCSPlayer(player, false, null);
            Weapon rifle = cp.getRifle();
            Weapon pistol = cp.getPistol();

            if (rifle != null) {
                Gun gun = me.zombie_striker.qg.api.QualityArmory.getGunByName(rifle.getName());
                Gun.updateAmmo(gun, player.getInventory().getItem(0), rifle.getMagazineCapacity());

                Utils.debug(rifle.getMagazineCapacity() + " ###### rifle " + rifle.getName());

                ItemStack ammo = gun.getAmmoType().getItemStack().clone();
                ammo.setAmount((rifle.getMagazines() - 1) * rifle.getMagazineCapacity());
                player.getInventory().setItem(6, ammo);
            }

            if (pistol != null) {
                Gun gun = me.zombie_striker.qg.api.QualityArmory.getGunByName(pistol.getName());
                Gun.updateAmmo(gun, player.getInventory().getItem(1), pistol.getMagazineCapacity());

                ItemStack ammo = gun.getAmmoType().getItemStack().clone();
                ammo.setAmount((pistol.getMagazines() - 1) * pistol.getMagazineCapacity());
                player.getInventory().setItem(7, ammo);
            }

            player.getInventory().remove(Material.TNT);
        }

        if (!terrorists.isEmpty()) {
            CSPlayer playerWithBomb = (CSPlayer) terrorists.toArray()[new Random().nextInt(terrorists.toArray().length)];
            playerWithBomb.getPlayer().getInventory().setItem(4, CSUtil.getBombItem());
        }

    }


    public void setupTeams(Player player, String team) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        org.bukkit.scoreboard.Team myTeam = board.getTeam(team);

        if (!myBukkit.isFolia() && board != null) {
            if (myTeam == null) {
                board.registerNewTeam(team);
                myTeam = board.getTeam(team);
                myTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.FOR_OTHER_TEAMS); //the command is hideforotherteams in vanilla
            }

            myBukkit.teamAddEntity(myTeam,player);
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
            Utils.debug("Returning a new CSPlayer...");
            return new CSPlayer(this, player, colour);
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

        if (SpawnTerroristsLocation != null) return SpawnTerroristsLocation;
        //cache...

        Utils.debug("Getting Terrorist spawn...");
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

        if (SpawnCounterTerroristsLocation != null) return SpawnCounterTerroristsLocation;

        Utils.debug("Getting Counter Terrorist spawn...");
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
         myBukkit.playerSetResourcePack(player,resourse, hash);
    }


    public void returnPlayertoGame(CSPlayer csplay) {

        if (csplay == null) {
            return;
        }

        Player player = csplay.getPlayer();

        if (ResourseHash.get(player.getName() + "RES") == null || ResourseHash.get(player.getName() + "RES") == "DEFAULT") {
            ResourseHash.remove(player.getName() + "RES");
            ResourseHash.put(player.getName() + "RES", "QUALITY");

            loadResourcePack(player, "https://github.com/ZombieStriker/QualityArmory-Resourcepack/releases/download/latest/QualityArmory.zip", "3a34fc09dcc6f009aa05741f8ab487dd17b13eaf");
        }

        if (terrorists.contains(csplay)) {
            csplay.setColourOpponent(counterTerroristsTeam.getColour());
            //Coloca na base terr
             myBukkit.playerTeleport(player,getTerroristSpawn(true));

            if (CounterStrike.i.gameState == GameState.SHOP) {
                boolean noOneHasTNT = false;

                for (CSPlayer csPlayer : terrorists) {
                    if (csPlayer.getBomb() != null) {
                        noOneHasTNT = true;
                        break;
                    }
                }

                //Utils.debug(noOneHasTNT +"  vs  "+isTNTDropped());

                if (!noOneHasTNT && !isTNTDropped()) {
                    //Utils.debug("Backup TNT");
                    csplay.getPlayer().getInventory().setItem(4, CSUtil.getBombItem());
                }
            }
        }

        if (counterTerrorists.contains(csplay)) {
            csplay.setColourOpponent(terroristsTeam.getColour());
            //Coloca na base contra
             myBukkit.playerTeleport(player,getCounterTerroristSpawn(true));
        }

        getPlayerUpdater().setScoreBoard(csplay);

//        player.getInventory().clear();
//        myBukkit.runTaskLater(player, null, null, () -> player.setGameMode(GameMode.SPECTATOR), 40);
//        PacketUtils.sendTitleAndSubtitle(player, ChatColor.YELLOW + "Get ready", ChatColor.RED + "You will resume playing in next round!", 1, 8, 1);
    }


    public void StartGameCounter(int delay) {

        if (gameCounterTask != null && gameCount != null) return; //already running skip
        if (delay == 0) delay = 1; //default delay
        if (gameCount == null) gameCount = new GameCounter(this);

        gameCounterTask = CounterStrike.i.myBukkit.runTaskTimer(null, null, null, () -> gameCount.run(), delay * 20L, 20L);
    }


    public void StopGameCounter() {
        CounterStrike.i.myBukkit.cancelTask(gameCounterTask);
        gameCounterTask = null;
    }


    public void ChooseConfigs(boolean virtualOn) {

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

        Location loc = new Location(world, x, y, z, yaw, pitch);

        loc.add(0, 0, 6);
        Location loc1;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Utils.debug(virtualOn + "  <<<  #### Player " + player.getName() + " configs virtual");

            if (virtualOn) {

                for (int i = 0; i <= 8; i++) {
                    for (int j = 0; j <= 4; j++) {
                        loc1 = loc.clone();
                        player.sendBlockChange(loc1.add(i, j, 0), Material.WHITE_CONCRETE.createBlockData());
                    }
                }

//                player.sendBlockChange(loc, Material.OAK_SIGN.createBlockData());
                player.sendBlockChange(loc.add(0, 1, 1), Material.BLUE_CONCRETE.createBlockData());
                player.sendBlockChange(loc.add(+2, 0, 0), Material.RED_CONCRETE.createBlockData());
                player.sendBlockChange(loc.add(+2, 0, 0), Material.YELLOW_CONCRETE.createBlockData());
                player.sendBlockChange(loc.add(+2, 0, 0), Material.GREEN_CONCRETE.createBlockData());
//
//                player.sendBlockChange(loc, Material.OAK_SIGN.createBlockData());
            } else {
                player.sendBlockChange(loc, loc.getBlock().getBlockData());


            }
        }

    }
}
