package dev.danablend.counterstrike;

import dev.danablend.counterstrike.database.Mundos;

import dev.danablend.counterstrike.commands.CounterStrikeCommand;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import dev.danablend.counterstrike.csplayer.Team;
import dev.danablend.counterstrike.csplayer.TeamEnum;
import dev.danablend.counterstrike.database.SQLiteConnection;
import dev.danablend.counterstrike.enums.Weapon;
import dev.danablend.counterstrike.listeners.*;
import dev.danablend.counterstrike.runnables.*;
import dev.danablend.counterstrike.shop.Shop;
import dev.danablend.counterstrike.shop.ShopListener;
import dev.danablend.counterstrike.tests.TestCommand;
import dev.danablend.counterstrike.utils.CSUtil;
import dev.danablend.counterstrike.utils.PacketUtils;
import dev.danablend.counterstrike.utils.Utils;

import me.zombie_striker.qg.guns.Gun;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static dev.danablend.counterstrike.Config.MAX_ROUNDS;
import static dev.danablend.counterstrike.Config.ROUNDS_TO_WIN;


public class CounterStrike extends JavaPlugin {

    public GameState gameState = GameState.LOBBY;

    Collection<CSPlayer> csPlayers = new ArrayList<>();
    Collection<CSPlayer> counterTerrorists = new ArrayList<CSPlayer>();
    Collection<CSPlayer> terrorists = new ArrayList<CSPlayer>();
    Collection<CSPlayer> tempPlayerList = new ArrayList<CSPlayer>();
    Set<TestCommand> testCommands = new HashSet<TestCommand>();

    PluginManager pm = Bukkit.getPluginManager();

    ItemStack shopItem;
    private GameTimer timer;
    public ShopPhaseManager Shop;
    public GameCounter gameCount;

    private Team counterTerroristsTeam;
    private Team terroristsTeam;
    public static CounterStrike i;

    private SQLiteConnection sqlite = null;
    public Hashtable HashWorlds = null;
    public Hashtable ResourseHash = new Hashtable();
    private dev.danablend.counterstrike.runnables.PlayerUpdater pUpdate;
    public String Map = "";
    private String SpawnTerrorists;
    private String SpawnCounterTerrorists;
    private String Lobby;


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

        Utils.debug("Setting up...");

        if (!CounterStrike.i.usingQualityArmory()) {
            System.out.println("#####  QualityArmory not loaded... Disabling CSMC");
            return;
        }

        LoadCOnfigs();

        me.Tontito.GeneralP.Main generalP;
        generalP = ((me.Tontito.GeneralP.Main) getServer().getPluginManager().getPlugin("GeneralP"));

        if (generalP != null && generalP.isEnabled()) {
            sqlite = new SQLiteConnection(generalP.getDataFolder(), "GeneralP.db");

            Hashtable tmpHashWorlds = generalP.getTrackingMethods().GetMundos();
            Hashtable HashWorlds = new Hashtable();

            Enumeration enu = tmpHashWorlds.elements();

            while (enu.hasMoreElements()) {
                me.Tontito.GeneralP.Mundos md = (me.Tontito.GeneralP.Mundos)HashWorlds.get(enu.nextElement());
                Mundos newMD = new Mundos(md.id,md.nome,md.modoCs);

                HashWorlds.put(enu.nextElement(),newMD);
            }
            System.out.println("Loaded mundos from generalp! ");

        } else {
            sqlite = new SQLiteConnection(this.getDataFolder(), "CSMC.db");

            try (Connection conn = sqlite.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("select id, nome, ifnull(modoCs,false) modoCs from mundos;")) {
                HashWorlds = new Hashtable();

                while (rs.next()) {
                    String mundo = rs.getString("nome");
                    Mundos md = new dev.danablend.counterstrike.database.Mundos(rs.getInt("id"), mundo, Boolean.parseBoolean(rs.getString("modoCs")));
                    HashWorlds.put(mundo, md);
                }
                System.out.println("Loaded mundos! ");
            } catch (SQLException e) {
                System.out.println("Exception loading mundos " + e.getMessage());
            }
        }

        this.terroristsTeam = new Team(TeamEnum.TERRORISTS, terrorists);
        this.counterTerroristsTeam = new Team(TeamEnum.COUNTER_TERRORISTS, counterTerrorists);
        saveDefaultConfig();
        gameCount = new GameCounter(this);
        gameCount.runTaskTimer(this, 40L, 200L);
        pUpdate = new PlayerUpdater(this);
        pUpdate.runTaskTimer(this, 0L, 20L);
        pm.registerEvents(new BlockPlaceListener(), this);
        pm.registerEvents(new BlockBreakListener(), this);
        pm.registerEvents(new FoodLevelChangeListener(), this);
        pm.registerEvents(new PlayerInteractListener(), this);
        pm.registerEvents(new PlayerItemDamageListener(), this);
        pm.registerEvents(new PlayerDropItemListener(), this);
        pm.registerEvents(new PlayerJoinListener(), this);
        pm.registerEvents(new PlayerDeathListener(), this);
        pm.registerEvents(new CustomPlayerDeathListener(), this);
        pm.registerEvents(new PlayerRespawnListener(), this);
        pm.registerEvents(new EntityDamageByEntityListener(), this);
        pm.registerEvents(new WeaponFireListener(), this);
        pm.registerEvents(new BulletHitListener(), this);
        pm.registerEvents(new ShopListener(this), this);
        pm.registerEvents(new BlockIgniteListener(), this);
        pm.registerEvents(new EntityPickupItemListener(), this);
        pm.registerEvents(new InventoryClickListener(), this);

