package io.github.alathra.horsecombat.listener;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.utility.coreutil.MomentumUtils;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

public class HorseDismountListener implements Listener {
    AlathraHorseCombat plugin = AlathraHorseCombat.getInstance();

    // Triggered when a player dismounts
    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        // Check if the entity that was dismounted is a horse and the one dismounted is a player
        if (event.getDismounted() instanceof Horse && event.getEntity() instanceof Player player) {
            // Reset momentum
            MomentumUtils.resetMomentum(player);

            // Debug message if enabled
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("[HorseCombat] Reset XP for ${player.name} after dismount");
            }
        }
    }
}
