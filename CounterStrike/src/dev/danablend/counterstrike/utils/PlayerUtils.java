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
import java.net.URL;

import static java.lang.Math.*;

public class PlayerUtils {

    public static boolean playerBehindPlayer_OLD(Player playerBehind, Player playerNotBehind) {
        double ang = Math.toDegrees(playerBehind.getLocation().getDirection().angle(playerNotBehind.getLocation().getDirection()));
        return (ang > 18 && ang < 40);
    }

    public static boolean playerBehindPlayer(Player playerBehind, Player playerNotBehind) {
        Location testy = (playerBehind.getLocation().subtract(playerNotBehind.getLocation()));

        //make sure it is not me
        if (testy.getX() != 0 && testy.getZ() != 0) {
            float PlayerBehind  = playerBehind.getLocation().getYaw();
            float playerTnFront = playerNotBehind.getLocation().getYaw();

            //view range
            if (abs(PlayerBehind - playerTnFront) < 15 ) {
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

        String[] result = CounterStrike.i.LoadDBSkins(targetName);

        if (result == null) {
            System.out.println("Saca " + targetName);

            if (targetName.startsWith("ct") || targetName.startsWith("terr")) {
                result = getSkin2(targetName);
            } else {
                result = getSkin(targetName);
            }

            if (result == null) return; //skin doesn't exist
        }

        PlayerProfile playerProfile = player.getPlayerProfile();
        playerProfile.setProperty(new ProfileProperty("textures", result[0], result[1]));
        player.setPlayerProfile(playerProfile);

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
            if (username.equals("terr1")) uuid = "9479ec26-246a-41e6-9672-877caa63ec16";
            if (username.equals("terr2")) uuid = "6ba92452-d9af-499d-89ee-81d2e0db6344";
            if (username.equals("terr3")) uuid = "5fa17a45-aeb0-44f2-9b69-c70915d7296c";
            if (username.equals("terr4")) uuid = "cac1b575-710e-4b5f-be53-7e3e612227c4";
            if (username.equals("terr5")) uuid = "fd7e8112-132d-432a-a9a4-3062c54b0b1c";
            if (username.equals("terr6")) uuid = "57f67d13-4dd8-4a8a-a9dc-62d42cbd3260";
            if (username.equals("terr7")) uuid = "9c655158-d3f6-4bd5-a853-7b74c5293b3b";
            if (username.equals("terr8")) uuid = "7efd0ba7-450c-4c11-9a9b-79e3a2c2e683";

            if (username.equals("ct1")) uuid = "4c012de0-5feb-4889-be1c-8244cba68466";
            if (username.equals("ct2")) uuid = "ae7f30c3-a5da-49a3-b592-9afb1913b440";
            if (username.equals("ct3")) uuid = "22cd54e0-7494-4b7a-8362-6a4fe65900a9";
            if (username.equals("ct4")) uuid = "1fb235b8-7e35-4b54-bcc8-97eaa2ba3c1c";
            if (username.equals("ct5")) uuid = "7d828bdc-b1d1-44ab-a27c-b4d36ece31c1";
            if (username.equals("ct6")) uuid = "b5df4f39-5b1a-4526-96f4-83c734cc8b2a";
            if (username.equals("ct7")) uuid = "4773ed21-3eeb-4b41-9c9b-2bc31bbd5880";
            if (username.equals("ct8")) uuid = "7d828bdc-b1d1-44ab-a27c-b4d36ece31c1";


            URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");

            InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());

            JsonObject properties = JsonParser.parseReader(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();

            String value = properties.get("value").getAsString();
            String signature = properties.get("signature").getAsString();

            String[] object = new String[2];
            object[0] = value;
            object[1] = signature;

            int check = CounterStrike.i.UpdateStoredSkin(username, object);

            System.out.println("Atualizacao devolveu " + check);

            if (check <= 0) CounterStrike.i.StoreSkin(username, object);

            return object;

        } catch (IllegalStateException | IOException | NullPointerException exception) {
        }
        return null;
    }


}
