package com.apexsions.core.cosmetics.condition;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import org.bukkit.entity.Player;

/**
 * Strategy interface representing dynamic, extensible unlock requirements
 * for Titles, Badges, and Particle Cosmetics.
 */
public interface UnlockCondition {

    /**
     * Checks whether the player currently fulfills this unlock condition.
     */
    boolean isMet(Player player, PlayerData data, ApexsionsCorePlugin plugin);

    /**
     * Returns a human-readable, MiniMessage-friendly requirement description.
     */
    String getDescription();
}
