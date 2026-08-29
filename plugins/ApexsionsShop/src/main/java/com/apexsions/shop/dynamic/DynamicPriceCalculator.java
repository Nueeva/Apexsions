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
        quantity = Math.max(1, quantity);
        double baseUnit = item.getBaseBuyPrice();

        double weatherMult = plugin.getWeatherPriceService().getBuyMultiplier(item, player.getWorld());
        double kingdomMult = plugin.getKingdomMarketService().getBuyMultiplier(item, player);
        double supplyMult = plugin.getSupplyScannerService().getSupplyBuyMultiplier(item);

        double rawUnit = baseUnit * weatherMult * kingdomMult * supplyMult;

        // Configurable Price Clamping (Default: 85% to 120% of base buy price)
        double minClamp = plugin.getConfigManager().getMarketsConfig().getDouble("clamping.min-buy-ratio", 0.85);
        double maxClamp = plugin.getConfigManager().getMarketsConfig().getDouble("clamping.max-buy-ratio", 1.20);
        double effectiveUnit = Math.max(baseUnit * minClamp, Math.min(baseUnit * maxClamp, rawUnit));
        double rawTotal = effectiveUnit * quantity;

        double taxPercent = plugin.getTaxService().getTaxPercent(player);
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
        quantity = Math.max(1, quantity);
        // Base sell is strictly 20% of base buy
        double baseUnit = item.getBaseSellPrice();

        double weatherMult = plugin.getWeatherPriceService().getSellMultiplier(item, player.getWorld());
        double kingdomMult = plugin.getKingdomMarketService().getSellMultiplier(item, player);
        double supplyMult = plugin.getSupplyScannerService().getSupplySellMultiplier(item, player, quantity);

        double rawUnit = baseUnit * weatherMult * kingdomMult * supplyMult;

        // Configurable Price Clamping (Default: 85% to 120% of base sell price)
        double minClamp = plugin.getConfigManager().getMarketsConfig().getDouble("clamping.min-sell-ratio", 0.85);
        double maxClamp = plugin.getConfigManager().getMarketsConfig().getDouble("clamping.max-sell-ratio", 1.20);
        double effectiveUnit = Math.max(baseUnit * minClamp, Math.min(baseUnit * maxClamp, rawUnit));
        double rawTotal = effectiveUnit * quantity;

        double taxPercent = plugin.getTaxService().getTaxPercent(player);
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
