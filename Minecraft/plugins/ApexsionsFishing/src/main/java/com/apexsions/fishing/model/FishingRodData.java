package com.apexsions.fishing.model;

import java.util.ArrayList;
import java.util.List;

public class FishingRodData {

    private final String id;
    private String displayName;
    private boolean autoCatch;
    private double luckBonus;
    private double weightBonus;
    private int catchSpeedSeconds;
    private int minLevel;
    private double priceRupiah;
    private double priceDiamond;
    private boolean unbreakable;
    private int customModelData;
    private List<String> description;

    public FishingRodData(String id, String displayName, boolean autoCatch, double luckBonus, double weightBonus,
                          int catchSpeedSeconds, int minLevel, double priceRupiah, double priceDiamond,
                          boolean unbreakable, int customModelData, List<String> description) {
        this.id = id;
        this.displayName = displayName;
        this.autoCatch = autoCatch;
        this.luckBonus = luckBonus;
        this.weightBonus = weightBonus;
        this.catchSpeedSeconds = catchSpeedSeconds > 0 ? catchSpeedSeconds : 20;
        this.minLevel = Math.max(0, minLevel);
        this.priceRupiah = priceRupiah;
        this.priceDiamond = priceDiamond;
        this.unbreakable = unbreakable;
        this.customModelData = customModelData;
        this.description = description != null ? new ArrayList<>(description) : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isAutoCatch() {
        return autoCatch;
    }

    public void setAutoCatch(boolean autoCatch) {
        this.autoCatch = autoCatch;
    }

    public double getLuckBonus() {
        return luckBonus;
    }

    public void setLuckBonus(double luckBonus) {
        this.luckBonus = luckBonus;
    }

    public double getWeightBonus() {
        return weightBonus;
    }

    public void setWeightBonus(double weightBonus) {
        this.weightBonus = weightBonus;
    }

    public int getCatchSpeedSeconds() {
        return catchSpeedSeconds;
    }

    public void setCatchSpeedSeconds(int catchSpeedSeconds) {
        this.catchSpeedSeconds = catchSpeedSeconds;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public double getPriceRupiah() {
        return priceRupiah;
    }

    public void setPriceRupiah(double priceRupiah) {
        this.priceRupiah = priceRupiah;
    }

    public double getPriceDiamond() {
        return priceDiamond;
    }

    public void setPriceDiamond(double priceDiamond) {
        this.priceDiamond = priceDiamond;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }
}
