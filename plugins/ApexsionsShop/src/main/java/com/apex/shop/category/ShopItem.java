package com.apex.shop.category;

import org.bukkit.Material;

public class ShopItem {

    private final String id;
    private final Material material;
    private final ShopCategory category;
    private final double baseBuyPrice;
    private final String displayName;

    public ShopItem(String id, Material material, ShopCategory category, double baseBuyPrice, String displayName) {
        this.id = id;
        this.material = material;
        this.category = category;
        this.baseBuyPrice = Math.max(1.0, baseBuyPrice);
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

    /**
     * Base Sell Price is strictly 20% of Base Buy Price.
     */
    public double getBaseSellPrice() {
        return Math.max(0.2, baseBuyPrice * 0.20);
    }

    public String getDisplayName() {
        return displayName;
    }
}
