package com.apexsions.economy.auction;

import org.bukkit.Material;

public enum AuctionSortMode {
    NEWEST("Baru Didaftarkan", Material.CLOCK),
    CHEAPEST("Harga Termurah", Material.GOLD_INGOT),
    EXPENSIVE("Harga Termahal", Material.DIAMOND_BLOCK);

    private final String displayName;
    private final Material icon;

    AuctionSortMode(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public AuctionSortMode next() {
        AuctionSortMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
