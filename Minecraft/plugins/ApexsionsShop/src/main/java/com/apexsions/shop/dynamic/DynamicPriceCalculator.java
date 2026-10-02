package com.apexsions.shop.dynamic;

import com.apexsions.core.api.Permissions;
import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopItem;
import org.bukkit.entity.Player;

/**
 * Calculates dynamic buy and sell prices for commodities based on:
 * - Base prices
 * - Dynamic Weather multipliers (Rain, Thunderstorm)
 * - Kingdom Territory Biome Specialty discounts/bonuses
 * - Real-time Global Market Supply Elasticity
 * - Configurable Price Clamping (Default 85% floor to 120% ceiling)
 * - Territory Taxation (Configured per kingdom or default)
 */
public class DynamicPriceCalculator {

    private final ApexsionsShop plugin;

    public DynamicPriceCalculator(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    public record PriceResult(
            double baseUnitPrice,
            double weatherMultiplier,
            double kingdomMultiplier,
            double supplyMultiplier,
            double eventMultiplier,
            double effectiveUnitPrice,
            int quantity,
            double rawTotalPrice,
            double taxPercent,
            double taxAmount,
            double finalTotalPrice
    ) {
        // Backward-compatible constructor for existing 10-parameter callers
        public PriceResult(
                double baseUnitPrice,
                double weatherMultiplier,
                double kingdomMultiplier,
                double supplyMultiplier,
                double effectiveUnitPrice,
                int quantity,
                double rawTotalPrice,
                double taxPercent,
                double taxAmount,
                double finalTotalPrice
        ) {
            this(baseUnitPrice, weatherMultiplier, kingdomMultiplier, supplyMultiplier, 1.0, effectiveUnitPrice, quantity, rawTotalPrice, taxPercent, taxAmount, finalTotalPrice);
        }
    }

    public PriceResult calculateBuyPrice(ShopItem item, Player player, int quantity) {
        return calculateBuyPrice(item, player, quantity, null);
    }

    public PriceResult calculateBuyPrice(ShopItem item, Player player, int quantity, String kingdomOverride) {
        quantity = Math.max(1, quantity);
        double baseUnit = item.getBaseBuyPrice();
        String activeKingdom = plugin.getKingdomMarketService().resolveKingdom(player, kingdomOverride);
        double weatherMult = player != null ? plugin.getWeatherPriceService().getBuyMultiplier(item, player.getWorld()) : 1.00;
        double kingdomMult = plugin.getKingdomMarketService().getBuyMultiplier(item, player, kingdomOverride);
        double supplyMult = plugin.getSupplyScannerService().getSupplyBuyMultiplier(item, activeKingdom);
        double eventMult = plugin.getMarketEventService() != null ? plugin.getMarketEventService().getEventBuyMultiplier(item) : 1.00;

        // Solterra Ores stability: 60% resistance against saturation drop (retains kingdom advantage while preventing infinite dumping)
        if (activeKingdom.equalsIgnoreCase("SOLTERRA") && item.getCategory() == com.apexsions.shop.category.ShopCategory.ORES) {
            supplyMult = 1.00 - ((1.00 - supplyMult) * 0.40);
        }

        double rawUnit = baseUnit * weatherMult * kingdomMult * supplyMult * eventMult;

        // Configurable Price Clamping (Default: 85% to 120% of base buy price)
        double minClamp = plugin.getConfigManager().getMarketsConfig().getDouble("clamping.min-buy-ratio", 0.85);
        double maxClamp = plugin.getConfigManager().getMarketsConfig().getDouble("clamping.max-buy-ratio", 1.20);

        double effectiveUnit = Math.max(baseUnit * minClamp, Math.min(baseUnit * maxClamp, rawUnit));
        double rawTotal = effectiveUnit * quantity;

        double taxPercent = plugin.getTaxService().getTaxPercent(player, kingdomOverride);
        double taxAmount = (rawTotal * (taxPercent / 100.0));
        double finalTotal = rawTotal + taxAmount;

        return new PriceResult(
                baseUnit,
                weatherMult,
                kingdomMult,
                supplyMult,
                eventMult,
                effectiveUnit,
                quantity,
                rawTotal,
                taxPercent,
                taxAmount,
                finalTotal
        );
    }

    public PriceResult calculateSellPrice(ShopItem item, Player player, int quantity) {
        return calculateSellPrice(item, player, quantity, null);
    }

    public PriceResult calculateSellPrice(ShopItem item, Player player, int quantity, String kingdomOverride) {
        quantity = Math.max(1, quantity);

        String activeKingdom = plugin.getKingdomMarketService().resolveKingdom(player, kingdomOverride);
        double customRatio = plugin.getKingdomMarketService().getCustomSellRatio(item, player, kingdomOverride);

        // Standard sell is 20% of base buy, or custom kingdom ratio (e.g. Solterra Ores 30%)
        double baseUnit = (customRatio > 0) ? (item.getBaseBuyPrice() * customRatio) : item.getBaseSellPrice();

        double weatherMult = player != null ? plugin.getWeatherPriceService().getSellMultiplier(item, player.getWorld()) : 1.00;
        double kingdomMult = plugin.getKingdomMarketService().getSellMultiplier(item, player, kingdomOverride);
        double supplyMult = plugin.getSupplyScannerService().getSupplySellMultiplier(item, activeKingdom);
        double eventMult = plugin.getMarketEventService() != null ? plugin.getMarketEventService().getEventSellMultiplier(item) : 1.00;

        if (activeKingdom.equalsIgnoreCase("SOLTERRA") && item.getCategory() == com.apexsions.shop.category.ShopCategory.ORES) {
            // Solterra Ores Resilience: 60% resistance against saturation drop
            supplyMult = 1.00 - ((1.00 - supplyMult) * 0.40);
        }

        double rawUnit = baseUnit * weatherMult * kingdomMult * supplyMult * eventMult;

        // Configurable Price Clamping
        double minClamp = plugin.getConfigManager().getMarketsConfig().getDouble("clamping.min-sell-ratio", 0.85);
        double maxClamp = plugin.getConfigManager().getMarketsConfig().getDouble("clamping.max-sell-ratio", 1.20);

        double effectiveUnit = Math.max(baseUnit * minClamp, Math.min(baseUnit * maxClamp, rawUnit));
        double baseRawTotal = effectiveUnit * quantity;

        // Rank Sell Price Bonus: Ascendant +3%, Archon +5%, Sovereign +8%, Emperor +12%, Sions +17%
        double rankBonusMultiplier = getRankSellBonusMultiplier(player);
        double rawTotal = baseRawTotal * rankBonusMultiplier;

        double taxPercent = plugin.getTaxService().getTaxPercent(player, kingdomOverride);
        double taxAmount = (rawTotal * (taxPercent / 100.0));
        // For selling: Player receives raw total minus tax
        double finalTotal = Math.max(0.0, rawTotal - taxAmount);

        return new PriceResult(
                baseUnit,
                weatherMult,
                kingdomMult,
                supplyMult,
                eventMult,
                effectiveUnit,
                quantity,
                rawTotal,
                taxPercent,
                taxAmount,
                finalTotal
        );
    }

    public double getRankSellBonusMultiplier(Player player) {
        if (player == null) return 1.0;
        if (player.hasPermission("apexsions.shop.sellbonus.sions") || player.hasPermission(Permissions.RANK_SIONS)) return 1.17; // +17%
        if (player.hasPermission("apexsions.shop.sellbonus.emperor") || player.hasPermission(Permissions.RANK_EMPEROR)) return 1.12; // +12%
        if (player.hasPermission("apexsions.shop.sellbonus.sovereign") || player.hasPermission(Permissions.RANK_SOVEREIGN)) return 1.08; // +8%
        if (player.hasPermission("apexsions.shop.sellbonus.archon") || player.hasPermission(Permissions.RANK_ARCHON)) return 1.05; // +5%
        if (player.hasPermission("apexsions.shop.sellbonus.ascendant") || player.hasPermission(Permissions.RANK_ASCENDANT)) return 1.03; // +3%
        return 1.0;
    }
}
