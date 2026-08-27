package com.apex.battlepass.shop.refresh;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.player.PlayerData;
import com.apex.battlepass.shop.ItemRarity;
import com.apex.battlepass.shop.ShopCategory;
import com.apex.battlepass.shop.ShopItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class ShopRefreshService {

    public enum RefreshResult {
        SUCCESS,
        INSUFFICIENT_FUNDS,
        ON_COOLDOWN,
        SEASON_INACTIVE,
        FAILED
    }

    private final ApexsionsBattlepass plugin;
    private int baseCost;
    private int minCost;
    private int maxCost;
    private double itemValueMultiplier;
    private double scalingPerRefresh;
    private int cooldownSeconds;
    private final Map<ItemRarity, Double> rarityMultipliers = new EnumMap<>(ItemRarity.class);

    public ShopRefreshService(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
        loadConfiguration();
    }

    public void loadConfiguration() {
        FileConfiguration config = plugin.getConfig();
        this.baseCost = config.getInt("shop.refresh.base-cost", 50);
        this.minCost = config.getInt("shop.refresh.min-cost", 25);
        this.maxCost = config.getInt("shop.refresh.max-cost", 500);
        this.itemValueMultiplier = config.getDouble("shop.refresh.item-value-multiplier", 1.0);
        this.scalingPerRefresh = config.getDouble("shop.refresh.scaling-per-refresh", 0.15);
        this.cooldownSeconds = config.getInt("shop.refresh.cooldown-seconds", 3);

        rarityMultipliers.clear();
        for (ItemRarity r : ItemRarity.values()) {
            double mult = config.getDouble("shop.refresh.rarity-multipliers." + r.name(), r.getDefaultMultiplier());
            rarityMultipliers.put(r, mult);
        }
    }

    public int calculateRefreshCost(Player player, ShopCategory category) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) return baseCost;

        Collection<ShopItem> currentItems = plugin.getShopManager().getDisplayItems(player, category);
        if (currentItems.isEmpty()) {
            currentItems = plugin.getShopManager().getShopItems(category).values();
        }

        // 1. Average Rarity Multiplier
        double rarityFactor = 1.0;
        if (!currentItems.isEmpty()) {
            double totalRarityMult = 0;
            for (ShopItem item : currentItems) {
                totalRarityMult += getRarityMultiplier(item.getRarity());
            }
            rarityFactor = totalRarityMult / currentItems.size();
        }

        // 2. Item Value Factor (based on coin pricing)
        double totalCoinValue = 0;
        int coinItemCount = 0;
        for (ShopItem item : currentItems) {
            if ("battle_coins".equalsIgnoreCase(item.getCurrencyType()) || "battlecoins".equalsIgnoreCase(item.getCurrencyType())) {
                totalCoinValue += item.getPrice();
                coinItemCount++;
            }
        }
        double avgCoinPrice = coinItemCount > 0 ? (totalCoinValue / coinItemCount) : 50.0;
        double valueFactor = Math.max(0.7, Math.min(2.5, (avgCoinPrice / 100.0) * itemValueMultiplier));

        // 3. Daily Scaling Factor (Anti-Abuse)
        double scalingFactor = 1.0 + (data.getDailyRefreshCount() * scalingPerRefresh);

        // 4. Compute Total
        double calculated = baseCost * rarityFactor * valueFactor * scalingFactor;
        int finalCost = (int) Math.round(calculated);

        return Math.max(minCost, Math.min(maxCost, finalCost));
    }

    public long getRemainingCooldownMillis(PlayerData data) {
        if (data == null || cooldownSeconds <= 0) return 0;
        long elapsed = System.currentTimeMillis() - data.getLastRefreshTimestamp();
        long required = cooldownSeconds * 1000L;
        return Math.max(0, required - elapsed);
    }

    public boolean isOnCooldown(PlayerData data) {
        return getRemainingCooldownMillis(data) > 0;
    }

    public synchronized RefreshResult executeRefresh(Player player, ShopCategory category) {
        if (!plugin.getSeasonManager().isActive()) {
            return RefreshResult.SEASON_INACTIVE;
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) {
            return RefreshResult.FAILED;
        }

        if (isOnCooldown(data)) {
            return RefreshResult.ON_COOLDOWN;
        }

        int cost = calculateRefreshCost(player, category);
        if (data.getCurrency() < cost) {
            return RefreshResult.INSUFFICIENT_FUNDS;
        }

        // Safe Transaction: Deduct Battle Coins
        if (!plugin.getCurrencyService().removeCurrency(player.getUniqueId(), cost)) {
            return RefreshResult.INSUFFICIENT_FUNDS;
        }

        // Generate new rotation
        List<String> newRotation = plugin.getShopManager().generateNewRotation(category);
        data.setRotation(category, newRotation);
        data.incrementDailyRefreshCount();

        // Asynchronously persist
        plugin.getRepository().savePlayerData(data);

        return RefreshResult.SUCCESS;
    }

    public void resetAllDailyRefreshCounts() {
        for (PlayerData data : plugin.getPlayerManager().getPlayerDataCache().values()) {
            data.setDailyRefreshCount(0);
        }
        // Background DB sync
        plugin.getPlayerManager().saveAllPlayerData();
    }

    public void resetPlayerDailyRefreshCount(PlayerData data) {
        if (data != null) {
            data.setDailyRefreshCount(0);
            plugin.getRepository().savePlayerData(data);
        }
    }

    // Getters & Setters for Admin Live Configuration
    public int getBaseCost() { return baseCost; }
    public void setBaseCost(int baseCost) { this.baseCost = Math.max(1, baseCost); }

    public int getMinCost() { return minCost; }
    public void setMinCost(int minCost) { this.minCost = Math.max(1, minCost); }

    public int getMaxCost() { return maxCost; }
    public void setMaxCost(int maxCost) { this.maxCost = Math.max(minCost, maxCost); }

    public double getScalingPerRefresh() { return scalingPerRefresh; }
    public void setScalingPerRefresh(double scalingPerRefresh) { this.scalingPerRefresh = Math.max(0, scalingPerRefresh); }

    public int getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(int cooldownSeconds) { this.cooldownSeconds = Math.max(0, cooldownSeconds); }

    public double getRarityMultiplier(ItemRarity rarity) {
        return rarityMultipliers.getOrDefault(rarity, rarity.getDefaultMultiplier());
    }

    public void setRarityMultiplier(ItemRarity rarity, double multiplier) {
        rarityMultipliers.put(rarity, Math.max(0.1, multiplier));
    }

    public Map<ItemRarity, Double> getRarityMultipliers() {
        return rarityMultipliers;
    }
}
