package dev.danablend.counterstrike.utils;

import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.utils.compatibility.MyBukkitPaper;
import dev.danablend.counterstrike.utils.compatibility.MyBukkitPaperNotLegacy;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.bukkit.Bukkit.getServer;
import static org.bukkit.Bukkit.isOwnedByCurrentRegion;

public class MyBukkit {
    private CounterStrike main;
    private boolean isFoliaBased;
    private boolean isPaperBased;
    private boolean isLegacy;
    private boolean isInformed = false;
    private boolean starting = true;

    private MyBukkitPaper myBukkitPaper;
    private MyBukkitPaperNotLegacy myBukkitPaperNotLegacy;

    public MyBukkit(CounterStrike main) {
        this.main = main;

        Class classCheck = null;

        try {
            classCheck = Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
        } catch (Exception e) {
            classCheck = null;
        }

        isFoliaBased = (classCheck != null);

        try {
            classCheck = Class.forName("io.papermc.paper.plugin.provider.classloader.PaperClassLoaderStorageAccess");
        } catch (Exception e) {
            classCheck = null;
        }

        isPaperBased = (classCheck != null);

        if (isPaperBased) {
            isLegacy = (Bukkit.getMinecraftVersion().equals("1.20.1") || Bukkit.getMinecraftVersion().equals("1.20.2"));
        } else {
            isLegacy = (Bukkit.getBukkitVersion().startsWith("1.20.1") || Bukkit.getBukkitVersion().startsWith("1.20.2"));
        }

        if (isPaperBased) myBukkitPaper = new MyBukkitPaper();

        if (!isLegacy) myBukkitPaperNotLegacy = new MyBukkitPaperNotLegacy();

        Utils.debug("Enabled    Paper:"+isPaperBased+"   Folia:"+isFoliaBased+ "   Legacy:"+ isLegacy + "   " +Bukkit.getBukkitVersion());
    }


    public boolean isFolia() {
        return isFoliaBased;
    }


    public boolean isPaper() {
        return isPaperBased;
    }


    public Object runTask(Player player, Location local, Entity entity, Runnable myrun) {
        if (isFoliaBased) {
            if (player != null) return player.getScheduler().run(main, st -> myrun.run(), null);
            else if (local != null) return getServer().getRegionScheduler().run(main, local, st -> myrun.run());
            else if (entity != null) return entity.getScheduler().run(main, st -> myrun.run(), null);
            else return getServer().getGlobalRegionScheduler().run(main, st -> myrun.run());
        } else {
            return org.bukkit.Bukkit.getScheduler().runTask(main, myrun);
        }
    }


    public Object runTaskLater(Player player, Location local, Entity entity, Runnable myrun, long delay) {
        if (isFoliaBased) {
            if (player != null) return player.getScheduler().runDelayed(main, st -> myrun.run(), null, delay);
            else if (local != null)
                return getServer().getRegionScheduler().runDelayed(main, local, st -> myrun.run(), delay);
            else if (entity != null) return entity.getScheduler().runDelayed(main, st -> myrun.run(), null, delay);
            else return getServer().getGlobalRegionScheduler().runDelayed(main, st -> myrun.run(), delay);
        } else {
            return getServer().getScheduler().runTaskLater(main, myrun, delay);
        }
    }


    public Object runTaskTimer(Player player, Location local, Entity entity, Runnable myrun, long delay, long period) {
        if (isFoliaBased) {
            if (player != null)
                return player.getScheduler().runAtFixedRate(main, st -> myrun.run(), null, delay, period);
            else if (local != null)
                return getServer().getRegionScheduler().runAtFixedRate(main, local, st -> myrun.run(), delay, period);
            else if (entity != null)
                return entity.getScheduler().runAtFixedRate(main, st -> myrun.run(), null, delay, period);
            else return getServer().getGlobalRegionScheduler().runAtFixedRate(main, st -> myrun.run(), delay, period);
        } else {
            return getServer().getScheduler().runTaskTimer(main, myrun, delay, period);
        }
    }


    public void cancelTask(Object task) {

        if (task == null) return;

        if (isFoliaBased)
            ((ScheduledTask) task).cancel();
        else
            ((BukkitTask) task).cancel();
    }


    public boolean isCancelled(Object task) {

        if (task == null) return true;

        if (isFoliaBased)
            return ((ScheduledTask) task).isCancelled();
        else
            return ((BukkitTask) task).isCancelled();
    }


    public boolean isOwnedby(Entity entity, Location local, Block block) {

        if (isFoliaBased) {
            if (entity != null) return isOwnedByCurrentRegion(entity);
            else if (local != null) return isOwnedByCurrentRegion(local);
            else if (block != null) return isOwnedByCurrentRegion(block);
        }
        return true;
    }


