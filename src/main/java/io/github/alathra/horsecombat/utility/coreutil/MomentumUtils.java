package io.github.alathra.horsecombat.utility.coreutil;

import org.bukkit.entity.Player;
import java.util.*;

public class MomentumUtils {
    private static HashMap<UUID, Integer> momentumMap = new HashMap<>();

    public static void increaseMomentum(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        int currentMomentum = momentumMap.getOrDefault(uuid, 0);
        int newMomentum = Math.max(0, Math.min(100, currentMomentum + amount)); // clamp values to 0-100
        momentumMap.put(uuid, newMomentum);
    }

    public static void reduceMomentum(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        int currentMomentum = momentumMap.getOrDefault(uuid, 0);
        int newMomentum = Math.max(0, currentMomentum - amount); // clamp values to at least 0
        momentumMap.put(uuid, newMomentum);
    }

    public static int getMomentum(Player player) {
        return momentumMap.getOrDefault(player.getUniqueId(), 0);
    }

    public static void resetMomentum(Player player) {
        momentumMap.put(player.getUniqueId(), 0);
    }
}
