package com.apexsions.shop.dynamic;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopCategory;
import com.apexsions.shop.category.ShopItem;
import org.bukkit.World;

public class WeatherPriceService {

    private final ApexsionsShop plugin;

    public WeatherPriceService(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    public enum WeatherType {
        CLEAR,
        RAIN,
        THUNDER
    }

    public WeatherType getCurrentWeather(World world) {
        if (world == null) return WeatherType.CLEAR;
        if (world.isThundering()) return WeatherType.THUNDER;
        if (world.hasStorm()) return WeatherType.RAIN;
        return WeatherType.CLEAR;
    }

    public double getBuyMultiplier(ShopItem item, World world) {
        // Buy prices stay mostly steady or slight demand shifts
        WeatherType weather = getCurrentWeather(world);
        if (weather == WeatherType.CLEAR && item.getCategory() == ShopCategory.FARMING) {
            return 1.10; // Kemarau: harga beli bibit/tanaman sedikit naik
        }
        return 1.00;
    }

    public double getSellMultiplier(ShopItem item, World world) {
        WeatherType weather = getCurrentWeather(world);
        ShopCategory category = item.getCategory();

        switch (weather) {
            case CLEAR -> {
                if (category == ShopCategory.FARMING) {
                    return plugin.getConfigManager().getMarketsConfig().getDouble("weather.clear.farming-sell-multiplier", 1.10);
                }
                return plugin.getConfigManager().getMarketsConfig().getDouble("weather.clear.mob-sell-multiplier", 1.00);
            }
            case RAIN -> {
                if (category == ShopCategory.FARMING) {
                    return plugin.getConfigManager().getMarketsConfig().getDouble("weather.rain.farming-sell-multiplier", 0.98);
                }
                if (category == ShopCategory.MOB_DROPS) {
                    return plugin.getConfigManager().getMarketsConfig().getDouble("weather.rain.mob-sell-multiplier", 1.05);
                }
                return 1.00;
            }
            case THUNDER -> {
                if (category == ShopCategory.FARMING) {
                    return plugin.getConfigManager().getMarketsConfig().getDouble("weather.thunder.farming-sell-multiplier", 0.95);
                }
                if (category == ShopCategory.MOB_DROPS) {
                    return plugin.getConfigManager().getMarketsConfig().getDouble("weather.thunder.mob-sell-multiplier", 1.15);
                }
                return 1.00;
            }
            default -> {
                return 1.00;
            }
        }
    }

    public String getWeatherDescription(World world) {
        WeatherType weather = getCurrentWeather(world);
        return switch (weather) {
            case CLEAR -> "<yellow>☀ Cerah / Kemarau (Bonus Panen +10%)</yellow>";
            case RAIN -> "<aqua>🌧 Hujan Subur</aqua>";
            case THUNDER -> "<dark_purple>⚡ Badai Petir (Bonus Drop Monster +15%)</dark_purple>";
        };
    }
}
