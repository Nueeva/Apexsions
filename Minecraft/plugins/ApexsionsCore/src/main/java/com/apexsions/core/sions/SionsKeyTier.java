package com.apexsions.core.sions;

/**
 * Key tiers for ancient chests within Kerajaan Sions.
 */
public enum SionsKeyTier {
    COMMON("common", "Biasa (Common)", "<gradient:#bdc3c7:#7f8c8d><bold>Kunci Kuno Sions</bold></gradient>"),
    ELITE("elite", "Elit (Elite)", "<gradient:#f1c40f:#d35400><bold>Kunci Khazanah Ksatria Sions</bold></gradient>"),
    BOSS("boss", "Void Kaisar (Raid Boss)", "<gradient:#9b59b6:#e74c3c><bold>Kunci Void Kaisar Valerius</bold></gradient>");

    private final String id;
    private final String displayName;
    private final String defaultFormattedName;

    SionsKeyTier(String id, String displayName, String defaultFormattedName) {
        this.id = id;
        this.displayName = displayName;
        this.defaultFormattedName = defaultFormattedName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultFormattedName() {
        return defaultFormattedName;
    }

    public static SionsKeyTier fromString(String str) {
        if (str == null) return COMMON;
        for (SionsKeyTier tier : values()) {
            if (tier.id.equalsIgnoreCase(str) || tier.name().equalsIgnoreCase(str)) {
                return tier;
            }
        }
        return COMMON;
    }
}
