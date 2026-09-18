package com.apexsions.shop.sync;

import com.apexsions.shop.ApexsionsShop;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Daemon & Service to fetch live commodity prices, taxes, and kingdom market
 * modifiers from the Azuriom WebBridge API endpoint (/shop/config).
 */
public class WebMarketSyncService {

    private final ApexsionsShop plugin;
    private final HttpClient httpClient;
    private String apiUrl;
    private String apiKey;
    private boolean enabled;

    public WebMarketSyncService(ApexsionsShop plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
        reloadSettings();
    }

    public void reloadSettings() {
        this.enabled = plugin.getConfig().getBoolean("web-bridge.enabled", true);
        this.apiUrl = plugin.getConfig().getString("web-bridge.api-url", "http://web.apexsions.my.id/api/apexsions-bridge");
        this.apiKey = plugin.getConfig().getString("web-bridge.api-key", "apexsions_bridge_key_live_2026");
    }

    /**
     * Pull the latest market configuration and item prices asynchronously.
     *
     * @return CompletableFuture completing with true on success, false otherwise.
     */
    public CompletableFuture<Boolean> syncAsync() {
        if (!enabled) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String targetUrl = apiUrl.replaceAll("/+$", "") + "/shop/config";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl))
                        .timeout(Duration.ofSeconds(10))
                        .header("Accept", "application/json")
                        .header("X-Apexsions-Key", apiKey)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    plugin.getLogger().log(Level.FINE, "[WebMarketSync] Server returned HTTP " + response.statusCode());
                    return false;
                }

                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                if (!json.has("status") || !json.get("status").getAsString().equals("success")) {
                    return false;
                }

                // 1. Update kingdom multipliers, weather, and clamping in markets.yml
                if (json.has("kingdoms") || json.has("supply_market")) {
                    updateMarketsConfig(json);
                }

                // 2. Update category items on disk
                if (json.has("items") && json.get("items").isJsonObject()) {
                    updateCategoriesConfig(json.getAsJsonObject("items"));
                }

                // 3. Trigger reload on Bukkit Main Thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.reloadPluginConfig();
                    plugin.getLogger().info("[WebMarketSync] Sukses menyinkronkan konfigurasi pasar & katalog harga komoditas dari WebBridge!");
                });

                return true;
            } catch (Exception e) {
                plugin.getLogger().log(Level.FINE, "[WebMarketSync] Gagal mem-fetch data dari WebBridge API: " + e.getMessage());
                return false;
            }
        });
    }

    private void updateMarketsConfig(JsonObject json) {
        try {
            File marketsFile = new File(plugin.getDataFolder(), "markets/markets.yml");
            if (!marketsFile.exists()) {
                marketsFile = new File(plugin.getDataFolder(), "markets.yml");
            }
            if (!marketsFile.exists()) return;

            YamlConfiguration config = YamlConfiguration.loadConfiguration(marketsFile);

            if (json.has("kingdoms") && json.get("kingdoms").isJsonObject()) {
                JsonObject kingdoms = json.getAsJsonObject("kingdoms");
                for (Map.Entry<String, JsonElement> entry : kingdoms.entrySet()) {
                    String kName = entry.getKey().toUpperCase();
                    if (!entry.getValue().isJsonObject()) continue;
                    JsonObject kObj = entry.getValue().getAsJsonObject();
                    for (Map.Entry<String, JsonElement> prop : kObj.entrySet()) {
                        String pName = prop.getKey().replace('_', '-');
                        if (prop.getValue().isJsonPrimitive()) {
                            if (prop.getValue().getAsJsonPrimitive().isBoolean()) {
                                config.set("kingdoms." + kName + "." + pName, prop.getValue().getAsBoolean());
                            } else if (prop.getValue().getAsJsonPrimitive().isNumber()) {
                                config.set("kingdoms." + kName + "." + pName, prop.getValue().getAsDouble());
                            } else {
                                config.set("kingdoms." + kName + "." + pName, prop.getValue().getAsString());
                            }
                        }
                    }
                }
            }

            if (json.has("supply_market") && json.get("supply_market").isJsonObject()) {
                JsonObject sm = json.getAsJsonObject("supply_market");
                for (Map.Entry<String, JsonElement> prop : sm.entrySet()) {
                    String pName = prop.getKey().replace('_', '-');
                    if (prop.getValue().isJsonPrimitive()) {
                        if (prop.getValue().getAsJsonPrimitive().isBoolean()) {
                            config.set("supply-market." + pName, prop.getValue().getAsBoolean());
                        } else if (prop.getValue().getAsJsonPrimitive().isNumber()) {
                            config.set("supply-market." + pName, prop.getValue().getAsDouble());
                        }
                    }
                }
            }

            if (json.has("clamping") && json.get("clamping").isJsonObject()) {
                JsonObject cl = json.getAsJsonObject("clamping");
                for (Map.Entry<String, JsonElement> prop : cl.entrySet()) {
                    String pName = prop.getKey().replace('_', '-');
                    if (prop.getValue().isJsonPrimitive() && prop.getValue().getAsJsonPrimitive().isNumber()) {
                        config.set("clamping." + pName, prop.getValue().getAsDouble());
                    }
                }
            }

            config.save(marketsFile);
        } catch (Exception ex) {
            plugin.getLogger().warning("[WebMarketSync] Gagal menyimpan pembaruan ke markets.yml: " + ex.getMessage());
        }
    }

    private void updateCategoriesConfig(JsonObject items) {
        try {
            File catDir = new File(plugin.getDataFolder(), "categories");
            if (!catDir.exists()) return;

            Map<String, YamlConfiguration> configs = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : items.entrySet()) {
                String itemId = entry.getKey();
                if (!entry.getValue().isJsonObject()) continue;
                JsonObject itemObj = entry.getValue().getAsJsonObject();
                String category = itemObj.has("category") ? itemObj.get("category").getAsString() : "ores";

                YamlConfiguration catConfig = configs.computeIfAbsent(category, cat -> {
                    File f = new File(catDir, cat + ".yml");
                    return f.exists() ? YamlConfiguration.loadConfiguration(f) : null;
                });

                if (catConfig == null) continue;

                if (catConfig.contains("items." + itemId)) {
                    if (itemObj.has("buy_price")) {
                        catConfig.set("items." + itemId + ".buy-price", itemObj.get("buy_price").getAsDouble());
                    }
                    if (itemObj.has("sell_price")) {
                        catConfig.set("items." + itemId + ".sell-price", itemObj.get("sell_price").getAsDouble());
                    }
                    if (itemObj.has("buy_enabled")) {
                        catConfig.set("items." + itemId + ".buy-enabled", itemObj.get("buy_enabled").getAsBoolean());
                    }
                }
            }

            for (Map.Entry<String, YamlConfiguration> entry : configs.entrySet()) {
                if (entry.getValue() != null) {
                    entry.getValue().save(new File(catDir, entry.getKey() + ".yml"));
                }
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("[WebMarketSync] Gagal menyimpan pembaruan berkas categories: " + ex.getMessage());
        }
    }
}
