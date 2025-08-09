package io.github.alathra.horsecombat.listener.comat;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.utils.EntityTypeUtil;
import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.config.Settings;
import io.github.alathra.horsecombat.hook.Hook;
import io.github.alathra.horsecombat.utility.coreutil.MomentumUtils;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class LanceStrikeListener implements Listener {

    private AlathraHorseCombat plugin;

    public LanceStrikeListener(AlathraHorseCombat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {

        // Skip if the event is already cancelled (e.g., by a claim plugin)
        if (event.isCancelled()) return;

        Entity damagingEntity = event.getDamager();

        // Skip if the attacker is not a player and initialize damagingEntity to damagingPlayer
        if (!(damagingEntity instanceof Player damagingPlayer)) return;

        Entity damagedEntity = event.getEntity();

        // Skip if the target is not a living entity (we want to hit mobs and players)
        if (!(damagedEntity instanceof LivingEntity)) return;

        // Check for Towny bypass - add this check
        if (Hook.Towny.isLoaded() && Settings.isTownyCompatibilityEnabled()) {
            // Get Towny API
            TownyAPI townyAPI = Hook.getTownyHook().getTownyAPI();
            // Get town of entity being damaged, if exists
            Town town = townyAPI.getTown(damagedEntity.getLocation());
            // Town exists
            if (town != null) {
                // Player is attacking an entity in their own town
                if (town.hasResident(damagingPlayer)) return;
                // Town down not have pvp, so we should disable horse combat damage
                if (!town.isPVP() && damagedEntity instanceof Player) return;
                if (damagedEntity instanceof Horse horse) {
                    if (horse.getPassengers().isEmpty()) return;
                }   // if the damaged entity is anything but a ridden horse
                else {
                    if (EntityTypeUtil.isInstanceOfAny(TownySettings.getProtectedEntityTypes(), damagedEntity)) return;
                }
            }
        }

        // Check to see if the item the player is holding is a lancing item
        ItemStack mainHandItem = damagingPlayer.getInventory().getItemInMainHand();
        String lanceItemIDInMainHand = Settings.getItemProvider().parseItemID(mainHandItem);
        if (lanceItemIDInMainHand == null || !Settings.getLanceIDList().contains(lanceItemIDInMainHand)) {
            return;
        }

        // lance strike for main hand
        lanceStrike(damagingPlayer, damagedEntity, event.getDamage(), event);
    }

    private void lanceStrike(Player attacker, Entity lancedEntity, double originalDamage, EntityDamageByEntityEvent event) {

        // Attacker is on a horse
        int momentum = MomentumUtils.getMomentum(attacker);

        // Get damage multiplier
        double damageMultiplier = Settings.getPlayerDamageMultiplier();
        if (!(lancedEntity instanceof Player)) {
            damageMultiplier = Settings.getMobDamageMultiplier();
        }

        double baseDamage = getBaseDamage(momentum, originalDamage);

        // Apply the mob multiplier if target is not a player
        double damage = baseDamage * damageMultiplier;

        // Apply damage
        event.setDamage(damage);

        // Only for debug - can be disabled in production
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("Entity hit with momentum: " + momentum + ", damage: " + damage + ", entity type: " + lancedEntity.getType());
        }

        // Handle knockoff for riders based on momentum thresholds
        int knockoffThreshold = Settings.getKnockoffThreshold(); // default is 50

        if (momentum >= knockoffThreshold && lancedEntity.getVehicle() != null) {
            if (Settings.getKnockoffChance() > Math.random()) {
                // Add delay before ejecting to avoid damage cancellation
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (lancedEntity.isValid() && lancedEntity.getVehicle() != null) {
                        lancedEntity.getVehicle().eject();
                    }
                }, 2L); // 2 tick delay (0.1 seconds)
            }
        }

        // Apply knockback if enabled
        if (lancedEntity instanceof Player) {
            if (Settings.isKnockbackPlayersEnabled() && momentum >= Settings.getKnockbackThreshold()) {
                double knockbackStrength = (momentum / 25.0) * Settings.getKnockbackMultiplier(); // 0.5 to 2.0 based on momentum
                var direction = lancedEntity.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
                lancedEntity.setVelocity(direction.multiply(knockbackStrength));
            }
        } else {
            if (Settings.isKnockbackMobsEnabled() && momentum >= Settings.getKnockbackThreshold()) {
                double knockbackStrength = (momentum / 25.0) * Settings.getKnockbackMultiplier(); // 0.5 to 2.0 based on momentum
                var direction = lancedEntity.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
                lancedEntity.setVelocity(direction.multiply(knockbackStrength));
            }
        }

        // Reset momentum after attack
        MomentumUtils.resetMomentum(attacker);

        // Play "clink" sound when player registers a hit
        if (Settings.isHitSoundEnabled() && momentum >= Settings.getHitSoundMinimumMomentum()) {
            for (Player nearby : attacker.getWorld().getNearbyPlayers(attacker.getLocation(), Settings.getHitSoundRange())) {
                nearby.playSound(Settings.getHitSound());
            }
        }

        if (Settings.areParticlesEnabled() && momentum >= Settings.getHitParticlesMinimumMomentum())
            attacker.getWorld().spawnParticle(Settings.getParticleType(), attacker.getLocation().add(0.0, 1.0, 0.0), Settings.getParticleAmount(), Settings.getParticleSpread(), Settings.getParticleSpread(), Settings.getParticleSpread());
    }

    private double getBaseDamage(int momentum, double damage) {
        if (momentum >= 100) return damage * Settings.getDamageMultiplierAtMaxMomentum();
        if (momentum >= 75) return damage * Settings.getDamageMultiplierFrom75To99Momentum();
        if (momentum >= 50) return damage * Settings.getDamageMultiplierFrom50To74Momentum();
        if (momentum >= 25) return damage * Settings.getDamageMultiplierFrom25To49Momentum();

        return damage * Settings.getDamageMultiplierFrom0To24Momentum();
    }

}
