package io.github.alathra.horsecombat.listener.combat;

import io.github.alathra.horsecombat.utility.coreutil.MomentumUtils;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

public class HorseDismountListener implements Listener {

    // Triggered when a player dismounts
    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        // Check if the entity that was dismounted is a horse and the one dismounted is a player
        if (event.getDismounted() instanceof Horse && event.getEntity() instanceof Player player) {
            // Reset momentum
            MomentumUtils.resetMomentum(player);
        }
    }
}
