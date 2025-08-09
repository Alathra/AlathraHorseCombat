package io.github.alathra.horsecombat.listener.combat;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.config.Settings;
import io.github.alathra.horsecombat.utility.coreutil.HorseState;
import io.github.alathra.horsecombat.utility.coreutil.MomentumUtils;
import io.github.milkdrinkers.colorparser.ColorParser;
import org.bukkit.Location;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.UUID;

public class MomentumListener implements Listener {

    private AlathraHorseCombat plugin;
    private final HashMap<UUID, HorseState> horseStateMap = new HashMap<>();

    public MomentumListener(AlathraHorseCombat plugin) {
        this.plugin = plugin;
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
}
