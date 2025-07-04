package io.github.alathra.horsecombat.utility.coreutil;

import org.bukkit.Location;

public class HorseState {
    public double lastX, lastZ; // location is not stored, since Y is not used
    public float lastYaw;
    public long lastMoveTime;
    public int decayRate;

    public HorseState(Location loc, long currentTime) {
        this.lastX = loc.getX();
        this.lastZ = loc.getZ();
        this.lastYaw = loc.getYaw();
        this.lastMoveTime = currentTime;
        this.decayRate = 5;
    }

    public double distanceSquared(Location loc) {
        double dx = loc.getX() - lastX;
        double dz = loc.getZ() - lastZ;
        return dx * dx + dz * dz;
    }

    public float yawDifference(float currentYaw) {
        return Math.abs((currentYaw - lastYaw + 540f) % 360f - 180f);
    }
}
