package com.apex.shop.dynamic;

import com.apex.shop.ApexsionsShop;
import com.apex.shop.category.ShopItem;
import org.bukkit.entity.Player;

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

        double effectiveUnit = baseUnit * weatherMult * kingdomMult * supplyMult;
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

        double effectiveUnit = baseUnit * weatherMult * kingdomMult * supplyMult;
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
