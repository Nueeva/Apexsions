package com.apexsions.core.claim;

/**
 * Granular permissions and hierarchical roles for citizens within a claimed territory.
 */
public enum ClaimRole {
    OWNER(4, "Pemilik Sah", "<gradient:#ffd700:#ffa500>👑 PEMILIK</gradient>"),
    MANAGER(3, "Pengelola", "<gradient:#00c6ff:#0072ff>🛡️ PENGELOLA</gradient>"),
    BUILDER(2, "Pembangun", "<gradient:#a8ff78:#78ffd6>🔨 PEMBANGUN</gradient>"),
    VISITOR(1, "Pengunjung", "<gray>🚶 PENGUNJUNG</gray>");

    private final int level;
    private final String displayName;
    private final String badge;

    ClaimRole(int level, String displayName, String badge) {
        this.level = level;
        this.displayName = displayName;
        this.badge = badge;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadge() {
        return badge;
    }

    public boolean isAtLeast(ClaimRole other) {
        return this.level >= other.level;
    }

    public static ClaimRole fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) return VISITOR;
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return VISITOR;
        }
    }
}
