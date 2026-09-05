package com.apexsions.core.level.xp.antiabuse;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.apexsions.core.config.ConfigManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Tracks PvP kills to prevent rapid repeat-kill XP farming on the same player.
 */
public class PvpKillTracker {

    private final Cache<String, Long> killCooldowns;
    private final ConfigManager configManager;

    public PvpKillTracker(ConfigManager configManager) {
        this.configManager = configManager;
        this.killCooldowns = Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(configManager.getPvpKillCooldownSeconds(), TimeUnit.SECONDS)
                .build();
    }

    /**
     * Checks if the kill is eligible for XP and records the kill if eligible.
     */
    public boolean canGainPvpXp(UUID killer, UUID victim) {
        if (killer.equals(victim)) {
            return false;
        }

        String pairKey = killer + ":" + victim;
        Long lastKill = killCooldowns.getIfPresent(pairKey);
        long now = System.currentTimeMillis();

        if (lastKill != null) {
            long cooldownMs = configManager.getPvpKillCooldownSeconds() * 1000L;
            if (now - lastKill < cooldownMs) {
                return false; // On cooldown
            }
        }

        killCooldowns.put(pairKey, now);
        return true;
    }
}
