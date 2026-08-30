package com.apexsions.shop.api;

import com.apexsions.shop.category.ShopCategory;
import com.apexsions.shop.category.ShopItem;
import com.apexsions.shop.dynamic.DynamicPriceCalculator.PriceResult;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Graceful No-Op Fallback implementation of ApexsionsShopAPI.
 */
public class NoOpApexsionsShopAPI implements ApexsionsShopAPI {

    public static final NoOpApexsionsShopAPI INSTANCE = new NoOpApexsionsShopAPI();

    private NoOpApexsionsShopAPI() {}

    @Override
    public @Nullable ShopItem getItem(@NotNull Material material) {
        return null;
    }

    @Override
    public @Nullable ShopItem getItem(@NotNull String id) {
        return null;
    }

    @Override
    public @NotNull List<ShopItem> getItemsByCategory(@NotNull ShopCategory category) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<ShopItem> getAllItems() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull PriceResult calculateBuyPrice(@NotNull ShopItem item, @NotNull Player player, int quantity) {
        return new PriceResult(0.0, 1.0, 1.0, 1.0, 0.0, quantity, 0.0, 0.0, 0.0, 0.0);
    }

    @Override
    public @NotNull PriceResult calculateSellPrice(@NotNull ShopItem item, @NotNull Player player, int quantity) {
        return new PriceResult(0.0, 1.0, 1.0, 1.0, 0.0, quantity, 0.0, 0.0, 0.0, 0.0);
    }

    @Override
    public double getPlayerKingdomTaxPercent(@NotNull Player player) {
        return 0.0;
    }

    @Override
    public void openShop(@NotNull Player player) {
        // No-Op
    }

    @Override
    public void openCategory(@NotNull Player player, @NotNull ShopCategory category) {
        // No-Op
    }

    @Override
    public void openSellGui(@NotNull Player player) {
        // No-Op
    }
}
