package io.github.alathra.horsecombat.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.utils.EntityTypeUtil;
import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.config.Settings;
import io.github.alathra.horsecombat.hook.Hook;
import io.github.alathra.horsecombat.utility.coreutil.HorseState;
import io.github.alathra.horsecombat.utility.coreutil.MomentumUtils;
import io.github.milkdrinkers.colorparser.ColorParser;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class HorseCombatListener implements Listener {

    AlathraHorseCombat plugin = AlathraHorseCombat.getInstance();
    private final HashMap<UUID, HorseState> horseStateMap = new HashMap<>();

    // Set to track entities currently being damaged to prevent infinite loops
    private final HashSet<UUID> entitiesBeingDamaged = new HashSet<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        // Skip if the event is already cancelled (e.g., by a claim plugin)
        if (event.isCancelled()) return;

        Entity damagingEntity = event.getDamager();

        // Skip if the attacker is not a player and initialize damagingEntity to damagingPlayer
        if (!(damagingEntity instanceof Player damagingPlayer)) return;

        Entity damagedEntity = event.getEntity();

        // Skip if the target is not a living entity (we want to hit mobs and players)
        if (!(damagedEntity instanceof LivingEntity livingEntity)) return;

        // Get UUID for tracking damage processing
        UUID damagedUuid = damagedEntity.getUniqueId();

        // Check if we're already processing damage for this entity - prevents infinite loops
        if (entitiesBeingDamaged.contains(damagedUuid)) return;

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
                if (!town.isPVP()) return;
            }

            if (damagedEntity instanceof Horse horse) {
                if (horse.getPassengers().isEmpty()) return;
            }   // if the damaged entity is anything but a ridden horse
            else {
                if (EntityTypeUtil.isInstanceOfAny(TownySettings.getProtectedEntityTypes(), damagedEntity)) return;
            }
        }

        // TODO: add option to enable if can be used on offhand
        ItemStack mainHandItem = damagingPlayer.getInventory().getItemInMainHand();

        // Check to see if the item the player is holding is a lancing item
        String lanceItemId = Settings.getItemProvider().parseItemID(mainHandItem);
        if (lanceItemId == null || !Settings.getLanceIDList().contains(lanceItemId)) return;

        // --- ATTEMPT TO APPLY HORSE COMBAT MECHANICS ---

        // Cancel vanilla damage to prevent stacking
        event.setCancelled(true);

        // Add to processing set to prevent recursion
        entitiesBeingDamaged.add(damagedUuid);

        // Attacker is on a horse
        int momentum = MomentumUtils.getMomentum(damagingPlayer);
        double originalDamage = event.getDamage();

        // Get damage multiplier
        double damageMultiplier = Settings.getPlayerDamageMultiplier();
        if (!(damagedEntity instanceof Player)) {
            damageMultiplier = Settings.getMobDamageMultiplier();
        }

        double baseDamage = getBaseDamage(momentum, originalDamage);

        // Apply the mob multiplier if target is not a player
        double damage = baseDamage * damageMultiplier;

        // Apply damage directly without causing another event
        livingEntity.damage(damage);

        // Only for debug - can be disabled in production
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("Entity hit with momentum: " + momentum + ", damage: " + damage + ", entity type: " + damagedEntity.getType());
        }

        // Handle knockoff for riders based on momentum thresholds
        int knockoffThreshold = Settings.getKnockoffThreshold(); // default is 50

        if (momentum >= knockoffThreshold && damagedEntity.getVehicle() != null) {
            if (Settings.getKnockoffChance() > Math.random()) {
                // Add delay before ejecting to avoid damage cancellation
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (damagedEntity.isValid() && damagedEntity.getVehicle() != null) {
                        damagedEntity.getVehicle().eject();
                    }
                }, 2L); // 2 tick delay (0.1 seconds)
            }
        }

        // Apply knockback if enabled
        if (damagedEntity instanceof Player) {
            if (Settings.isKnockbackPlayersEnabled() && momentum >= Settings.getKnockbackThreshold()) {
                double knockbackStrength = momentum / 50.0; // 0.5 to 2.0 based on momentum
                var direction = damagedEntity.getLocation().toVector().subtract(damagingPlayer.getLocation().toVector()).normalize();
                damagedEntity.setVelocity(direction.multiply(knockbackStrength));
            }
        } else {
            if (Settings.isKnockbackMobsEnabled() && momentum >= Settings.getKnockbackThreshold()) {
                double knockbackStrength = momentum / 50.0; // 0.5 to 2.0 based on momentum
                var direction = damagedEntity.getLocation().toVector().subtract(damagingPlayer.getLocation().toVector()).normalize();
                damagedEntity.setVelocity(direction.multiply(knockbackStrength));
            }
        }

        if (!(damagedEntity instanceof Player) && momentum >= 25) {
            double knockbackStrength = momentum / 50.0; // 0.5 to 2.0 based on momentum
            var direction = damagedEntity.getLocation().toVector().subtract(damagingPlayer.getLocation().toVector()).normalize();
            damagedEntity.setVelocity(direction.multiply(knockbackStrength));
        }

        // Use only sounds based on momentum levels - no heavy particles or entities
        playMomentumSounds(damagingPlayer, momentum);

        // Reset momentum after attack
        MomentumUtils.resetMomentum(damagingPlayer);

        // Play "clink" sound when player registers a hit
        if (Settings.isHitSoundEnabled() && momentum >= Settings.getHitSoundMinimumMomentum()) {
            for (Player nearby : damagingPlayer.getWorld().getNearbyPlayers(damagingPlayer.getLocation(), Settings.getHitSoundRange())) {
                nearby.playSound(Settings.getHitSound());
            }
        }

        if (Settings.areParticlesEnabled() && momentum >= Settings.getHitParticlesMinimumMomentum())
            damagingPlayer.getWorld().spawnParticle(Settings.getParticleType(), damagedEntity.getLocation().add(0.0, 1.0, 0.0), Settings.getParticleAmount(), Settings.getParticleSpread(), Settings.getParticleSpread(), Settings.getParticleSpread());

        // Always remove from processing set when done
        entitiesBeingDamaged.remove(damagedUuid);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getVehicle() instanceof Horse horse) {
            UUID horseUuid = horse.getUniqueId();
            long currentTime = System.currentTimeMillis();
            Location currentLocation = horse.getLocation();
            float currentYaw = currentLocation.getYaw();

            // Get HorseState
            HorseState horseState = horseStateMap.computeIfAbsent(horseUuid, id -> new HorseState(currentLocation, 0L));

            // Check if the horse has moved
            double distanceSquared = horseState.distanceSquared(currentLocation);
            double movementThreshold = Settings.getStallCancelDistance(); // default is 0.05

            if (distanceSquared < movementThreshold * movementThreshold) {
                // Horse isn't moving (or moving very little)
                Long stallTime = Settings.getStallTimeMillis(); // default is 0.5 seconds or 500ms

                if (currentTime - horseState.lastMoveTime > stallTime) {
                    // Horse has been still for the configured time
                    // Get momentum decay rate (how quickly momentum drops when standing still)
                    int decayRate = horseState.decayRate;
                    int maxDecayRate = Settings.getMaxDecayRate(); // default is 20

                    // Rapidly drop momentum, but not instantly
                    MomentumUtils.reduceMomentum(player, decayRate);

                    // Increase decay rate for progressive momentum loss
                    horseState.decayRate = Math.min(decayRate + 2, maxDecayRate);

                    updateMomentumBar(player);

                    if (plugin.isDebugEnabled() && MomentumUtils.getMomentum(player) % 10 == 0) {
                        plugin.getLogger().info("Player ${player.name} momentum decaying: ${MomentumUtils.getMomentum(player)}");
                    }
                }
            } else {
                // Calculate the difference in yaw (angle change)
                float yawDifference = horseState.yawDifference(currentYaw);
                double turnThreshold = Settings.getTurnMinDegrees(); // default 30.0

                if (yawDifference > turnThreshold) {
                    // Sharp turn detected
                    int momentumLoss = Settings.getTurnLoss(); // default is 10
                    MomentumUtils.reduceMomentum(player, momentumLoss);
                    updateMomentumBar(player);

                    if (plugin.isDebugEnabled()) {
                        plugin.getLogger().info("Player ${player.name} turning: momentum ${MomentumUtils.getMomentum(player)}");
                    }
                } else {
                    // Straight movement, increase momentum
                    int momentumGain = Settings.getBaseGain(); // default is 1
                    MomentumUtils.increaseMomentum(player, momentumGain);
                    updateMomentumBar(player);

                    if (plugin.isDebugEnabled() && MomentumUtils.getMomentum(player) % 10 == 0) {
                        plugin.getLogger().info("Player ${player.name} momentum increasing: ${MomentumUtils.getMomentum(player)}");
                    }
                }

                horseState.update(currentLocation, currentTime);
            }
        }
    }

    private void updateMomentumBar(Player player) {
        int momentum = MomentumUtils.getMomentum(player);

        String format = Settings.getActionBarFormat();
        String message = format.replace("%momentum%", String.valueOf(momentum));

        player.sendActionBar(ColorParser.of(message).build());
    }

    private void playMomentumSounds(Player player, int momentum) {
        Location loc = player.getLocation();

        if (momentum >= 100) {
            player.getWorld().playSound(loc, Sound.ENTITY_SLIME_SQUISH, 1f, 0.5f);
            player.getWorld().playSound(loc, Sound.ENTITY_SLIME_JUMP, 1f, 0.5f);
        } else if (momentum >= 75) {
            player.getWorld().playSound(loc, Sound.ENTITY_SLIME_SQUISH, 0.8f, 0.7f);
            player.getWorld().playSound(loc, Sound.ENTITY_SLIME_JUMP, 0.8f, 0.7f);
        } else if (momentum >= 50) {
            player.getWorld().playSound(loc, Sound.ENTITY_SLIME_SQUISH, 0.6f, 0.9f);
        } else if (momentum >= 25) {
            player.getWorld().playSound(loc, Sound.ENTITY_SLIME_SQUISH, 0.4f, 1.1f);
        }
    }

    private double getBaseDamage(int momentum, double damage) {
        if (momentum >= 100) return damage * Settings.getDamageMultiplierAtMaxMomentum();
        if (momentum >= 75) return damage * Settings.getDamageMultiplierFrom75To99Momentum();
        if (momentum >= 50) return damage * Settings.getDamageMultiplierFrom50To74Momentum();
        if (momentum >= 25) return damage * Settings.getDamageMultiplierFrom25To49Momentum();

        return damage * Settings.getDamageMultiplierFrom0To24Momentum();
    }
}
