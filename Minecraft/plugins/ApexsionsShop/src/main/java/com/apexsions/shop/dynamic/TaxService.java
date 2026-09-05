package com.apexsions.shop.dynamic;

import com.apexsions.shop.ApexsionsShop;
import org.bukkit.entity.Player;

public class TaxService {

    private final ApexsionsShop plugin;

    public TaxService(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    public double getTaxPercent(Player player) {
        return getTaxPercent(player, null);
    }

    public double getTaxPercent(Player player, String kingdomOverride) {
        if (kingdomOverride != null && !kingdomOverride.equalsIgnoreCase("NONE")) {
            return plugin.getKingdomCoreHook().getKingdomTax(kingdomOverride);
        }

        if (plugin.getConfig().getBoolean("tax.use-core-kingdom-tax", true)) {
            return plugin.getKingdomCoreHook().getPlayerKingdomTax(player);
        }

        return plugin.getConfig().getDouble("tax.default-tax-percent", 10.0);
    }

    public double calculateTaxAmount(double rawPrice, Player player) {
        return calculateTaxAmount(rawPrice, player, null);
    }

    public double calculateTaxAmount(double rawPrice, Player player, String kingdomOverride) {
        double percent = getTaxPercent(player, kingdomOverride);
        return rawPrice * (percent / 100.0);
    }
}
