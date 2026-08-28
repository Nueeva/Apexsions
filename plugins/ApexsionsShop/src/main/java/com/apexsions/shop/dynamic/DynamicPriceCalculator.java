package com.apexsions.shop.dynamic;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopItem;
import org.bukkit.entity.Player;

/**
 * Calculates dynamic buy and sell prices for commodities based on:
 * - Base prices
 * - Dynamic Weather multipliers (Rain, Thunderstorm)
 * - Kingdom Territory Biome Specialty discounts/bonuses
 * - Real-time Supply & Demand Saturation
 * - Strict Price Clamping (Min 50% floor to Max 200% ceiling)
 * - Territory Taxation (10% routed to Kingdom Treasury)
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
        double supplyMult = 1.00;

        double rawUnit = baseUnit * weatherMult * kingdomMult * supplyMult;

        // Price Clamping: Min 50% to Max 200% of base buy price
        double effectiveUnit = Math.max(baseUnit * 0.50, Math.min(baseUnit * 2.00, rawUnit));
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

        // Price Clamping: Min 50% to Max 200% of base sell price
        double effectiveUnit = Math.max(baseUnit * 0.50, Math.min(baseUnit * 2.00, rawUnit));
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
