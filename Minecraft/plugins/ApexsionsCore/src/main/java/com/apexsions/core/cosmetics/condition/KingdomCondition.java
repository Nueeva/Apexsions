package com.apexsions.core.cosmetics.condition;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import org.bukkit.entity.Player;

public class KingdomCondition implements UnlockCondition {

    private final String kingdomKey;
    private final int requiredLevel;

    public KingdomCondition(String kingdomKey, int requiredLevel) {
        this.kingdomKey = kingdomKey;
        this.requiredLevel = requiredLevel;
    }

    @Override
    public boolean isMet(Player player, PlayerData data, ApexsionsCorePlugin plugin) {
        if (player.isOp()) return true;
        if (data == null || data.getRegionId() == null) return false;
        if (data.getLevel() < requiredLevel) return false;

        return plugin.getRegionManager().getRegion(data.getRegionId())
                .map(Region::getKey)
                .filter(k -> k.equalsIgnoreCase(kingdomKey))
                .isPresent();
    }

    @Override
    public String getDescription() {
        return "<gray>Khusus Kerajaan:</gray> <gold>" + kingdomKey + "</gold> <gray>(Min. Lv. " + requiredLevel + ")</gray>";
    }
}
