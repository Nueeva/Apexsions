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
        String kingdom = plugin.getKingdomCoreHook().getPlayerKingdom(player);
        if (kingdom == null || kingdom.equalsIgnoreCase("NONE")) {
            // Check location kingdom
            kingdom = plugin.getKingdomCoreHook().getKingdomAtLocation(player.getLocation());
        }

        ShopCategory cat = item.getCategory();
        String path = "kingdoms." + kingdom.toUpperCase() + "." + cat.getId() + "-buy-multiplier";
        return plugin.getConfigManager().getMarketsConfig().getDouble(path, 1.00);
    }

    public double getSellMultiplier(ShopItem item, Player player) {
        // Sell price generally mirrors the kingdom's buy multiplier to preserve 20% ratio with regional variance
        double buyMult = getBuyMultiplier(item, player);
        return buyMult;
    }

    public String getKingdomNameFormatted(Player player) {
        String key = plugin.getKingdomCoreHook().getPlayerKingdom(player);
        return switch (key.toUpperCase()) {
            case "ZENITHAR" -> "<gradient:#ffe900:#f39c12><bold>Zenithar</bold></gradient>";
            case "SOLTERRA" -> "<gradient:#ff4d4d:#c0392b><bold>Solterra</bold></gradient>";
            case "SYLVAMOOR" -> "<gradient:#87ceeb:#3498db><bold>Sylvamoor</bold></gradient>";
            default -> "<gray>Tanpa Kerajaan</gray>";
        };
    }
}
