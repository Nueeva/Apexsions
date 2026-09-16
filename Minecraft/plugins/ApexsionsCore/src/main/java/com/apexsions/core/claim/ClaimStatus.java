package com.apexsions.core.claim;

/**
 * Status of a land claim regarding tax upkeep and grace periods.
 */
public enum ClaimStatus {
    ACTIVE("Aktif", "<green>AKTIF</green>"),
    GRACE_PERIOD("Masa Tenggang", "<gradient:#ff416c:#ff4b2b><b>MENUNGGAK PAJAK</b></gradient>"),
    EXPIRED("Kedaluwarsa", "<dark_red>KEDALUWARSA</dark_red>");

    private final String displayName;
    private final String badge;

    ClaimStatus(String displayName, String badge) {
        this.displayName = displayName;
        this.badge = badge;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadge() {
        return badge;
    }

    public static ClaimStatus fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) return ACTIVE;
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVE;
        }
    }
}
