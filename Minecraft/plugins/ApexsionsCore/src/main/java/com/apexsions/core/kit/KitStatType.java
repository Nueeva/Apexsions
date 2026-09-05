package com.apexsions.core.kit;

/**
 * Types of stat-based combat and attribute bonuses for armor sets.
 */
public enum KitStatType {
    DAMAGE_REDUCTION("Pengurangan Damage", "%", 15.0, "<blue><bold>🛡 DAMAGE REDUCTION</bold></blue>"),
    CRITICAL_DAMAGE_REDUCTION("Pengurangan Damage Critical", "%", 20.0, "<dark_aqua><bold>🛡 CRIT RESISTANCE</bold></dark_aqua>"),
    DODGE_CHANCE("Peluang Menghindar", "%", 10.0, "<green><bold>💨 DODGE CHANCE</bold></green>"),
    EXTRA_MAX_HEALTH("Tambahan Nyawa Maksimal", " HP", 6.0, "<red><bold>❤ EXTRA MAX HEALTH</bold></red>"),
    MOVEMENT_SPEED_BOOST("Bonus Kecepatan Gerak", "%", 15.0, "<aqua><bold>✦ SPEED BOOST</bold></aqua>"),

    // Deprecated legacy offensive stats (kept only for backwards-compatibility, omitted from Armor GUI)
    @Deprecated
    ATTACK_DAMAGE_BOOST("Peningkatan Serangan", "%", 20.0, "<red><bold>⚔ ATTACK BOOST</bold></red>"),
    @Deprecated
    CRITICAL_DAMAGE_BOOST("Bonus Damage Critical", "%", 25.0, "<gold><bold>⚡ CRITICAL STRIKE</bold></gold>");

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
        if (this == DAMAGE_REDUCTION || this == CRITICAL_DAMAGE_REDUCTION) {
            double abs = Math.abs(value);
            return "-" + (long) abs + unit;
        } else if (unit.equals("%")) {
            return "+" + (long) value + unit;
        } else {
            return "+" + (long) value + unit;
        }
    }
}
