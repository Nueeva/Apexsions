package com.apexsions.core.title;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.cosmetics.condition.UnlockCondition;
import com.apexsions.core.player.PlayerData;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Model representing an unlockable prestige Title in Apexsions.
 * Supports unlocking via specific permission nodes (apexsions.title.<id>),
 * wildcard/admin permissions, database unlocks, and custom conditions.
 */
public class TitleItem {

    private final String id;
    private final String displayName;
    private final String description;
    private final String permission;
    private final UnlockCondition condition;

    public TitleItem(String id, String displayName, String description, UnlockCondition condition) {
        this(id, displayName, description, "apexsions.title." + (id != null ? id.toLowerCase(Locale.ROOT) : ""), condition);
    }

    public TitleItem(String id, String displayName, String description, String permission, UnlockCondition condition) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.permission = (permission != null && !permission.isBlank()) ? permission : "apexsions.title." + (id != null ? id.toLowerCase(Locale.ROOT) : "");
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

    public String getPermission() {
        return permission;
    }

    public UnlockCondition getCondition() {
        return condition;
    }

    /**
     * Checks if the title is unlocked for the given player based on:
     * 1. Admin bypass / OP / Wildcard permission (apexsions.title.*, apexsions.admin)
     * 2. Direct permission node (apexsions.title.<id> or configured permission)
     * 3. Database persistent unlock (purchased or awarded)
     * 4. Requirement condition (level, kingdom rank, monarch, etc.)
     */
    public boolean isUnlocked(Player player, PlayerData data, ApexsionsCorePlugin plugin) {
        if (player == null) return false;

        // 1. Admin or Wildcard Bypass
        if (player.isOp() || player.hasPermission("apexsions.admin") || player.hasPermission("apexsions.title.*")) {
            return true;
        }

        // 2. Specific Permission Node (Default: apexsions.title.<id>)
        if (player.hasPermission("apexsions.title." + id.toLowerCase(Locale.ROOT))) {
            return true;
        }

        // 3. Custom Permission Node if specified
        if (permission != null && !permission.isBlank() && player.hasPermission(permission)) {
            return true;
        }

        // 4. Database persistent unlock
        if (data != null && data.hasUnlockedTitle(id)) {
            return true;
        }

        // 5. Condition requirement check
        return condition != null && condition.isMet(player, data, plugin);
    }
}
