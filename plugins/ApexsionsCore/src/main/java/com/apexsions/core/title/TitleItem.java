package com.apexsions.core.title;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.cosmetics.condition.UnlockCondition;
import com.apexsions.core.player.PlayerData;
import org.bukkit.entity.Player;

/**
 * Model representing an unlockable prestige Title in Apexsions.
 */
public class TitleItem {

    private final String id;
    private final String displayName;
    private final String description;
    private final UnlockCondition condition;

    public TitleItem(String id, String displayName, String description, UnlockCondition condition) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.condition = condition;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public UnlockCondition getCondition() {
        return condition;
    }

    public boolean isUnlocked(Player player, PlayerData data, ApexsionsCorePlugin plugin) {
        if (data != null && data.hasUnlockedTitle(id)) {
            return true;
        }
        return condition != null && condition.isMet(player, data, plugin);
    }
}
