package io.github.alathra.horsecombat.listener;

import com.google.common.base.Function;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class HorseCombatListener implements Listener {

    AlathraHorseCombat plugin = AlathraHorseCombat.getInstance();
    private final HashMap<UUID, HorseState> horseStateMap = new HashMap<>();
    // Player UUID, Entity ID
    private static final Map<UUID, Integer> offhandTaggedEntities = new HashMap<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {

        // Skip if the event is already cancelled (e.g., by a claim plugin)
        if (event.isCancelled()) return;

        Entity damagingEntity = event.getDamager();

        // Skip if the attacker is not a player and initialize damagingEntity to damagingPlayer
        if (!(damagingEntity instanceof Player damagingPlayer)) return;

        Entity damagedEntity = event.getEntity();

        // Check for offhand tag, called event
        if (offhandTaggedEntities.containsValue(damagedEntity.getEntityId())) {
            lanceStrike(damagingPlayer, damagedEntity, event.getDamage(), event);
            plugin.getServer().getScheduler().runTaskLater(plugin, ()-> offhandTaggedEntities.remove(damagingPlayer.getUniqueId()), 1L);
            return;
        }

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

            if (!Settings.isOffhandCombatEnabled()) return;

            // --- PREPARE LANCE STRIKE FOR OFFHAND ---
            ItemStack offHandItem = damagingPlayer.getInventory().getItemInOffHand();
            String lanceItemIDInOffHand = Settings.getItemProvider().parseItemID(offHandItem);
            // If lance is not in offhand, exit
            if(lanceItemIDInOffHand == null || !Settings.getLanceIDList().contains(lanceItemIDInOffHand)) return;

            // TAG ENTITY
            offhandTaggedEntities.put(damagingPlayer.getUniqueId(), damagedEntity.getEntityId());

            // CALL NEW DAMAGE EVENT
            Map<EntityDamageEvent.DamageModifier, Double> modifiers = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
            modifiers.put(EntityDamageEvent.DamageModifier.BASE, event.getDamage());
            Map<EntityDamageEvent.DamageModifier, Function<? super Double, Double>> modifierFunctions = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
            modifierFunctions.put(EntityDamageEvent.DamageModifier.BASE, d -> d);
            EntityDamageByEntityEvent offHandEvent = new EntityDamageByEntityEvent (
                damagingEntity,
                damagedEntity,
                EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                event.getDamageSource(),
                modifiers,
                modifierFunctions,
                false
            );
            Bukkit.getPluginManager().callEvent(offHandEvent);
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

        // Use only sounds based on momentum levels - no heavy particles or entities
        playMomentumSounds(attacker, momentum);

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
                    int momentumGain = Settings.getBaseGain();
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

    public static Map<UUID, Integer> getOffhandTaggedEntities() {
        return offhandTaggedEntities;
    }
}
