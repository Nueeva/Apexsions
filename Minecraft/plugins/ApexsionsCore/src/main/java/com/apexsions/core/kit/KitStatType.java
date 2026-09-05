package com.apexsions.core.kit;

/**
 * Types of stat-based combat and attribute bonuses for armor sets.
 */
public enum KitStatType {
    DAMAGE_REDUCTION("Pengurangan Damage", "%", -15.0, "<gradient:#3498db:#2980b9><bold>🛡 DAMAGE REDUCTION</bold></gradient>"),
    ATTACK_DAMAGE_BOOST("Peningkatan Serangan", "%", 20.0, "<gradient:#e74c3c:#c0392b><bold>⚔ ATTACK BOOST</bold></gradient>"),
    DODGE_CHANCE("Peluang Menghindar", "%", 10.0, "<gradient:#2ecc71:#27ae60><bold>💨 DODGE CHANCE</bold></gradient>"),
    CRITICAL_DAMAGE_BOOST("Bonus Damage Critical", "%", 25.0, "<gradient:#f39c12:#d35400><bold>⚡ CRITICAL STRIKE</bold></gradient>"),
    EXTRA_MAX_HEALTH("Tambahan Nyawa Maksimal", " HP", 6.0, "<gradient:#e91e63:#9c27b0><bold>❤ EXTRA MAX HEALTH</bold></gradient>"),
    MOVEMENT_SPEED_BOOST("Bonus Kecepatan Gerak", "%", 15.0, "<gradient:#00ffff:#00bcd4><bold>⚡ SPEED BOOST</bold></gradient>");

    private final String displayName;
    private final String unit;
    private final double defaultValue;
    private final String formattedBadge;

    KitStatType(String displayName, String unit, double defaultValue, String formattedBadge) {
        this.displayName = displayName;
        this.unit = unit;
        this.defaultValue = defaultValue;
        this.formattedBadge = formattedBadge;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUnit() {
        return unit;
    }

    public double getDefaultValue() {
        return defaultValue;
    }

    public String getFormattedBadge() {
        return formattedBadge;
    }

    public String formatValue(double value) {
        if (this == DAMAGE_REDUCTION) {
            double abs = Math.abs(value);
            return "-" + (long) abs + unit;
        } else if (unit.equals("%")) {
            return "+" + (long) value + unit;
        } else {
            return "+" + (long) value + unit;
        }
    }
}
