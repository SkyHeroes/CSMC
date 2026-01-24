package dev.danablend.counterstrike.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.danablend.counterstrike.CounterStrike;
import dev.danablend.counterstrike.csplayer.CSPlayer;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Math.abs;

public class PlayerUtils {

    public static boolean playerBehindPlayer_OLD(Player playerBehind, Player playerNotBehind) {
        double ang = Math.toDegrees(playerBehind.getLocation().getDirection().angle(playerNotBehind.getLocation().getDirection()));
        return (ang > 18 && ang < 40);
    }

    public static boolean playerBehindPlayer(Player playerBehind, Player playerNotBehind) {
        Location testy = (playerBehind.getLocation().subtract(playerNotBehind.getLocation()));

        //make sure it is not me
        if (testy.getX() != 0 && testy.getZ() != 0) {
            float PlayerBehind = playerBehind.getLocation().getYaw();
            float playerTnFront = playerNotBehind.getLocation().getYaw();

            //view range
            if (abs(PlayerBehind - playerTnFront) < 15) {
                return true;
            }
        }

        return false;
    }

    public static Location getRightHeadLocation(LivingEntity entity) {
        Location eyeLocation = entity.getEyeLocation();
        Location newLocation = eyeLocation.add(rotateRight90Degrees(eyeLocation.getDirection()).multiply(0.2));
        return newLocation;
    }

    public static Vector rotateRight90Degrees(Vector vector) {
        double x = vector.getX();
        vector.setX(-vector.getZ()).setZ(x);
        return vector;
    }

    public static Boolean isInLobbyLocation(Player player) {
        if (CounterStrike.i.getLobbyLocation() == null) return false;

        Location locRaw = CounterStrike.i.getLobbyLocation();

        Integer xx = locRaw.getBlockX();
        Integer zz = locRaw.getBlockZ();
        Location loc = player.getLocation();
        Integer x = loc.getBlockX();
        Integer z = loc.getBlockZ();

        if (x > (xx - 20) && x < (xx + 20)) {
            if (z > (zz - 20) && z < (zz + 20)) {
                return true;
            }
        }

        return false;
    }

    public static Boolean isInSpawn(CSPlayer csplayer) {

        if (csplayer == null) {
            return false;
        }

        Player player = csplayer.getPlayer();

        final Location spawn = csplayer.getSpawnLocation();
        final Location current = player.getLocation();

        if (spawn.getBlockX() > current.getBlockX() + 4 || spawn.getBlockX() < current.getBlockX() - 4) {
            return false;
        }
        if (spawn.getBlockZ() > current.getBlockZ() + 4 || spawn.getBlockZ() < current.getBlockZ() - 4) {
            return false;
        }

        return true;
    }

    //REGION SKINS
    public static void changeSkin(Player player, String targetName) {

        if (!CounterStrike.i.myBukkit.isPaper()) return;

        try {
            String[] result = CounterStrike.i.LoadDBSkins(targetName);

            if (result == null) {
                Utils.debug("Downloading skin for " + targetName);

                if (targetName.startsWith("ct") || targetName.startsWith("terr")) {
                    result = getSkin2(targetName);
                } else {
                    result = getSkin(targetName);
                }

                if (result == null) return; //skin doesn't exist
            }

            CSPlayer csplayer = CounterStrike.i.getCSPlayer(player, false, null);

            if (!csplayer.isNPC()) {
                Utils.debug(" Loading skin for player " + player.getName() + " with profile " + targetName);
                PlayerProfile playerProfile = player.getPlayerProfile();
                playerProfile.setProperty(new ProfileProperty("textures", result[0], result[1]));
                player.setPlayerProfile(playerProfile);
            } else {
               // CounterStrike.i.changeBotSkin(player);
            }
        } catch (Exception exception) {
            Utils.debug("Exception " + exception);
        }
    }


    private static String[] getSkin(String username) {

        try {
            URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);

            InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());

            String uuid = JsonParser.parseReader(reader_0).getAsJsonObject().get("id").getAsString();

            URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");

            InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());

            JsonObject properties = JsonParser.parseReader(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();

            String value = properties.get("value").getAsString();
            String signature = properties.get("signature").getAsString();

            String[] object = new String[2];
            object[0] = value;
            object[1] = signature;

            int check = CounterStrike.i.UpdateStoredSkin(username, object);

            if (check <= 0) CounterStrike.i.StoreSkin(username, object);

            return object;

        } catch (IllegalStateException | IOException | NullPointerException exception) {
        }
        return null;
    }


    private static String[] getSkin2(String username) {

        try {
            String uuid = "";
            if (username.equals("terr1")) uuid = "9aa86445570145c89436277e9233bdb1";
            if (username.equals("terr2")) uuid = "b6945799b5ec4d54b8e9575bc3d9b427";
            if (username.equals("terr3")) uuid = "a3d17eb6ee1c4056a85d625e9c71bc05";
            if (username.equals("terr4")) uuid = "8845f15e656e47c9a2d3aa83488cd65d";
            if (username.equals("terr5")) uuid = "a9fd04ea5692415f967acf57c217901e";
            if (username.equals("terr6")) uuid = "fea6feb3618145a1bd54eab404d899a6";
            if (username.equals("terr7")) uuid = "e6be8d3c135b4c2ab3cb7463000e5db6";
            if (username.equals("terr8")) uuid = "1d90a71ec6244450929ba7236ce27157";

            if (username.equals("ct1")) uuid = "54630329364d4392b0e94a4721e31b6f";
            if (username.equals("ct2")) uuid = "a89ed82b9e334f49a0e99aeec00abc2f";
            if (username.equals("ct3")) uuid = "aba204c3cecc4254bd2a29e4334068d7";
            if (username.equals("ct4")) uuid = "66e1f89e914c44daaf15a01c14736fad";
            if (username.equals("ct5")) uuid = "afbc35f2ec2f43729619372d66f684c8";
            if (username.equals("ct6")) uuid = "3362b00a33514d5896e110e0af77c373";
            if (username.equals("ct7")) uuid = "5bafb79adcfd442b9131bed828cc897f";
            if (username.equals("ct8")) uuid = "28ff74cc430c4bff872114f8741d5a14";


            URL url = new URL("https://api.mineskin.org/v2/skins/" + uuid);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
       //     con.setRequestProperty("User-Agent", "MineSkin-User-Agent");
       //     con.setRequestProperty("Authorization", "Bearer msk_lkIUtF8N_Adz5cgIACTKMlDeKDCADqaSl4I7Ej5gib4Min-4XrtjT_p1OYuYxRdSkvoFOz3hh");

            InputStreamReader reader_1 = new InputStreamReader(con.getInputStream());

            JsonObject root = JsonParser.parseReader(reader_1).getAsJsonObject();

            JsonObject texture = root
                    .getAsJsonObject("skin")
                    .getAsJsonObject("texture")
                    .getAsJsonObject("data");

            String value = texture.get("value").getAsString();
            String signature = texture.get("signature").getAsString();

            String[] object = new String[2];
            object[0] = value;
            object[1] = signature;

            int check = CounterStrike.i.UpdateStoredSkin(username, object);

            if (check <= 0) CounterStrike.i.StoreSkin(username, object);

            return object;

        } catch (IllegalStateException | IOException | NullPointerException exception) {
            CounterStrike.i.getLogger().warning("exception " + exception);
        }
        return null;
    }


}
