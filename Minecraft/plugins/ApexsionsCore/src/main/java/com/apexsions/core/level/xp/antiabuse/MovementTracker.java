package com.apexsions.core.level.xp.antiabuse;

import com.apexsions.core.config.ConfigManager;
import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks movement distances and jump cooldowns to award exploration XP responsibly without tick spam.
 * Supports walking, swimming, elytra gliding, and vehicle/mount riding.
 */
public class MovementTracker {

    public enum MovementMode {
        WALKING,
        SWIMMING,
        GLIDING,
        RIDING
    }

    private final ConfigManager configManager;
    private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();
    private final Map<UUID, Double> accumulatedWalkingDistance = new ConcurrentHashMap<>();
    private final Map<UUID, Double> accumulatedSwimmingDistance = new ConcurrentHashMap<>();
    private final Map<UUID, Double> accumulatedGlidingDistance = new ConcurrentHashMap<>();
    private final Map<UUID, Double> accumulatedRidingDistance = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastJumpTimes = new ConcurrentHashMap<>();

    public MovementTracker(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Backward-compatible movement tracker method.
     */
    public boolean trackMovement(UUID uuid, Location from, Location to, boolean swimming, double threshold) {
        return trackMovement(uuid, from, to, swimming ? MovementMode.SWIMMING : MovementMode.WALKING, threshold);
    }

    /**
     * Tracks player movement for the specified movement mode.
     */
    public boolean trackMovement(UUID uuid, Location from, Location to, MovementMode mode, double threshold) {
        if (from.getWorld() != to.getWorld()) {
            lastLocations.put(uuid, to);
            return false;
        }

        double distance = from.distance(to);

        // Max velocity check tailored per movement mode (Elytra & Riding move much faster than walking)
        double maxAllowedSpeed = switch (mode) {
            case GLIDING -> 50.0;
            case RIDING -> 25.0;
            case WALKING, SWIMMING -> 10.0;
        };

        if (distance > maxAllowedSpeed || distance < 0.05) {
            lastLocations.put(uuid, to);
            return false;
        }

        Map<UUID, Double> map = switch (mode) {
            case GLIDING -> accumulatedGlidingDistance;
            case RIDING -> accumulatedRidingDistance;
            case SWIMMING -> accumulatedSwimmingDistance;
            case WALKING -> accumulatedWalkingDistance;
        };

        double current = map.getOrDefault(uuid, 0.0) + distance;

        if (current >= threshold) {
            map.put(uuid, 0.0);
            return true;
        } else {
            map.put(uuid, current);
            return false;
        }
    }

    public boolean canGainJumpXp(UUID uuid) {
        long now = System.currentTimeMillis();
        long lastJump = lastJumpTimes.getOrDefault(uuid, 0L);
        long cooldownMs = configManager.getJumpCooldownSeconds() * 1000L;

        if (now - lastJump >= cooldownMs) {
            lastJumpTimes.put(uuid, now);
            return true;
        }
        return false;
    }

    public void removePlayer(UUID uuid) {
        lastLocations.remove(uuid);
        accumulatedWalkingDistance.remove(uuid);
        accumulatedSwimmingDistance.remove(uuid);
        accumulatedGlidingDistance.remove(uuid);
        accumulatedRidingDistance.remove(uuid);
        lastJumpTimes.remove(uuid);
    }
}
