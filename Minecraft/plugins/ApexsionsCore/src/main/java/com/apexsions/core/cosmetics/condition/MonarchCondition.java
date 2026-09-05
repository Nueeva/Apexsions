package com.apexsions.core.cosmetics.condition;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import org.bukkit.entity.Player;

public class MonarchCondition implements UnlockCondition {

    private final String requiredKingdom;

    public MonarchCondition() {
        this(null);
    }

    public MonarchCondition(String requiredKingdom) {
        this.requiredKingdom = requiredKingdom;
    }

    @Override
    public boolean isMet(Player player, PlayerData data, ApexsionsCorePlugin plugin) {
        if (player == null) return false;

        if (requiredKingdom != null) {
            String kingName = plugin.getConfigManager().getKingdomKing(requiredKingdom);
            return kingName != null && player.getName().equalsIgnoreCase(kingName);
        }

        for (String k : new String[]{"ZENITHAR", "SOLTERRA", "SYLVAMOOR"}) {
            String kingName = plugin.getConfigManager().getKingdomKing(k);
            if (kingName != null && player.getName().equalsIgnoreCase(kingName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        if (requiredKingdom != null) {
            return "<gray>Syarat Khusus:</gray> <gold><bold>Raja / Ratu " + requiredKingdom + "</bold></gold>";
        }
        return "<gray>Syarat Khusus:</gray> <gold><bold>Raja / Ratu Penguasa Kerajaan</bold></gold>";
    }
}
