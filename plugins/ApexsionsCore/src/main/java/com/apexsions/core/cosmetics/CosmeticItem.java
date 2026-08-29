package com.apexsions.core.cosmetics;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.cosmetics.condition.UnlockCondition;
import com.apexsions.core.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Model representing a particle cosmetic effect in Apexsions.
 */
public class CosmeticItem {

    private final String id;
    private final CosmeticType type;
    private final String displayName;
    private final String description;
    private final Material icon;
    private final Particle particle;
    private final UnlockCondition condition;
    private final int intervalTicks;

    public CosmeticItem(String id, CosmeticType type, String displayName, String description, Material icon, Particle particle, UnlockCondition condition, int intervalTicks) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.particle = particle;
        this.condition = condition;
        this.intervalTicks = Math.max(1, intervalTicks);
    }

    public String getId() {
        return id;
    }

    public CosmeticType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Material getIcon() {
        return icon;
    }

    public Particle getParticle() {
        return particle;
    }

    public UnlockCondition getCondition() {
        return condition;
    }

    public int getIntervalTicks() {
        return intervalTicks;
    }

    public boolean isUnlocked(Player player, PlayerData data, ApexsionsCorePlugin plugin) {
        if (data != null && data.hasUnlockedCosmetic(id)) {
            return true;
        }
        return condition != null && condition.isMet(player, data, plugin);
    }
}
