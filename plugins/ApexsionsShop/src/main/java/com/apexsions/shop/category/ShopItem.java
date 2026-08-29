package com.apexsions.shop.category;

import org.bukkit.Material;

public class ShopItem {

    private final String id;
    private final Material material;
    private final ShopCategory category;
    private final double baseBuyPrice;
    private final double baseSellPrice;
    private final boolean buyEnabled;
    private final String displayName;

    public ShopItem(String id, Material material, ShopCategory category, double baseBuyPrice, String displayName) {
        this(id, material, category, baseBuyPrice, baseBuyPrice * 0.20, true, displayName);
    }

    public ShopItem(String id, Material material, ShopCategory category, double baseBuyPrice, double baseSellPrice, boolean buyEnabled, String displayName) {
        this.id = id;
        this.material = material;
        this.category = category;
        this.baseBuyPrice = Math.max(1.0, baseBuyPrice);
        this.baseSellPrice = baseSellPrice > 0 ? baseSellPrice : Math.max(0.2, this.baseBuyPrice * 0.20);
        this.buyEnabled = buyEnabled;
        this.displayName = displayName != null ? displayName : "<white>" + material.name() + "</white>";
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public ShopCategory getCategory() {
        return category;
    }

    public double getBaseBuyPrice() {
        return baseBuyPrice;
    }

    public double getBaseSellPrice() {
        return baseSellPrice;
    }

    public boolean isBuyEnabled() {
        return buyEnabled;
    }

    public String getDisplayName() {
        return displayName;
    }
}
