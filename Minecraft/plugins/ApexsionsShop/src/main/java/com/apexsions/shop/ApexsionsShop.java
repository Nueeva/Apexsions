package com.apexsions.shop;

import com.apexsions.shop.api.ApexsionsShopAPI;
import com.apexsions.shop.category.ShopCategory;
import com.apexsions.shop.category.ShopItem;
import com.apexsions.shop.category.ShopItemRegistry;
import com.apexsions.shop.command.SellCommand;
import com.apexsions.shop.command.ShopAdminCommand;
import com.apexsions.shop.command.ShopCommand;
import com.apexsions.shop.dynamic.*;
import com.apexsions.shop.gui.CategoryShopMenu;
import com.apexsions.shop.gui.SellGuiMenu;
import com.apexsions.shop.gui.ShopMainMenu;
import com.apexsions.shop.gui.core.ShopGuiListener;
import com.apexsions.shop.integration.EconomyHook;
import com.apexsions.shop.integration.KingdomCoreHook;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ApexsionsShop extends JavaPlugin implements ApexsionsShopAPI {

    private static ApexsionsShop instance;

    private com.apexsions.shop.config.ConfigManager configManager;
    private ShopItemRegistry itemRegistry;
    private EconomyHook economyHook;
    private KingdomCoreHook kingdomCoreHook;
    private WeatherPriceService weatherPriceService;
    private KingdomMarketService kingdomMarketService;
    private SupplyScannerService supplyScannerService;
    private TaxService taxService;
    private DynamicPriceCalculator dynamicPriceCalculator;
    private MarketBroadcastService marketBroadcastService;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Initialize Configuration Manager
        this.configManager = new com.apexsions.shop.config.ConfigManager(this);
        this.configManager.load();

        // 2. Initialize Hooks & Services
        this.economyHook = new EconomyHook(this);
        this.economyHook.initialize();

        this.kingdomCoreHook = new KingdomCoreHook(this);
        this.kingdomCoreHook.initialize();

        this.itemRegistry = new ShopItemRegistry(this);
        this.itemRegistry.load();

        this.weatherPriceService = new WeatherPriceService(this);
        this.kingdomMarketService = new KingdomMarketService(this);
        this.supplyScannerService = new SupplyScannerService(this);
        this.supplyScannerService.start();
        this.taxService = new TaxService(this);
        this.dynamicPriceCalculator = new DynamicPriceCalculator(this);
        this.marketBroadcastService = new MarketBroadcastService(this);
        this.marketBroadcastService.start();

        // 3. Register SPI API Provider
        com.apexsions.shop.api.ApexsionsShopProvider.register(this);

        // 2. Register GUI Listeners
        getServer().getPluginManager().registerEvents(new ShopGuiListener(), this);

        // 3. Register Commands
        ShopCommand shopCmd = new ShopCommand(this);
        PluginCommand shop = getCommand("shop");
        if (shop != null) {
            shop.setExecutor(shopCmd);
            shop.setTabCompleter(shopCmd);
        }

        SellCommand sellCmd = new SellCommand(this);
        PluginCommand sell = getCommand("sell");
        if (sell != null) {
            sell.setExecutor(sellCmd);
            sell.setTabCompleter(sellCmd);
        }

        ShopAdminCommand shopAdminCmd = new ShopAdminCommand(this);
        PluginCommand shopAdmin = getCommand("shopadmin");
        if (shopAdmin != null) {
            shopAdmin.setExecutor(shopAdminCmd);
            shopAdmin.setTabCompleter(shopAdminCmd);
        }

        getLogger().info("==========================================");
        getLogger().info(" ApexsionsShop v" + getDescription().getVersion() + " has been enabled!");
        getLogger().info(" Dynamic Kingdom Economy & Market Ready");
        getLogger().info("==========================================");
    }

    @Override
    public void onDisable() {
        if (marketBroadcastService != null) {
            marketBroadcastService.stop();
        }
        if (supplyScannerService != null) {
            supplyScannerService.stop();
        }
        com.apexsions.shop.api.ApexsionsShopProvider.unregister();
        getLogger().info("ApexsionsShop has been disabled.");
    }

    public void reloadPluginConfig() {
        if (configManager != null) {
            configManager.load();
        }
        if (itemRegistry != null) {
            itemRegistry.load();
        }
    }

    public static ApexsionsShop getInstance() {
        return instance;
    }

    public MarketBroadcastService getMarketBroadcastService() {
        return marketBroadcastService;
    }

    public com.apexsions.shop.config.ConfigManager getConfigManager() {
        return configManager;
    }

    public ShopItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public EconomyHook getEconomyHook() {
        return economyHook;
    }

    public KingdomCoreHook getKingdomCoreHook() {
        return kingdomCoreHook;
    }

    public WeatherPriceService getWeatherPriceService() {
        return weatherPriceService;
    }

    public KingdomMarketService getKingdomMarketService() {
        return kingdomMarketService;
    }

    public SupplyScannerService getSupplyScannerService() {
        return supplyScannerService;
    }

    public TaxService getTaxService() {
        return taxService;
    }

    public DynamicPriceCalculator getDynamicPriceCalculator() {
        return dynamicPriceCalculator;
    }

    // --- API Implementation ---

    @Override
    public @Nullable ShopItem getItem(@NotNull Material material) {
        return itemRegistry.getItem(material);
    }

    @Override
    public @Nullable ShopItem getItem(@NotNull String id) {
        return itemRegistry.getItem(id);
    }

    @Override
    public @NotNull List<ShopItem> getItemsByCategory(@NotNull ShopCategory category) {
        return itemRegistry.getItemsByCategory(category);
    }

    @Override
    public @NotNull Collection<ShopItem> getAllItems() {
        return itemRegistry.getAllItems();
    }

    @Override
    public @NotNull DynamicPriceCalculator.PriceResult calculateBuyPrice(@NotNull ShopItem item, @NotNull Player player, int quantity) {
        return dynamicPriceCalculator.calculateBuyPrice(item, player, quantity);
    }

    @Override
    public @NotNull DynamicPriceCalculator.PriceResult calculateSellPrice(@NotNull ShopItem item, @NotNull Player player, int quantity) {
        return dynamicPriceCalculator.calculateSellPrice(item, player, quantity);
    }

    @Override
    public double getPlayerKingdomTaxPercent(@NotNull Player player) {
        return taxService.getTaxPercent(player);
    }

    @Override
    public void openShop(@NotNull Player player) {
        new ShopMainMenu(this, player).open();
    }

    @Override
    public void openCategory(@NotNull Player player, @NotNull ShopCategory category) {
        new CategoryShopMenu(this, player, category, null, 1).open();
    }

    @Override
    public void openSellGui(@NotNull Player player) {
        new SellGuiMenu(this, player).open();
    }
}
