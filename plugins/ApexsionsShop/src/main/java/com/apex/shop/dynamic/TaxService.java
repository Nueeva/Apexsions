package com.apex.shop.dynamic;

import com.apex.shop.ApexsionsShop;
import org.bukkit.entity.Player;

public class TaxService {

    private final ApexsionsShop plugin;

    public TaxService(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    public double getTaxPercent(Player player) {
        double defaultTax = plugin.getConfig().getDouble("tax.default-tax-percent", 10.0);

        if (plugin.getConfig().getBoolean("tax.use-core-kingdom-tax", true) && plugin.getKingdomCoreHook().isCoreAvailable()) {
            String kingdom = plugin.getKingdomCoreHook().getPlayerKingdom(player);
            // Default kingdom tax rate or custom override
            if (kingdom != null && !kingdom.equalsIgnoreCase("NONE")) {
                // If ApexsionsCore config has custom region tax, we could fetch it, else fallback to defaultTax
                return defaultTax;
            }
        }
        return defaultTax;
    }

    public double calculateTaxAmount(double rawPrice, Player player) {
        double percent = getTaxPercent(player);
        return rawPrice * (percent / 100.0);
    }
}
