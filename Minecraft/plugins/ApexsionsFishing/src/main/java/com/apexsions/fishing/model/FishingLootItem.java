package com.apexsions.fishing.model;

import org.bukkit.Material;

public class FishingLootItem {

    private final String id;
    private final Material material;
    private final String displayName;
    private final FishRarity rarity;
    private final CatchType catchType;
    private final double chanceWeight;
    private final double minWeight;
    private final double maxWeight;
    private final double basePrice;
    private final String description;

    public FishingLootItem(String id, Material material, String displayName, FishRarity rarity, CatchType catchType,
                           double chanceWeight, double minWeight, double maxWeight, double basePrice, String description) {
        this.id = id;
        this.material = material != null ? material : Material.COD;
        this.displayName = displayName != null ? displayName : "<white>Hasil Pancingan</white>";
        this.rarity = rarity != null ? rarity : FishRarity.COMMON;
        this.catchType = catchType != null ? catchType : CatchType.FISH;
        this.chanceWeight = chanceWeight > 0 ? chanceWeight : 1.0;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.basePrice = basePrice;
        this.description = description != null ? description : "";
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public FishRarity getRarity() {
        return rarity;
    }

    public CatchType getCatchType() {
        return catchType;
    }

    public double getChanceWeight() {
        return chanceWeight;
    }

    public double getMinWeight() {
        return minWeight;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFish() {
        return catchType == CatchType.FISH;
    }
}
