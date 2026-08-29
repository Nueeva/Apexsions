package com.apexsions.core.cosmetics.condition;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import org.bukkit.entity.Player;

public class PermissionCondition implements UnlockCondition {

    private final String permission;
    private final String displayHint;

    public PermissionCondition(String permission, String displayHint) {
        this.permission = permission;
        this.displayHint = displayHint;
    }

    @Override
    public boolean isMet(Player player, PlayerData data, ApexsionsCorePlugin plugin) {
        if (player.isOp()) return true;
        return player.hasPermission(permission);
    }

    @Override
    public String getDescription() {
        return "<gray>Akses Khusus:</gray> <light_purple>" + (displayHint != null ? displayHint : permission) + "</light_purple>";
    }
}
