package com.apexsions.core.level.xp.antiabuse;

import com.apexsions.core.config.ConfigManager;
import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks movement distances and jump cooldowns to award exploration XP responsibly without tick spam.
 */
public class MovementTracker {

    private final ConfigManager configManager;
    private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();
    private final Map<UUID, Double> accumulatedWalkingDistance = new ConcurrentHashMap<>();
    private final Map<UUID, Double> accumulatedSwimmingDistance = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastJumpTimes = new ConcurrentHashMap<>();

    public MovementTracker(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean trackMovement(UUID uuid, Location from, Location to, boolean swimming, double threshold) {
        if (from.getWorld() != to.getWorld()) {
            lastLocations.put(uuid, to);
            return false;
        }

        double distance = from.distance(to);
        // Ignore teleportations or massive velocity changes (> 10 blocks in a single move event)
        if (distance > 10.0 || distance < 0.05) {
            lastLocations.put(uuid, to);
            return false;
        }

        Map<UUID, Double> map = swimming ? accumulatedSwimmingDistance : accumulatedWalkingDistance;
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
        lastJumpTimes.remove(uuid);
    }
}
