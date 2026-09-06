package com.apexsions.crate.integration;

import com.apexsions.shop.api.ApexsionsShopAPI;
import com.apexsions.shop.api.ApexsionsShopProvider;
import org.bukkit.entity.Player;

public class ApexsionsShopIntegration {

    public static boolean isShopAvailable() {
        return ApexsionsShopProvider.isAvailable();
    }

    public static void openShop(Player player) {
        if (isShopAvailable()) {
            ApexsionsShopAPI shopAPI = ApexsionsShopProvider.get();
            shopAPI.openShop(player);
        }
    }
}
