package io.github.alathra.horsecombat.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.config.Settings;
import io.github.alathra.horsecombat.hook.Hook;
import io.github.alathra.horsecombat.utility.coreutil.MomentumUtils;
import io.github.alathra.horsecombat.utility.itemutil.ItemProvider;
import io.github.milkdrinkers.colorparser.ColorParser;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class HorseCombatListener implements Listener {
    public static String townyBypassPermission = "horsecombat.admin.townybypass";

    AlathraHorseCombat plugin = AlathraHorseCombat.getInstance();

    // Map to store the previous yaw of each horse
    private final HashMap<UUID, Float> horseYawMap = new HashMap<>();

    // Map to store the previous location of each horse
    private final HashMap<UUID, Vector> horseLocationMap = new HashMap<>();

    // Map to store the last movement time of each horse
    private final HashMap<UUID, Long> horseLastMoveMap = new HashMap<>();

    // Set to track entities currently being damaged to prevent infinite loops
    private final HashSet<UUID> entitiesBeingDamaged = new HashSet<>();

    // Store decay rate for rapid momentum drop
    private final HashMap<UUID, Integer> momentumDecayRates = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        Entity damagingEntity = event.getDamager();
        Entity targetEntity = event.getEntity();

        // Skip if the event is already cancelled (e.g., by a claim plugin)
        if (event.isCancelled()) return;

        // Skip if the attacker is not a player and initialize damagingEntity to damagingPlayer
        if (!(damagingEntity instanceof Player damagingPlayer)) return;

        // Skip if the target is not a living entity (we want to hit mobs and players)
        if (!(targetEntity instanceof LivingEntity)) return;

        // Get UUID for tracking damage processing
        UUID targetUuid = targetEntity.getUniqueId();

        // Check if we're already processing damage for this entity - prevents infinite loops
        if (entitiesBeingDamaged.contains(targetUuid)) return;

        // Check for Towny bypass - add this check
        if (Hook.getTownyHook().isHookLoaded() && !damagingEntity.hasPermission(townyBypassPermission) && Settings.isTownyConfigEnabled()) {
            TownyAPI townyAPI = Hook.getTownyHook().getTownyAPI();

            TownBlock townBlockPlayerIsIn = townyAPI.getTownBlock(damagingPlayer);
            if (townBlockPlayerIsIn == null) return;

            Town townPlayerIsIn = townyAPI.getTownOrNull(townBlockPlayerIsIn);
            if (townPlayerIsIn == null) return;

            // If target is in a town and there's no war
            if (!townPlayerIsIn.hasActiveWar()) {
                // If attacker is not the resident of that town
                if (!townPlayerIsIn.hasResident(damagingPlayer)) {
                    if (!townPlayerIsIn.isPVP()) {
                        // Check if mob protection is enabled
                        boolean isProtectMobsEnabled = Settings.isProtectMobsEnabled(); // default true

                        // If target is a player OR (target is a mob AND we protect mobs)
                        if (targetEntity instanceof Player || isProtectMobsEnabled) {
                            // Debug message
                            if (plugin.isDebugEnabled()) {
                                plugin.getLogger().info("Blocking attack: target=" + targetEntity.getType() + ", protectMobs=" + isProtectMobsEnabled);
                            }

                            damagingPlayer.sendMessage(ColorParser.of("<dark_gray>[<dark_red>AlathraHorseCombat<dark_gray>] <red>You cannot attack in this town!").build());
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }

        //TODO add option to enable if can be used on offhand
        ItemStack lance = damagingPlayer.getInventory().getItemInMainHand();
        String lanceItemId = ItemProvider.NEXO.parseItemID(lance);

        // If item is not a nexo item or not included in the lance list in configs
        if (lanceItemId == null || !Settings.getLanceList().contains(lanceItemId)) return;

        // Cancel vanilla damage to prevent stacking
        event.setCancelled(true);

        try {
            // Add to processing set to prevent recursion
            entitiesBeingDamaged.add(targetUuid);

            if (damagingPlayer.getVehicle() instanceof Horse) {
                // Attacker is on a horse
                int momentum = MomentumUtils.getMomentum(damagingPlayer);
                double maxDamage = Settings.getMaxDamage(); // default 10.0
                double mobDamageMultiplier = 1.0;

                if (!(targetEntity instanceof Player)) {
                    mobDamageMultiplier = Settings.getMobDamageMultiplier(); // default 1.5
                }

                double baseDamage = getBaseDamage(momentum, maxDamage);

                // Apply the mob multiplier if target is not a player
                double damage = baseDamage * mobDamageMultiplier;

                // Apply damage directly without causing another event
                ((LivingEntity) targetEntity).damage(damage);

                // Only for debug - can be disabled in production
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().info("Entity hit with momentum: " + momentum + ", damage: " + damage + ", entity type: " + targetEntity.getType());
                }

                // Handle knockoff for riders based on momentum thresholds
                int knockoffThreshold = Settings.getKnockoffThreshold(); // default is 50

                if (momentum >= knockoffThreshold && targetEntity.getVehicle() != null) {
                    // Add delay before ejecting to avoid damage cancellation
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        if (targetEntity.isValid() && targetEntity.getVehicle() != null) {
                            targetEntity.getVehicle().eject();
                        }
                    }, 2L); // 2 tick delay (0.1 seconds)
                }

                // Apply knockback to mobs (not for players, as it can be annoying in PvP)
                if (!(targetEntity instanceof Player) && momentum >= 25) {
                    double knockbackStrength = momentum / 50.0; // 0.5 to 2.0 based on momentum
                    var direction = targetEntity.getLocation().toVector().subtract(damagingPlayer.getLocation().toVector()).normalize();
                    targetEntity.setVelocity(direction.multiply(knockbackStrength));
                }

                // Use only sounds based on momentum levels - no heavy particles or entities
                playMomentumSounds(damagingPlayer, momentum);

                // Reset momentum after attack
                MomentumUtils.resetMomentum(damagingPlayer);
            } else {
                // Attacker is on foot
                double footDamage;

                if (targetEntity instanceof Player) {
                    footDamage = Settings.getFootDamage(); // default is 0.5
                } else {
                    // Different damage for mobs when on foot
                    footDamage = Settings.getFootDamageMobs(); // default is 1.0
                }

                ((LivingEntity) targetEntity).damage(footDamage); // Apply configurable damage

                int slownessDuration = Settings.getSlownessDuration(); // default is 100
                int slownessLevel = Settings.getSlownessLevel(); // default is 1
                damagingPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slownessDuration, slownessLevel));
            }

            // Play hit sound - safely handle nullable value
            String hitSoundString = Settings.getSoundHit(); // default is ENTITY_SLIME_ATTACK

            try {
                damagingPlayer.getWorld().playSound(targetEntity.getLocation(), hitSoundString, 1f, 1f);
            } catch (IllegalArgumentException e) {
                // Log invalid sound and fall back to default
                if (plugin.isDebugEnabled()) {
                    plugin.getLogger().warning("Invalid sound in config: $hitSoundString. Using default sound.");
                }
                damagingPlayer.getWorld().playSound(targetEntity.getLocation(), Sound.ENTITY_SLIME_ATTACK, 1f, 1f);
            }
        } finally {
            // Always remove from processing set when done
            entitiesBeingDamaged.remove(targetUuid);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getVehicle() instanceof Horse horse) {
            UUID horseUuid = horse.getUniqueId();
            Long currentTime = System.currentTimeMillis();

            // Get current location as vector for distance calculation
            Vector currentLocation = horse.getLocation().toVector();
            float currentYaw = horse.getLocation().getYaw();

            // Get previous states
            Vector previousLocation = horseLocationMap.getOrDefault(horseUuid, currentLocation);
            float previousYaw = horseYawMap.getOrDefault(horseUuid, currentYaw);

            // Check if the horse has moved
            double distance = currentLocation.distance(previousLocation);
            double movementThreshold = Settings.getMovementThreshold(); // default is 0.05

            if (distance < movementThreshold) {
                // Horse isn't moving (or moving very little)
                Long stallTime = Settings.getStallTimeMs(); // default is 0.5 seconds or 500ms
                Long lastMoveTime = horseLastMoveMap.getOrDefault(horseUuid, currentTime);

                if (currentTime - lastMoveTime > stallTime) {
                    // Horse has been still for the configured time
                    // Get momentum decay rate (how quickly momentum drops when standing still)
                    int decayRate = momentumDecayRates.getOrDefault(horseUuid, 5);
                    int maxDecayRate = Settings.getMaxDecayRate(); // default is 20

                    // Rapidly drop momentum, but not instantly
                    MomentumUtils.reduceMomentum(player, decayRate);

                    // Increase decay rate for progressive momentum loss
                    momentumDecayRates.put(horseUuid, Math.max(decayRate + 2, maxDecayRate));

                    updateMomentumBar(player);
                    // Use only sound feedback for stopping - minimal particles
                    if (Settings.shouldShowStopEffects()) { // default true
                        player.getWorld().playSound(horse.getLocation(), Sound.ENTITY_HORSE_BREATHE, 0.5f, 0.8f);
                    }

                    if (plugin.isDebugEnabled() && MomentumUtils.getMomentum(player) % 10 == 0) {
                        plugin.getLogger().info("Player ${player.name} momentum decaying: ${MomentumUtils.getMomentum(player)}");
                    }
                }
            } else {
                // Horse is moving, update last move time
                horseLastMoveMap.put(horseUuid, currentTime);

                // Reset decay rate when moving again
                momentumDecayRates.put(horseUuid, 5);

                // Calculate the difference in yaw (angle change)
                float yawDifference = Math.abs(angleDifference(currentYaw, previousYaw));
                double turnThreshold = Settings.getTurnThreshold(); // default 30.0

                if (yawDifference > turnThreshold) {
                    // Sharp turn detected
                    int momentumLoss = Settings.getTurnLoss(); // default is 10
                    MomentumUtils.reduceMomentum(player, momentumLoss);
                    updateMomentumBar(player);

                    // Just use sound feedback for turns - no particles
                    if (Settings.shouldShowTurnEffects()) { // default is true
                        player.getWorld().playSound(horse.getLocation(), Sound.ENTITY_HORSE_BREATHE, 1f, 1f);
                    }

                    if (plugin.isDebugEnabled()) {
                        plugin.getLogger().info("Player ${player.name} turning: momentum ${MomentumUtils.getMomentum(player)}");
                    }
                } else {
                    // Straight movement, increase momentum
                    int momentumGain = Settings.getStraightGain(); // default is 1
                    MomentumUtils.increaseMomentum(player, momentumGain);
                    updateMomentumBar(player);

                    if (plugin.isDebugEnabled() && MomentumUtils.getMomentum(player) % 10 == 0) {
                        plugin.getLogger().info("Player ${player.name} momentum increasing: ${MomentumUtils.getMomentum(player)}");
                    }
                }
            }

            horseYawMap.put(horseUuid, currentYaw);
            horseLocationMap.put(horseUuid, currentLocation);
        }
    }

    // Helper function to properly calculate angle differences (handles wrap-around)
    private float angleDifference(float angle1, float angle2) {
        float diff = (angle1 - angle2) % 360;

        if (diff < -180) diff += 360;
        if (diff > 180) diff -= 360;

        return Math.abs(diff);
    }

    private void updateMomentumBar(Player player) {
        int momentum = MomentumUtils.getMomentum(player);

        // Show action bar message if configured to do so
        if (Settings.shouldUseActionBar()) { // default true
            String format = Settings.getActionBarFormat();
            String message = format.replace("%momentum%", String.valueOf(momentum));

            player.sendActionBar(ColorParser.of(message).build());
        }
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

    private double getBaseDamage(int momentum, double maxDamage) {
        if (momentum >= 100) return maxDamage;
        if (momentum >= 75) return maxDamage * 0.75;
        if (momentum >= 50) return maxDamage * 0.5;
        if (momentum >= 25) return maxDamage * 0.25;
        return maxDamage * 0.1;
    }
}
