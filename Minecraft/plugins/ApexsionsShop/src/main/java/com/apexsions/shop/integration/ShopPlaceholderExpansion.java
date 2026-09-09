package com.apexsions.shop.integration;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopCategory;
import com.apexsions.shop.dynamic.WeatherPriceService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShopPlaceholderExpansion extends PlaceholderExpansion {

    private final ApexsionsShop plugin;

    public ShopPlaceholderExpansion(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "apexsionsshop";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Nueeva";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        Player player = (offlinePlayer != null && offlinePlayer.isOnline()) ? offlinePlayer.getPlayer() : null;

        switch (params.toLowerCase()) {
            case "tax_rate", "tax_percent" -> {
                double tax = (player != null && plugin.getTaxService() != null)
                        ? plugin.getTaxService().getTaxPercent(player)
                        : plugin.getConfig().getDouble("tax.default-tax-percent", 10.0);
                return String.format("%.1f%%", tax);
            }
            case "tax_raw" -> {
                double tax = (player != null && plugin.getTaxService() != null)
                        ? plugin.getTaxService().getTaxPercent(player)
                        : plugin.getConfig().getDouble("tax.default-tax-percent", 10.0);
                return String.valueOf(tax);
            }
            case "total_items" -> {
                return String.valueOf(plugin.getItemRegistry().getAllItems().size());
            }
            case "total_categories" -> {
                return String.valueOf(ShopCategory.values().length);
            }
            case "weather" -> {
                World world = player != null ? player.getWorld() : (!Bukkit.getWorlds().isEmpty() ? Bukkit.getWorlds().get(0) : null);
                if (plugin.getWeatherPriceService() != null && world != null) {
                    return plugin.getWeatherPriceService().getCurrentWeather(world).name();
                }
                return "CLEAR";
            }
            case "weather_display" -> {
                World world = player != null ? player.getWorld() : (!Bukkit.getWorlds().isEmpty() ? Bukkit.getWorlds().get(0) : null);
                if (plugin.getWeatherPriceService() != null && world != null) {
                    WeatherPriceService.WeatherType wt = plugin.getWeatherPriceService().getCurrentWeather(world);
                    return switch (wt) {
                        case CLEAR -> "☀ Cerah";
                        case RAIN -> "🌧 Hujan";
                        case THUNDER -> "⚡ Badai Petir";
                    };
                }
                return "☀ Cerah";
            }
            default -> {
                return null;
            }
        }
    }
}
