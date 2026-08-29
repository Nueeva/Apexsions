package com.apexsions.core.cosmetics.condition;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import org.bukkit.entity.Player;

public class MonarchCondition implements UnlockCondition {

    @Override
    public boolean isMet(Player player, PlayerData data, ApexsionsCorePlugin plugin) {
        if (player.isOp()) return true;
        if (data == null || data.getRegionId() == null) return false;

        String kingdomKey = plugin.getRegionManager().getRegion(data.getRegionId())
                .map(Region::getKey)
                .orElse("NONE");

        String kingName = plugin.getConfigManager().getKingdomKing(kingdomKey);
        return kingName != null && player.getName().equalsIgnoreCase(kingName);
    }

    @Override
    public String getDescription() {
        return "<gray>Syarat Khusus:</gray> <gold><bold>Raja / Ratu Penguasa Kerajaan</bold></gold>";
    }
}
