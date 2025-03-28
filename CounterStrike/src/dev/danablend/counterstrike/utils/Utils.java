package dev.danablend.counterstrike.utils;

import dev.danablend.counterstrike.Config;
import dev.danablend.counterstrike.CounterStrike;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;

import java.util.*;


public class Utils {

    public static String color(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }


    public static void sendMessage(Entity ent, String msg) {
        ent.sendMessage(color(msg));
    }


    public static void debug(String msg) {
        if (Config.DEBUGGING_ENABLED) {
            CounterStrike.i.getLogger().info(msg);
        }
    }


    public static String getFormattedTimeString(long timeInSeconds) {
        String timeStr = new String();
        long sec_term = 1;
        long min_term = 60 * sec_term;
        long hour_term = 60 * min_term;
        long result = Math.abs(timeInSeconds);

        int hour = (int) (result / hour_term);
        result = result % hour_term;
        int min = (int) (result / min_term);
        result = result % min_term;
        int sec = (int) (result / sec_term);

        if (timeInSeconds < 0) {
            timeStr = "-";
        } else if (hour > 0) {
            timeStr += hour + ":";
        }

        timeStr += min + ":";

        if (sec < 10) {
            timeStr += "0" + sec;
        } else if (sec > 0) {
            timeStr += sec;
        }
        return timeStr;
    }


    // Method to sort Hashtable by Values
    public static int sortByValue(Hashtable<String, Integer> hashtable) {

        if (hashtable.size() == 0) return 0;

        Map<Integer, Integer> frequencyMap = new HashMap<>();

        // Count the frequency of each value
        for (int mapId : hashtable.values()) {
            frequencyMap.put(mapId, frequencyMap.getOrDefault(mapId, 0) + 1);
        }

        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(frequencyMap.entrySet());

        list.sort(Map.Entry.comparingByValue());

        return list.get(list.size() - 1).getKey();

    }

}
