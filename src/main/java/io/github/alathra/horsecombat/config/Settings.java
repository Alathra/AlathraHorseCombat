package io.github.alathra.horsecombat.config;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.utility.Cfg;
import io.github.alathra.horsecombat.utility.Logger;
import io.github.alathra.horsecombat.utility.itemutil.ItemProvider;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.Subst;

import java.util.ArrayList;
import java.util.List;

public class Settings {
    public static AlathraHorseCombat plugin;

    public static void init(AlathraHorseCombat plugin) {
        Settings.plugin = plugin;
    }

    // Towny configs
    public static boolean isTownyConfigEnabled() {
        return plugin.getConfigHandler().getConfig().getOrDefault("towny.enabled", true);
    }

    public static List<String> getLanceIDList() {
        // TODO: populate vanilla ids here
        return plugin.getConfigHandler().getConfig().getOrDefault("lance.itemIdList", List.of());
    }

    public static ItemProvider getItemProvider() {
        String itemProviderString = Cfg.get().getOrDefault("lance.itemProvider", "VANILLA");
        if (itemProviderString.isBlank() || itemProviderString.isEmpty())
            itemProviderString = "VANILLA";

        try {
            return ItemProvider.valueOf(itemProviderString);
        } catch (IllegalStateException e) {
            Logger.get().warn("Invalid 'ItemProvider' defined in config.yml. Defaulting to vanilla...");
            return ItemProvider.VANILLA;
        }
    }

    // Combat configs
    public static double getMobDamageMultiplier() {
        return plugin.getConfigHandler().getConfig().getOrDefault("combat.mobDamageMultiplier", 1.5);
    }

    public static double getPlayerDamageMultiplier() {
        return plugin.getConfigHandler().getConfig().getOrDefault("combat.playerDamageMultiplier", 1.5);
    }

    public static int getKnockoffThreshold() {
        return plugin.getConfigHandler().getConfig().getOrDefault("combat.knockoffThreshold", 50);
    }

    public static double getKnockoffChance() {
        return plugin.getConfigHandler().getConfig().getOrDefault("combat.knockoffChance", 0.33);
    }

    // Sound Configs
    public static boolean isHitSoundEnabled() {
        return Cfg.get().getOrDefault("sounds.hit.enabled", false);
    }

    public static Sound getHitSound() {
        @Subst("minecraft:entity.zombie.attack_iron_door") String soundID = Cfg.get().getOrDefault("sounds.hit.effect", "minecraft:entity.zombie.attack_iron_door");
        float volume = Cfg.get().getOrDefault("GeneralSettings.LockpickSound.volume", 1.0).floatValue();
        float pitch = Cfg.get().getOrDefault("GeneralSettings.LockpickSound.pitch", 1.0).floatValue();

        return Sound.sound()
            .type(Key.key(soundID))
            .source(Sound.Source.BLOCK)
            .volume(volume)
            .pitch(pitch)
            .build();
    }

    // Momentum Configs
    public static double getMovementThreshold() {
        return plugin.getConfigHandler().getConfig().getOrDefault("momentum.movementThreshold", 0.7);
    }

    public static Long getStallTimeMs() {
        return plugin.getConfigHandler().getConfig().getOrDefault("momentum.stallTimeMs", 500L);
    }

    public static int getMaxDecayRate() {
        return plugin.getConfigHandler().getConfig().getOrDefault("momentum.maxDecayRate", 20);
    }

    public static double getTurnThreshold() {
        return plugin.getConfigHandler().getConfig().getOrDefault("momentum.turnThreshold", 30.0);
    }

    public static int getTurnLoss() {
        return plugin.getConfigHandler().getConfig().getOrDefault("momentum.turnLoss", 10);
    }

    public static int getStraightGain() {
        return plugin.getConfigHandler().getConfig().getOrDefault("momentum.straightGain", 2);
    }

    public static double getDamageMultiplierAtMaxMomentum() {
        return plugin.getConfigHandler().getConfig().getOrDefault("momentum.momentumDamageMultiplier.momentum_100", 2.5);
    }

    public static double getDamageMultiplierFrom75To99Momentum() {
        return plugin.getConfigHandler().getConfig().getOrDefault("momentum.momentumDamageMultiplier.momentum_75-99", 2);
    }

    public static double getDamageMultiplierFrom50To74Momentum() {
        return plugin.getConfigHandler().getConfig().getOrDefault("momentum.momentumDamageMultiplier.momentum_50-74", 1.5);
    }

    public static double getDamageMultiplierFrom25To49Momentum() {
        return plugin.getConfigHandler().getConfig().getOrDefault("momentum.momentumDamageMultiplier.momentum_25-49", 1);
    }

    public static double getDamageMultiplierFrom0To24Momentum() {
        return plugin.getConfigHandler().getConfig().getOrDefault("momentum.momentumDamageMultiplier.momentum_0-24", 0.5);
    }

    // Display Settings
    public static String getActionBarFormat() {
        return plugin.getConfigHandler().getConfig().getOrDefault("display.actionBarFormat", "<aqua>Momentum: %momentum%/100");
    }
}
