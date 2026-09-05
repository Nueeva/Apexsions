package com.apexsions.battlepass.player;

import com.apexsions.battlepass.shop.ShopCategory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {

    private final UUID uuid;
    private int seasonId;
    private int level;
    private int xp;
    private int currency;
    private final Set<String> passes = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<String> claimedRewards = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<String, Integer> questProgress = new ConcurrentHashMap<>();
    private final Map<String, Boolean> questCompleted = new ConcurrentHashMap<>();
    private final Map<String, Integer> shopPurchases = new ConcurrentHashMap<>();
    private final Map<ShopCategory, List<String>> activeRotations = new ConcurrentHashMap<>();
    private int dailyRefreshCount;
    private int totalRefreshCount;
    private long lastRefreshTimestamp;
    private long lastDailyReset;
    private long lastWeeklyReset;
    private long lastMonthlyReset;

    public PlayerData(UUID uuid, int seasonId) {
        this.uuid = uuid;
        this.seasonId = seasonId;
        this.level = 1;
        this.xp = 0;
        this.currency = 0;
        this.passes.add("free");
        this.dailyRefreshCount = 0;
        this.totalRefreshCount = 0;
        this.lastRefreshTimestamp = 0;
        this.lastDailyReset = System.currentTimeMillis();
        this.lastWeeklyReset = System.currentTimeMillis();
        this.lastMonthlyReset = System.currentTimeMillis();
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
    }

    public void addXp(int amount) {
        this.xp += Math.max(0, amount);
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = Math.max(0, currency);
    }

    public void addCurrency(int amount) {
        this.currency += Math.max(0, amount);
    }

    public boolean removeCurrency(int amount) {
        if (this.currency >= amount) {
            this.currency -= amount;
            return true;
        }
        return false;
    }

    public Set<String> getPasses() {
        return passes;
    }

    public boolean hasPass(String passId) {
        return passes.contains(passId.toLowerCase());
    }

    public void addPass(String passId) {
        passes.add(passId.toLowerCase());
    }

    public Set<String> getClaimedRewards() {
        return claimedRewards;
    }

    public boolean isRewardClaimed(int level, String passId) {
        return claimedRewards.contains(level + "_" + passId.toLowerCase());
    }

    public void setRewardClaimed(int level, String passId) {
        claimedRewards.add(level + "_" + passId.toLowerCase());
    }

    public Map<String, Integer> getQuestProgress() {
        return questProgress;
    }

    public int getQuestProgress(String questId) {
        return questProgress.getOrDefault(questId, 0);
    }

    public void setQuestProgress(String questId, int progress) {
        questProgress.put(questId, Math.max(0, progress));
    }

    public Map<String, Boolean> getQuestCompleted() {
        return questCompleted;
    }

    public boolean isQuestCompleted(String questId) {
        return questCompleted.getOrDefault(questId, false);
    }

    public void setQuestCompleted(String questId, boolean completed) {
        questCompleted.put(questId, completed);
    }

    public Map<String, Integer> getShopPurchases() {
        return shopPurchases;
    }

    public int getShopPurchaseCount(String shopItemId) {
        return shopPurchases.getOrDefault(shopItemId, 0);
    }

    public void addShopPurchase(String shopItemId, int count) {
        shopPurchases.put(shopItemId, getShopPurchaseCount(shopItemId) + count);
    }

    public void incrementShopPurchaseCount(String shopItemId) {
        addShopPurchase(shopItemId, 1);
    }

    public int getDailyRefreshCount() {
        return dailyRefreshCount;
    }

    public void setDailyRefreshCount(int dailyRefreshCount) {
        this.dailyRefreshCount = Math.max(0, dailyRefreshCount);
    }

    public void incrementDailyRefreshCount() {
        this.dailyRefreshCount++;
        this.totalRefreshCount++;
        this.lastRefreshTimestamp = System.currentTimeMillis();
    }

    public int getTotalRefreshCount() {
        return totalRefreshCount;
    }

    public void setTotalRefreshCount(int totalRefreshCount) {
        this.totalRefreshCount = Math.max(0, totalRefreshCount);
    }

    public long getLastRefreshTimestamp() {
        return lastRefreshTimestamp;
    }

    public void setLastRefreshTimestamp(long lastRefreshTimestamp) {
        this.lastRefreshTimestamp = lastRefreshTimestamp;
    }

    public Map<ShopCategory, List<String>> getActiveRotations() {
        return activeRotations;
    }

    public List<String> getRotation(ShopCategory category) {
        return activeRotations.get(category);
    }

    public void setRotation(ShopCategory category, List<String> itemIds) {
        if (itemIds != null) {
            activeRotations.put(category, new ArrayList<>(itemIds));
        } else {
            activeRotations.remove(category);
        }
    }

    public long getLastDailyReset() {
        return lastDailyReset;
    }

    public void setLastDailyReset(long lastDailyReset) {
        this.lastDailyReset = lastDailyReset;
    }

    public long getLastWeeklyReset() {
        return lastWeeklyReset;
    }

    public void setLastWeeklyReset(long lastWeeklyReset) {
        this.lastWeeklyReset = lastWeeklyReset;
    }

    public long getLastMonthlyReset() {
        return lastMonthlyReset;
    }

    public void setLastMonthlyReset(long lastMonthlyReset) {
        this.lastMonthlyReset = lastMonthlyReset;
    }

    public void resetProgressForNewSeason(int newSeasonId) {
        this.seasonId = newSeasonId;
        this.level = 1;
        this.xp = 0;
        this.claimedRewards.clear();
        this.questProgress.clear();
        this.questCompleted.clear();
        this.shopPurchases.clear();
        this.activeRotations.clear();
        this.dailyRefreshCount = 0;
    }
}