    public static ArmorStand startLabel(Location location) {

        Location loc = location.clone();
        loc.add(0, -0.75, 0);
        ArmorStand hologram = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        hologram.setVisible(false);
        hologram.setCustomNameVisible(true);
        hologram.setGravity(false);

        return hologram;
    }


    public void showLabel(ArmorStand hologram, String message, boolean visible) {

        if (hologram == null) return;

        if (visible) {

            if (isPaperBased) { //Paper or Folia

                ArmorStand finalHologram = hologram;
                CounterStrike.i.myBukkit.runTaskLater(null, null, hologram, () -> {
                    myBukkitPaper.hologramCustomName(finalHologram, message);
                }, 1);

            } else {
                hologram.setCustomName(message);
            }
        } else {
            if (hologram != null) {
               // Utils.debug("    Limpa ArmorStand ");

                if (!CounterStrike.i.isEnabled()) {
                    hologram.remove();
                    hologram = null;
                    return;
                }

                ArmorStand finalA = hologram;
                CounterStrike.i.myBukkit.runTaskLater(null, null, hologram, () -> {
                    finalA.remove();
                }, 1);

                hologram = null;
            }
        }

    }


    public void setMeta(ItemMeta meta, String text) {
        if (isPaperBased) {
            myBukkitPaper.metaDisplayName(meta, text);
        } else {
            meta.setDisplayName(text);
        }
    }


    public void setPlayerListName(Player player, String text) {
        if (isPaperBased) {
            myBukkitPaper.playerListName(player, text);
        } else {
            player.setPlayerListName(ChatColor.WHITE + player.getName());
        }
    }


    public void sendActionBar(Player player, String text) {
        if (isPaperBased) {
            myBukkitPaper.playerSendActionBar(player, text);
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
        }
    }


    public void consoleSendMessage(String text, String textComponent, String color) {
        if (isPaper())
            myBukkitPaper.consoleSendMessage(text, textComponent, getNamedTextColorFromString(color));
        else
            Bukkit.getConsoleSender().sendMessage(text + ChatColor.valueOf(color) + textComponent);
    }


    public void teamAddEntity(Team myTeam, Player player) {
        if (isPaperBased) {
            myTeam.addEntity(player);
        } else {
            myTeam.addPlayer(player);
        }
    }


    public void playerTeleport(Player player, Location loc) {
        if (isPaperBased) {
            runTaskLater(player, null, null, () -> player.teleportAsync(loc), 5);
        } else {
            player.teleport(loc);
        }
    }


    public void playerSetResourcePack(Player player, String resourse, String hash) {
        if (isPaperBased) {
            runTaskLater(player, null, null, () -> player.setResourcePack(resourse, hash), 2 * 20);
        } else {
            player.setResourcePack(resourse);
        }
    }


    public void setRotation(Entity entity, float yaw) {
        if (isPaperBased) {
            entity.setRotation(entity.getLocation().getPitch(), entity.getLocation().getYaw() + yaw);
        } else {
            Location loc = entity.getLocation();
            loc.setYaw((loc.getYaw() + yaw));
            entity.teleport(loc);
        }
    }


    public boolean isDownloded(PlayerResourcePackStatusEvent event) {

        if (!isLegacy) return myBukkitPaperNotLegacy.isDownloded(event);
        return false;
    }


    public void UpdateChecker(boolean loop) {
        String projectName = main.getDescription().getName();

        if (loop) {
            runTaskTimer(null, null, null, () -> {
                runCheck(projectName);
            }, 5, (240 * 60 * 20)); // every 6h
        } else {
            runTaskLater(null, null, null, () -> {
                runCheck(projectName);
            }, 20);
        }
    }


    private void runCheck(String projectName) {
        try {
            StringBuilder page = makeAsyncGetRequest("https://cld.pt/dl/download/51c19f75-8900-49f2-8e1b-a92256bf2d4a/bukkit.txt?download=true/");

            if (page != null && page.length() > 10) {
                String pagina = page.toString();

                int pointer = pagina.indexOf("project-file-name-container-" + projectName);
                pagina = pagina.substring(pointer); //smaller data

                String tmp = pagina.substring(pagina.indexOf("https://cdn.modrinth.com/"));
                String version = tmp.substring(tmp.indexOf("data-name=\"") + 11).split("\"")[0];
                String url = tmp.split("\"")[0];
                String features = tmp.substring(tmp.indexOf("features=\"") + 10).split("\"")[0];

                promptUpdate(version, url, projectName, features);
            }
        } catch (Exception e) {
            String versionMessage = "[" + projectName + "] Connection exception: " + e.getMessage();
            String intColor ="RED";

            consoleSendMessage("[" + projectName + "]", versionMessage, intColor);
        }
    }


