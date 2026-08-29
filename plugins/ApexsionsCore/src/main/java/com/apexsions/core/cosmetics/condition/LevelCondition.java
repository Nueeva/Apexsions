package com.apexsions.core.cosmetics.condition;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import org.bukkit.entity.Player;

public class LevelCondition implements UnlockCondition {

    private final int requiredLevel;

    public LevelCondition(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    @Override
    public boolean isMet(Player player, PlayerData data, ApexsionsCorePlugin plugin) {
        if (player.isOp()) return true;
        if (data == null) return false;
        return data.getLevel() >= requiredLevel;
    }

    @Override
    public String getDescription() {
        return "<gray>Syarat Level:</gray> <yellow>Lv. " + requiredLevel + "</yellow>";
    }
}
