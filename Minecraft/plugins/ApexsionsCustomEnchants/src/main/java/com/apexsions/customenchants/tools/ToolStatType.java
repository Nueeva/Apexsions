package com.apexsions.customenchants.tools;

import org.bukkit.Material;

/**
 * Unique attribute-based synergies for weapons and tools when wearing a matching armor set.
 * Excludes any abilities already present in Custom Enchants to ensure zero overlap.
 */
public enum ToolStatType {

    // === WEAPON ATTRIBUTES ===
    WEAPON_DAMAGE_BOOST("Peningkatan Serangan", "%", 20.0, Material.DIAMOND_SWORD, Category.WEAPON),
    ATTACK_SPEED_BOOST("Kecepatan Ayunan Senjata", "%", 25.0, Material.FEATHER, Category.WEAPON),
    CRITICAL_DAMAGE_BOOST("Bonus Kerusakan Critical", "%", 25.0, Material.BLAZE_POWDER, Category.WEAPON),
    ATTACK_REACH_BOOST("Jarak Jangkau Serangan (Reach)", " Blok", 1.0, Material.FISHING_ROD, Category.WEAPON),

    // === TOOL ATTRIBUTES (MINING / FARMING / FISHING) ===
    MINING_REACH_BOOST("Jarak Jangkau Blok (Mining Reach)", " Blok", 1.0, Material.COMPASS, Category.TOOL),
    EXP_MULTIPLIER("Bonus Pengali EXP Aktivitas", "%", 50.0, Material.EXPERIENCE_BOTTLE, Category.TOOL),
    UNBREAKABLE_SET("Durability Abadi (Tidak Rusak)", "", 1.0, Material.ANVIL, Category.TOOL),
    FATIGUE_IMMUNITY("Kebal Efek Mining Fatigue", "", 1.0, Material.MILK_BUCKET, Category.TOOL);

    public enum Category {
        WEAPON,
        TOOL
    }

    private final String displayName;
    private final String unit;
    private final double defaultValue;
    private final Material icon;
    private final Category category;

    ToolStatType(String displayName, String unit, double defaultValue, Material icon, Category category) {
        this.displayName = displayName;
        this.unit = unit;
        this.defaultValue = defaultValue;
        this.icon = icon;
        this.category = category;
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

    public Material getIcon() {
        return icon;
    }

    public Category getCategory() {
        return category;
    }

    public String formatValue(double val) {
        if (this == UNBREAKABLE_SET || this == FATIGUE_IMMUNITY) {
            return val > 0 ? "Aktif" : "Nonaktif";
        }
        if (unit.equals(" Blok")) {
            return "+" + (long) val + unit;
        }
        if (unit.equals("%")) {
            return "+" + (long) val + "%";
        }
        return "+" + (long) val + unit;
    }
}