        getCommand("csmc").setExecutor(new CounterStrikeCommand(this));

        for (World w : Bukkit.getWorlds()) {

            if (HashWorlds != null) {
                Object obj = HashWorlds.get(w.getName());

                //if hasn't generalP loaded
                if (obj != null) {
                    Mundos md = (Mundos) obj;

                    if (!md.modoCs) { //se explicitamente nao tem modcs salta
                        w.setGameRule(GameRule.NATURAL_REGENERATION, true);
                        continue;
                    }
                }
            }

            w.setGameRule(GameRule.NATURAL_REGENERATION, false);
            w.setGameRule(GameRule.KEEP_INVENTORY, false);
        }

        Utils.debug("Creating shop item...");
        shopItem = new ItemStack(Material.CHEST);
        ItemMeta meta = shopItem.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "(Right click to open shop)");
        shopItem.setItemMeta(meta);

        Config c = new Config();
        c.loadWeapons();

        new Shop();

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

    public void LoadDBRandomConfigs() {

        if (sqlite != null) {

            String result = sqlite.select("select max(id) from CSMaps");

            Long Rand;

            while (true) {
                Rand = Math.round(1 + Math.random() * Integer.parseInt(result));

                String result1 = sqlite.select("select descr from CSMaps where id = " + Rand);

                if (result1 != null && !result1.equals("")) {
                    Map = result1;
                    broadcastMessage(ChatColor.RED + "Map " + result1 + " was randomly chosen");
                    break;
                }

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }

            Lobby = sqlite.select("select SpawnLobby from CSMaps where id = " + Rand);
            SpawnTerrorists = sqlite.select("select SpawnTerrorists from CSMaps where id = " + Rand);
            SpawnCounterTerrorists = sqlite.select("select SpawnCounter from CSMaps where id = " + Rand);

            return;
        }

        Lobby = getConfig().getString("lobby-location");
        SpawnTerrorists = getConfig().getString("spawn-locations.terrorist");
        SpawnCounterTerrorists = getConfig().getString("spawn-locations.counterterrorist");
    }

    public void LoadCOnfigs() {
        Lobby = getConfig().getString("lobby-location");
        SpawnTerrorists = getConfig().getString("spawn-locations.terrorist");
        SpawnCounterTerrorists = getConfig().getString("spawn-locations.counterterrorist");
    }

    public ItemStack getKnife() {
        Utils.debug("Getting knife...");
        ItemStack knife = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = knife.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Standard Knife");
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
        Utils.debug("Starting game initiated...");
        System.out.println(" starting game ");

        gameState = GameState.STARTING;

        try {
            this.getTerroristSpawn(false).getWorld().getEntities().stream().filter(Item.class::isInstance).forEach(Entity::remove); // remove tnt from ground
        } catch (Exception e) {
        }

        for (CSPlayer csPlayer : terrorists) {
            Player p = csPlayer.getPlayer();

            csPlayer.setColourOpponent(counterTerroristsTeam.getColour());

            //Coloca na base
            p.teleport(getTerroristSpawn(true));
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
            p.teleport(getCounterTerroristSpawn(true));
            if (p.getInventory().getItem(1) == null) {
                p.getInventory().setItem(1, Weapon.getByName("ct-pistol-default").getItem());
            }
            giveEquipment(csPlayer);
            csPlayer.settempMVP(0);
        }

        for (CSPlayer csplayer : getCSPlayers()) {
            Player player = csplayer.getPlayer();

            //#### trying to hide name???
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
                // gun.reload(player);
                Gun.updateAmmo(gun, player.getInventory().getItem(0).getItemMeta(), rifle.getMagazineCapacity());

                ItemStack ammo = gun.getAmmoType().getItemStack().clone();
                ammo.setAmount((rifle.getMagazines() - 1) * rifle.getMagazineCapacity());
                player.getInventory().setItem(6, ammo);
            }

            if (pistol != null) {
                me.zombie_striker.qg.guns.Gun gun = me.zombie_striker.qg.api.QualityArmory.getGunByName(pistol.getName());
                //gun.reload(player);
                Gun.updateAmmo(gun, player.getInventory().getItem(1).getItemMeta(), pistol.getMagazineCapacity());

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
            System.out.println(" ###### avoided duplication thread to Shop ");
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

        if (winnerTeam.getWins() == ROUNDS_TO_WIN) {

            gameState = GameState.LOBBY;

            PacketUtils.sendTitleAndSubtitleToInGame(winnerText, ChatColor.YELLOW + "They also won the whole game! (Left click to join new game)", 0, 10, 1);
            PacketUtils.sendActionBarToInGame(winnerText, 5);

            Delay = 15;

            winnerTeam.setLosses(0);
            winnerTeam.setWins(0);
            winnerTeam.setColour("WHITE");
            loserTeam.setLosses(0);
            loserTeam.setWins(0);
            loserTeam.setColour("WHITE");

            for (Player player : Bukkit.getOnlinePlayers()) {

                //Clears coulors
                player.setPlayerListName(ChatColor.WHITE + player.getName());

                CSPlayer csplayer = getCSPlayer(player, false, null);

                if (csplayer != null) {
                    player.getInventory().clear();
                    csplayer.clear();
                }

                String mundo = player.getWorld().getName();

                if (CounterStrike.i.HashWorlds != null) {
                    Mundos md = (Mundos) CounterStrike.i.HashWorlds.get(mundo);

                    if (md != null && !md.modoCs) {
                        continue;
                    }
                }

                player.teleport(getLobbyLocation());
                player.setGameMode(GameMode.SURVIVAL);
            }

            if (gameCount == null) {
                gameCount = new GameCounter(this);
                gameCount.runTaskTimer(this, Delay * 20L, 200L);
            }
            return; //stops game

        } else {
            PacketUtils.sendTitleAndSubtitleToInGame(winnerText, ChatColor.YELLOW + "The next round will start shortly.", 0, 6, 1);
        }

        gameState = GameState.LOBBY;

        PacketUtils.sendActionBarToInGame(winnerText, 5);

        if (winnerTeam.getWins() + winnerTeam.getLosses() == (Math.round(MAX_ROUNDS / 2))) {

            Delay = 5;

            System.out.println("################################# swap teams ##################################");

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
                    PacketUtils.sendTitleAndSubtitle(csplayer.getPlayer(), ChatColor.YELLOW + "You are NOW a " + ChatColor.BLUE + "Counter Terrorist", ChatColor.BLUE + "Defend the sites from terrorists, defuse the bomb.", 1, 5, 1);
                } else {
                    PacketUtils.sendTitleAndSubtitle(csplayer.getPlayer(), ChatColor.YELLOW + "You are NOW a " + ChatColor.RED + "Terrorist", ChatColor.RED + "Plant the bomb on the sites, have it explode.", 1, 5, 1);
                }
            }

            for (CSPlayer csplayer : winnerTeam.getCsPlayers()) {
                csplayer.setMoney(Config.STARTING_MONEY);

                if (csplayer.getTeam().equals(TeamEnum.COUNTER_TERRORISTS)) {
                    PacketUtils.sendTitleAndSubtitle(csplayer.getPlayer(), ChatColor.YELLOW + "You are NOW a " + ChatColor.BLUE + "Counter Terrorist", ChatColor.BLUE + "Defend the sites from terrorists, defuse the bomb.", 1, 5, 1);
                } else {
                    PacketUtils.sendTitleAndSubtitle(csplayer.getPlayer(), ChatColor.YELLOW + "You are NOW a " + ChatColor.RED + "Terrorist", ChatColor.RED + "Plant the bomb on the sites, have it explode.", 1, 5, 1);
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

            player.teleport(getLobbyLocation());
        }

        if (gameCount == null) {
            gameCount = new GameCounter(this);
            gameCount.runTaskTimer(this, Delay * 40L, 200L);
        }

        Utils.debug("Game successfully restarted...");
    }

    public GameTimer getGameTimer() {
        Utils.debug("Getting game timer...");
        return timer;
    }

    public void setGameTimer(GameTimer gametimer) {
        Utils.debug("Setting game timer...");
        this.timer = gametimer;
    }

    public CSPlayer getCSPlayer(Player player, boolean cria, String colour) {
        Utils.debug("Getting CSPlayer...");
        for (CSPlayer csPlayer : csPlayers) {
            if (csPlayer.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                Utils.debug("Returning existing CSPlayer...");
                return csPlayer;
            }
        }

        if (cria) {
            Utils.debug("Returning a new CSPlayer...");
            return new CSPlayer(this, player, colour);
        } else {
            return null;
        }
    }

    public Collection<CSPlayer> getCSPlayers() {
        Utils.debug("Getting CSPlayers...");
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
        Utils.debug("Getting CT Team...");
        return counterTerroristsTeam;
    }

    public Team getTerroristsTeam() {
        Utils.debug("Getting T Team...");
        return terroristsTeam;
    }

    public GameState getGameState() {
        Utils.debug("Getting GameState...");
        return gameState;
    }

    public Location getLobbyLocation() {
        Utils.debug("Getting Lobby Location...");
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

        return new Location(world, x, y, z, yaw, pitch);
    }

    public Location getCounterTerroristSpawn(boolean rand) {
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

        return new Location(world, x, y, z, yaw, pitch);
    }

    public void giveEquipment(CSPlayer csPlayer) {

        if (true) {
            return;
        }

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
}
