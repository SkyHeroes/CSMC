package dev.danablend.counterstrike;

import dev.danablend.counterstrike.commands.CounterStrikeCommand;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.Team;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.database.Mundos;
import dev.danablend.counterstrike.database.SQLiteConnection;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.listeners.*;
import dev.danablend.counterstrike.runnables.*;
import dev.danablend.counterstrike.shop.Shop;
import dev.danablend.counterstrike.shop.ShopListener;
import dev.danablend.counterstrike.tests.TestCommand;
import dev.danablend.counterstrike.utils.CSUtil;
import dev.danablend.counterstrike.utils.MyBukkit;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.zombie_striker.qg.guns.Gun;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

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
    }

    public void onDisable() {
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

        if (Bukkit.getVersion().toUpperCase().contains("FOLIA")) {
            myBukkit = new MyBukkit(this, 8);
        } else {
            myBukkit = new MyBukkit(this, 1);
        }

        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        sqlite = new SQLiteConnection(getDataFolder(), "CSMC.db");

        try (Connection conn = sqlite.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select id, nome, ifnull(modoCs,false) modoCs from mundos;")) {
            HashWorlds = new Hashtable();

            while (rs.next()) {
                String mundo = rs.getString("nome");
                Mundos md = new dev.danablend.counterstrike.database.Mundos(rs.getInt("id"), mundo, Boolean.parseBoolean(rs.getString("modoCs")));
                HashWorlds.put(mundo, md);
                Utils.debug("Loaded map " + mundo + "  " + md.modoCs);
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

        for (World w : Bukkit.getWorlds()) {

            if (HashWorlds != null) {
                Object obj = HashWorlds.get(w.getName());

                if (obj != null) {
                    Mundos md = (Mundos) obj;

                    if (!md.modoCs) {
                        this.myBukkit.runTask(null,null,null, () -> w.setGameRule(GameRule.NATURAL_REGENERATION, true));
                        continue;
                    }
                }
            }

            this.myBukkit.runTask(null,null,null, () -> {
                w.setGameRule(GameRule.NATURAL_REGENERATION, false);
                w.setGameRule(GameRule.KEEP_INVENTORY, false);
            });
        }

        Utils.debug("Creating shop item...");
        shopItem = new ItemStack(Material.CHEST);
        ItemMeta meta = shopItem.getItemMeta();
        Component component = Component.text(ChatColor.YELLOW + "(Right click to open shop)");
        meta.displayName(component);
        shopItem.setItemMeta(meta);

        Config c = new Config();
        c.loadWeapons();

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

                Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(location.split(",")[0]);

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

            if (result.equals("")) {
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

            LoadDBMapConfigs(result1);
            return;
        }

        Lobby = getConfig().getString("lobby-location");
        SpawnTerrorists = getConfig().getString("spawn-locations.terrorist");
        SpawnCounterTerrorists = getConfig().getString("spawn-locations.counterterrorist");

        SpawnCounterTerroristsLocation = null;
        SpawnTerroristsLocation = null;

        myBukkit.runTaskLater(null, getTerroristSpawn(false), null, () -> Preparemap(), 1);
    }


    public void LoadDBMapConfigs(String Map) {

        Integer Rand = Integer.parseInt(sqlite.select("select id from CSMaps where descr = '" + Map + "'"));

        Lobby = sqlite.select("select SpawnLobby from CSMaps where id = " + Rand);
        SpawnTerrorists = sqlite.select("select SpawnTerrorists from CSMaps where id = " + Rand);
        SpawnCounterTerrorists = sqlite.select("select SpawnCounter from CSMaps where id = " + Rand);

        SpawnCounterTerroristsLocation = null;
        SpawnTerroristsLocation = null;

        myBukkit.runTaskLater(null, getTerroristSpawn(false), null, () -> Preparemap(), 1);

    }


    private void Preparemap() {

        myBukkit.runTask(null,getTerroristSpawn(false),null, () -> {
            this.getTerroristSpawn(false).getWorld().getEntities().stream().filter(Item.class::isInstance).forEach(Entity::remove); // remove tnt from ground
        });

//        try {
//            Location local = getTerroristSpawn(false);
//
//            //   this.getTerroristSpawn(false).getWorld().getEntities().stream().filter(Item.class::isInstance).forEach(Entity::remove); // remove tnt from ground
//            myBukkit.runTaskLater(null, local, null, () -> {
//                // local.getWorld().getEntities().stream().filter(Item.class::isInstance).forEach(Entity::remove);
//                // entity.getType()  == Material.TNT
//
//                for (Entity entity : local.getWorld().getEntities()) {
//                    if (entity instanceof Item) {
//                        myBukkit.runTaskLater(null, null, entity, () -> {
//                            entity.remove();
//                        }, 1);
//                    }
//                }
//            }, 1);
//        } catch (Exception e) {
//        }

        //  if (!(Map.equals("Dust2") || Map.equals("Mirage"))) return

        for (int n = 1; n == 3; n++) {
            getTerroristSpawn(false).getWorld().spawnEntity(getTerroristSpawn(true), EntityType.CHICKEN);
        }

        for (int n = 1; n == 3; n++) {
            // getCounterTerroristSpawn(false).getWorld().spawn spawnCreature(TargetLocation, EntityType.ZOMBIE);
            getCounterTerroristSpawn(false).getWorld().spawnEntity(getCounterTerroristSpawn(true), EntityType.CHICKEN);
        }

    }


    public void SendOptions(Player player) {

        String result = sqlite.select("select max(id) from CSMaps");
        String result1;
        // JSONMessage j = null;

        for (int n = 1; n <= Integer.parseInt(result); n++) {

            result1 = sqlite.select("select descr from CSMaps where id = " + n);

            if (result1 != null && !result1.equals("")) {

//                if (n == 1) {
//                    j = JSONMessage.create(result1)
//                            .color(ChatColor.GREEN)
//                            .tooltip("Click to select map")
//                            .runCommand("/csmc LoadMap " + result1);
//                } else {
//                    j.then(" / ")
//                            .color(ChatColor.GRAY)
//                            .style(ChatColor.BOLD)
//                            .then(result1)
//                            .color(ChatColor.GREEN)
//                            .tooltip("Click to select map")
//                            .runCommand("/csmc LoadMap " + result1);
//                }

            }

        }

        //      j.send(player);

    }


    public void LoadCOnfigs() {
        //alrealy loaded map
        Utils.debug("Loading static map and locations...");
        Lobby = getConfig().getString("lobby-location");
        SpawnTerrorists = getConfig().getString("spawn-locations.terrorist");
        SpawnCounterTerrorists = getConfig().getString("spawn-locations.counterterrorist");

        SpawnCounterTerroristsLocation = null;
        SpawnTerroristsLocation = null;
    }


    public ItemStack getKnife() {
        //     Utils.debug("Getting knife...");
        ItemStack knife = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = knife.getItemMeta();
        Component component = Component.text(ChatColor.GRAY + "Standard Knife");
        meta.displayName(component);
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

        gameState = GameState.STARTING;

        for (CSPlayer csPlayer : terrorists) {
            Player p = csPlayer.getPlayer();

            csPlayer.setColourOpponent(counterTerroristsTeam.getColour());

            //Coloca na base
            myBukkit.runTask(p, null, null, () -> p.teleportAsync(getTerroristSpawn(true)));

            if (p.getInventory().getItem(1) == null) {
                p.getInventory().setItem(1, Weapon.getByName("t-pistol-default").getItem());
            }
            giveEquipment(csPlayer);
            csPlayer.settempMVP(0);
        }

        for (CSPlayer csPlayer : counterTerrorists) {
            Player p = csPlayer.getPlayer();

            csPlayer.setColourOpponent(terroristsTeam.getColour());

            //Coloca na base
            myBukkit.runTask(p, null, null, () -> p.teleportAsync(getCounterTerroristSpawn(true)));

            if (p.getInventory().getItem(1) == null) {
                p.getInventory().setItem(1, Weapon.getByName("ct-pistol-default").getItem());
            }
            giveEquipment(csPlayer);
            csPlayer.settempMVP(0);
        }


        for (CSPlayer csplayer : getCSPlayers()) {
            Player player = csplayer.getPlayer();

            //#### trying to hide name???
//            ArmorStand stand = player.getLocation().getWorld().spawn(player.getLocation().add(0.0D, -1.25D, 0.0D), ArmorStand.class);
//            SkullMeta meta = (SkullMeta) head.getItemMeta();
//            meta = VersionUtils.setPlayerHead(p, meta);
//            head.setItemMeta(meta);
//
//            stand.setVisible(false);
//            stand.getEquipment().setHelmet(head);
//            stand.setGravity(false);
//            stand.setCustomNameVisible(false);


            player.setCustomNameVisible(false);


            player.setGameMode(GameMode.SURVIVAL);
            player.setFlying(false);
            player.setAllowFlight(false);
            player.getInventory().setItem(2, getKnife());
            player.getInventory().setItem(8, getShopItem());

            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

            CSPlayer cp = this.getCSPlayer(player, false, null);
            Weapon rifle = cp.getRifle();
            Weapon pistol = cp.getPistol();

            if (rifle != null) {
                me.zombie_striker.qg.guns.Gun gun = me.zombie_striker.qg.api.QualityArmory.getGunByName(rifle.getName());
                Gun.updateAmmo(gun, player.getInventory().getItem(0), rifle.getMagazineCapacity());

                ItemStack ammo = gun.getAmmoType().getItemStack().clone();
                ammo.setAmount((rifle.getMagazines() - 1) * rifle.getMagazineCapacity());
                player.getInventory().setItem(6, ammo);
            }

            if (pistol != null) {
                me.zombie_striker.qg.guns.Gun gun = me.zombie_striker.qg.api.QualityArmory.getGunByName(pistol.getName());
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


        for (Player player : Bukkit.getOnlinePlayers()) {   //quem estiver no mundo de cs é teleportado

            String mundo = player.getWorld().getName();

            if (CounterStrike.i.HashWorlds != null) {
                Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

                if (md != null && !md.modoCs) {
                    continue;
                }
            }

            myBukkit.runTask(player, null, null, () -> player.teleportAsync(getLobbyLocation()));
        }

        //usar delay
        StartGameCounter(Delay);

        Utils.debug("Game successfully restarted...");
    }


    private void FinishGame(Team winnerTeam, Team loserTeam) {

        gameState = GameState.LOBBY;

        winnerTeam.setLosses(0);
        winnerTeam.setWins(0);
        winnerTeam.setColour("WHITE");
        loserTeam.setLosses(0);
        loserTeam.setWins(0);
        loserTeam.setColour("WHITE");

        for (Player player : Bukkit.getOnlinePlayers()) {

            //Clears coulors
            TextComponent component = Component.text(ChatColor.WHITE + player.getName());
            player.playerListName(component);

            CSPlayer csplayer = getCSPlayer(player, false, null);

            if (csplayer != null) {
                player.getInventory().clear();
                getPlayerUpdater().deleteScoreBoards(player);
                csplayer.clear();
            }

            String mundo = player.getWorld().getName();

            if (CounterStrike.i.HashWorlds != null) {
                Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

                if (md != null && !md.modoCs) continue;
            }

            myBukkit.runTask(player, null, null, () -> player.teleportAsync(getLobbyLocation()));
            player.setGameMode(GameMode.SURVIVAL);
        }

        getPlayerUpdater().playersWithScoreboard = new ArrayList<>(); //list reset

        //   StartGameCounter(0);
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

    public Location getLobbyLocation() {
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
        int delay = 2;
        Utils.debug("Muda pack para " + resourse);

        this.myBukkit.runTaskLater(player,null,null, () -> player.setResourcePack(resourse, hash), delay * 20);
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
            myBukkit.runTask(player, null, null, () -> player.teleportAsync(getTerroristSpawn(true)));
        }

        if (counterTerrorists.contains(csplay)) {
            csplay.setColourOpponent(terroristsTeam.getColour());
            //Coloca na base contra
            myBukkit.runTask(player, null, null, () -> player.teleportAsync(getCounterTerroristSpawn(true)));
        }

        getPlayerUpdater().setScoreBoard(csplay);

        player.getInventory().clear();
        myBukkit.runTaskLater(player, null, null, () -> player.setGameMode(GameMode.SPECTATOR), 40);

    }


    public void StartGameCounter(int delay) {

        if (gameCounterTask != null && gameCount != null) return; //already running skip
        if (delay == 0) delay = 1; //default delay
        if (gameCount == null) gameCount = new GameCounter(this);

        gameCounterTask = CounterStrike.i.myBukkit.runTaskTimer(null, null, null, () -> gameCount.run(), delay * 20L, 20L);
    }

    public void StopGameCounter() {
        if (CounterStrike.i.myBukkit.isFolia())
            ((ScheduledTask) gameCounterTask).cancel();
        else
            ((BukkitTask) gameCounterTask).cancel();

        gameCounterTask = null;
    }
}
