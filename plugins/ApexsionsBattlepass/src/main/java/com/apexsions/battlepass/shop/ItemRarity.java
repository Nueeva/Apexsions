package com.apexsions.battlepass.shop;

public enum ItemRarity {
    COMMON("&fCommon", "&f", 1.0),
    UNCOMMON("&aUncommon", "&a", 1.2),
    RARE("&9Rare", "&9", 1.5),
    EPIC("&5Epic", "&5", 2.0),
    LEGENDARY("&6Legendary", "&6", 3.0),
    MYTHIC("&dMythic", "&d", 5.0);

    private final String displayName;
    private final String color;
    private final double defaultMultiplier;

    ItemRarity(String displayName, String color, double defaultMultiplier) {
        this.displayName = displayName;
        this.color = color;
        this.defaultMultiplier = defaultMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public double getDefaultMultiplier() {
        return defaultMultiplier;
    }

    public static ItemRarity fromString(String str) {
        if (str == null) return COMMON;
        try {
            return ItemRarity.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return COMMON;
        }
    }
}
