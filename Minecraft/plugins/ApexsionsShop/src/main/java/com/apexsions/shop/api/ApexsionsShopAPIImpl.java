package com.apexsions.shop.api;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopCategory;
import com.apexsions.shop.category.ShopItem;
import com.apexsions.shop.dynamic.DynamicPriceCalculator.PriceResult;
import com.apexsions.shop.gui.CategoryShopMenu;
import com.apexsions.shop.gui.SellGuiMenu;
import com.apexsions.shop.gui.ShopMainMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ApexsionsShopAPIImpl implements ApexsionsShopAPI {

    private final ApexsionsShop plugin;

    public ApexsionsShopAPIImpl(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable ShopItem getItem(@NotNull Material material) {
        return plugin.getItemRegistry().getItem(material);
    }

    @Override
    public @Nullable ShopItem getItem(@NotNull String id) {
        return plugin.getItemRegistry().getItem(id);
    }

    @Override
    public @NotNull List<ShopItem> getItemsByCategory(@NotNull ShopCategory category) {
        return plugin.getItemRegistry().getItemsByCategory(category);
    }

    @Override
    public @NotNull Collection<ShopItem> getAllItems() {
        return plugin.getItemRegistry().getAllItems();
    }

    @Override
    public @NotNull PriceResult calculateBuyPrice(@NotNull ShopItem item, @NotNull Player player, int quantity) {
        return plugin.getDynamicPriceCalculator().calculateBuyPrice(item, player, quantity);
    }

    @Override
    public @NotNull PriceResult calculateSellPrice(@NotNull ShopItem item, @NotNull Player player, int quantity) {
        return plugin.getDynamicPriceCalculator().calculateSellPrice(item, player, quantity);
    }

    @Override
    public double getPlayerKingdomTaxPercent(@NotNull Player player) {
        return plugin.getTaxService().getTaxPercent(player);
    }

    @Override
    public void openShop(@NotNull Player player) {
        new ShopMainMenu(plugin, player).open();
    }

    @Override
    public void openCategory(@NotNull Player player, @NotNull ShopCategory category) {
        new CategoryShopMenu(plugin, player, category, null, 1).open();
    }

    @Override
    public void openSellGui(@NotNull Player player) {
        new SellGuiMenu(plugin, player, null).open();
    }
}