    private StringBuilder makeAsyncGetRequest(String url) {
        StringBuilder response = new StringBuilder();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    response.append(line);
                }
            }
        } catch (Exception ex) {
        }
        return response;
    }


    private void promptUpdate(String serverVersion, String Url, String projectName, String features) {
        String versionMessage;
        String intColor = "GRAY";

        if (serverVersion == null) {
            versionMessage = " Unknown error checking version";
            intColor = "RED";

            consoleSendMessage("[" + projectName + "]", versionMessage, intColor);

            return;
        }

        String tmpServerVersion = null;
        if (serverVersion.split(" v").length > 1) tmpServerVersion = serverVersion.split(" v")[1];
        if (tmpServerVersion == null) tmpServerVersion = serverVersion.split(" ")[1];
        serverVersion = tmpServerVersion;

        String currentVersion = main.getDescription().getVersion();
        int versionStatus = checkGreater(serverVersion, currentVersion);

        if (versionStatus == -1) {
            if (features.length() > 1) {
                features = "\nFeaturing: " + features.replace("<br>", "\n") + "\n";
            } else {
                features = "";
            }

            versionMessage = "[" + projectName + "] " + " NEW VERSION: " + serverVersion + features +
                    "Available at: " + Url;

            if (!isInformed) {
                for (Player myPlayer : main.getServer().getOnlinePlayers()) {
                    if (myPlayer.isOp()) {
                        myPlayer.sendMessage(ChatColor.GREEN + versionMessage);
                        isInformed = true;
                    }
                }
            }

            versionMessage = " THERE IS A NEW UPDATE AVAILABLE Version: " + serverVersion +
                    " at: " + Url + "   " + features;
            intColor = "GREEN";

        } else if (versionStatus == 0) {
            if (!starting) return;
            versionMessage = " You have the latest released version";
            intColor = "GREEN";
        } else if (versionStatus == 1) {
            if (!starting) return;
            versionMessage = " Congrats, you are testing a new version!";
            intColor = "YELLOW";
        } else {
            if (!starting) return;
            versionMessage = " Unknown error checking version (" + versionStatus + ")" + serverVersion + "   " + currentVersion;
            intColor = "RED";
        }

        consoleSendMessage("[" + projectName + "]", versionMessage, intColor);

        starting = false;
    }


    public int checkGreater(String v1, String v2) {
        int counter = v1.split("\\.").length;

        if (counter > v2.split("\\.").length) v2 = v2 + ".0";
        if (counter < v2.split("\\.").length) {
            v1 = v1 + ".0";
            counter++;
        }

        for (int k = 0; k < counter; k++) {
            try {
                if (Integer.parseInt(v1.split("\\.")[k]) > Integer.parseInt(v2.split("\\.")[k])) {
                    return -1;
                } else if (Integer.parseInt(v1.split("\\.")[k]) < Integer.parseInt(v2.split("\\.")[k])) {
                    return 1;
                } else {
                    //next loop
                }
            } catch (Exception e) {
                return -2;
            }
        }
        return 0;//same version
    }


    public static NamedTextColor getNamedTextColorFromString(String colorName) {
        // Convert to lowercase and manually map to enum values
        switch (colorName.toLowerCase()) {
            case "black":
                return NamedTextColor.BLACK;
            case "dark_blue":
                return NamedTextColor.DARK_BLUE;
            case "dark_green":
                return NamedTextColor.DARK_GREEN;
            case "dark_aqua":
                return NamedTextColor.DARK_AQUA;
            case "dark_red":
                return NamedTextColor.DARK_RED;
            case "dark_purple":
                return NamedTextColor.DARK_PURPLE;
            case "gold":
                return NamedTextColor.GOLD;
            case "gray":
                return NamedTextColor.GRAY;
            case "dark_gray":
                return NamedTextColor.DARK_GRAY;
            case "blue":
                return NamedTextColor.BLUE;
            case "green":
                return NamedTextColor.GREEN;
            case "aqua":
                return NamedTextColor.AQUA;
            case "red":
                return NamedTextColor.RED;
            case "light_purple":
                return NamedTextColor.LIGHT_PURPLE;
            case "yellow":
                return NamedTextColor.YELLOW;
            case "white":
                return NamedTextColor.WHITE;
            default:
                // Return a default color if not found
                System.out.println("Invalid color name, defaulting to WHITE.");
                return NamedTextColor.WHITE;
        }
    }


}
