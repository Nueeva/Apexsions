package com.apexsions.shop.dynamic;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopCategory;
import com.apexsions.shop.category.ShopItem;
import org.bukkit.entity.Player;

public class KingdomMarketService {

    private final ApexsionsShop plugin;

    public KingdomMarketService(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    public double getBuyMultiplier(ShopItem item, Player player) {
        return getBuyMultiplier(item, player, null);
    }

    public double getBuyMultiplier(ShopItem item, Player player, String kingdomOverride) {
        String kingdom = resolveKingdom(player, kingdomOverride);
        if (kingdom == null || kingdom.equalsIgnoreCase("NONE")) {
            return 1.00;
        }

        ShopCategory cat = item.getCategory();
        String path = "kingdoms." + kingdom.toUpperCase() + "." + cat.getId() + "-buy-multiplier";
        return plugin.getConfigManager().getMarketsConfig().getDouble(path, 1.00);
    }

    public double getSellMultiplier(ShopItem item, Player player) {
        return getSellMultiplier(item, player, null);
    }

    public double getSellMultiplier(ShopItem item, Player player, String kingdomOverride) {
        String kingdom = resolveKingdom(player, kingdomOverride);
        if (kingdom == null || kingdom.equalsIgnoreCase("NONE")) {
            return 1.00;
        }

        ShopCategory cat = item.getCategory();
        String sellPath = "kingdoms." + kingdom.toUpperCase() + "." + cat.getId() + "-sell-multiplier";
        if (plugin.getConfigManager().getMarketsConfig().contains(sellPath)) {
            return plugin.getConfigManager().getMarketsConfig().getDouble(sellPath);
        }

        // Sell price generally mirrors the kingdom's buy multiplier to preserve ratio with regional variance
        return getBuyMultiplier(item, player, kingdomOverride);
    }

    /**
     * Checks if this kingdom overrides the base sell-to-buy ratio (e.g. Solterra 65% on ORES).
     * Returns -1.0 if standard 20% base sell ratio applies.
     */
    public double getCustomSellRatio(ShopItem item, Player player, String kingdomOverride) {
        String kingdom = resolveKingdom(player, kingdomOverride);
        if (kingdom == null || kingdom.equalsIgnoreCase("NONE")) {
            return -1.0;
        }

        ShopCategory cat = item.getCategory();
        String ratioPath = "kingdoms." + kingdom.toUpperCase() + "." + cat.getId() + "-sell-ratio";
        if (plugin.getConfigManager().getMarketsConfig().contains(ratioPath)) {
            return plugin.getConfigManager().getMarketsConfig().getDouble(ratioPath, -1.0);
        }
        return -1.0;
    }

    public String resolveKingdom(Player player, String kingdomOverride) {
        if (kingdomOverride != null && !kingdomOverride.trim().isEmpty() && !kingdomOverride.equalsIgnoreCase("NONE")) {
            return kingdomOverride.toUpperCase();
        }
        if (player == null) return "NONE";

        String kingdom = plugin.getKingdomCoreHook().getPlayerKingdom(player);
        if (kingdom == null || kingdom.equalsIgnoreCase("NONE")) {
            kingdom = plugin.getKingdomCoreHook().getKingdomAtLocation(player.getLocation());
        }
        return kingdom != null ? kingdom.toUpperCase() : "NONE";
    }

    public String getKingdomNameFormatted(Player player) {
        return getKingdomNameFormatted(resolveKingdom(player, null));
    }

    public String getKingdomNameFormatted(String key) {
        if (key == null) return "<gray>Tanpa Kerajaan</gray>";
        return switch (key.toUpperCase()) {
            case "ZENITHAR" -> "<gradient:#ffe900:#f39c12><bold>Zenithar</bold></gradient>";
            case "SOLTERRA" -> "<gradient:#ff4d4d:#c0392b><bold>Solterra</bold></gradient>";
            case "SYLVAMOOR" -> "<gradient:#87ceeb:#3498db><bold>Sylvamoor</bold></gradient>";
            default -> "<gray>Tanpa Kerajaan</gray>";
        };
    }
}
