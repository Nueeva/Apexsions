package com.apexsions.shop.dynamic;

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
            double effectiveUnitPrice,
            int quantity,
            double rawTotalPrice,
            double taxPercent,
            double taxAmount,
            double finalTotalPrice
    ) {}

    public PriceResult calculateBuyPrice(ShopItem item, Player player, int quantity) {
        return calculateBuyPrice(item, player, quantity, null);
    }

    public PriceResult calculateBuyPrice(ShopItem item, Player player, int quantity, String kingdomOverride) {
        quantity = Math.max(1, quantity);
        double baseUnit = item.getBaseBuyPrice();

        double weatherMult = player != null ? plugin.getWeatherPriceService().getBuyMultiplier(item, player.getWorld()) : 1.00;
        double kingdomMult = plugin.getKingdomMarketService().getBuyMultiplier(item, player, kingdomOverride);
        double supplyMult = plugin.getSupplyScannerService().getSupplyBuyMultiplier(item);

        String activeKingdom = plugin.getKingdomMarketService().resolveKingdom(player, kingdomOverride);
        // Solterra Ores stability: immune to market supply saturation
        if (activeKingdom.equalsIgnoreCase("SOLTERRA") && item.getCategory() == com.apexsions.shop.category.ShopCategory.ORES) {
            supplyMult = 1.00;
        }

        double rawUnit = baseUnit * weatherMult * kingdomMult * supplyMult;

        // Configurable Price Clamping (Default: 85% to 120% of base buy price)
        double minClamp = plugin.getConfigManager().getMarketsConfig().getDouble("clamping.min-buy-ratio", 0.85);
        double maxClamp = plugin.getConfigManager().getMarketsConfig().getDouble("clamping.max-buy-ratio", 1.20);
        if (activeKingdom.equalsIgnoreCase("ZENITHAR")) {
            // Zenithar price instability: wider fluctuation bounds
            minClamp *= 0.90;
            maxClamp *= 1.15;
        }

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

        // Standard sell is 20% of base buy, or custom kingdom ratio (e.g. Solterra Ores 65%)
        double baseUnit = (customRatio > 0) ? (item.getBaseBuyPrice() * customRatio) : item.getBaseSellPrice();

        double weatherMult = player != null ? plugin.getWeatherPriceService().getSellMultiplier(item, player.getWorld()) : 1.00;
        double kingdomMult = plugin.getKingdomMarketService().getSellMultiplier(item, player, kingdomOverride);
        double supplyMult = plugin.getSupplyScannerService().getSupplySellMultiplier(item, player, quantity);

        if (activeKingdom.equalsIgnoreCase("SOLTERRA") && item.getCategory() == com.apexsions.shop.category.ShopCategory.ORES) {
            supplyMult = 1.00; // Stabil tanpa saturasi drop
        }

        double rawUnit = baseUnit * weatherMult * kingdomMult * supplyMult;

        // Configurable Price Clamping
        double minClamp = plugin.getConfigManager().getMarketsConfig().getDouble("clamping.min-sell-ratio", 0.85);
        double maxClamp = plugin.getConfigManager().getMarketsConfig().getDouble("clamping.max-sell-ratio", 1.20);
        if (activeKingdom.equalsIgnoreCase("ZENITHAR")) {
            minClamp *= 0.85;
            maxClamp *= 1.10;
        }

        double effectiveUnit = Math.max(baseUnit * minClamp, Math.min(baseUnit * maxClamp, rawUnit));
        double rawTotal = effectiveUnit * quantity;

        double taxPercent = plugin.getTaxService().getTaxPercent(player, kingdomOverride);
        double taxAmount = (rawTotal * (taxPercent / 100.0));
        // For selling: Player receives raw total minus tax
        double finalTotal = Math.max(0.0, rawTotal - taxAmount);

        return new PriceResult(
                baseUnit,
                weatherMult,
                kingdomMult,
                supplyMult,
                effectiveUnit,
                quantity,
                rawTotal,
                taxPercent,
                taxAmount,
                finalTotal
        );
    }
}
