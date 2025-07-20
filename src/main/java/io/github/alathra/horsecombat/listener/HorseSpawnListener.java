package io.github.alathra.horsecombat.listener;

import io.github.alathra.horsecombat.config.Settings;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class HorseSpawnListener implements Listener {
    @EventHandler
    public void onHorseSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Horse horse) {
            AttributeInstance maxHealthAttribute = horse.getAttribute(Attribute.MAX_HEALTH);

            if (maxHealthAttribute != null) {
                double boostedHealth = maxHealthAttribute.getValue() + Settings.getBonusHealth();

                maxHealthAttribute.setBaseValue(boostedHealth);
                horse.setHealth(boostedHealth);
            }
        }
    }
}
