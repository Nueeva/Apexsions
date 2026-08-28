package com.apexsions.shop.api;

import com.apexsions.shop.category.ShopCategory;
import com.apexsions.shop.category.ShopItem;
import com.apexsions.shop.dynamic.DynamicPriceCalculator.PriceResult;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface ApexsionsShopAPI {

    @Nullable
    ShopItem getItem(@NotNull Material material);

    @Nullable
    ShopItem getItem(@NotNull String id);

    @NotNull
    List<ShopItem> getItemsByCategory(@NotNull ShopCategory category);

    @NotNull
    Collection<ShopItem> getAllItems();

    @NotNull
    PriceResult calculateBuyPrice(@NotNull ShopItem item, @NotNull Player player, int quantity);

    @NotNull
    PriceResult calculateSellPrice(@NotNull ShopItem item, @NotNull Player player, int quantity);

    double getPlayerKingdomTaxPercent(@NotNull Player player);

    void openShop(@NotNull Player player);

    void openCategory(@NotNull Player player, @NotNull ShopCategory category);

    void openSellGui(@NotNull Player player);
}
