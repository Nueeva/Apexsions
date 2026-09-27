package com.apexsions.shop.dynamic.event;

import com.apexsions.shop.category.ShopCategory;
import org.bukkit.Material;

/**
 * Macroeconomic Event Cycles for the Realm of Apexsions.
 * Dynamically shifts supply, demand, and prices to simulate real-world commodity cycles.
 */
public enum MarketEvent {
    MARKET_EQUILIBRIUM(
            "Keseimbangan Pasar",
            "Iklim ekonomi stabil. Seluruh komoditas diperdagangkan pada nilai pasar normal.",
            Material.CLOCK,
            "#f1c40f",
            "#f39c12",
            1.00, // farming
            1.00, // food
            1.00, // ores
            1.00, // blocks
            1.00, // dyes
            1.00  // mob drops
    ),
    AGRARIAN_BOOM(
            "Musim Panen Raya Realm",
            "Hasil panen ladang dan peternakan melimpah! Permintaan ekspor hasil pangan melonjak +25%.",
            Material.GOLDEN_CARROT,
            "#2ecc71",
            "#27ae60",
            1.25, // farming (+25%)
            1.25, // food (+25%)
            1.00, // ores
            1.00, // blocks
            1.10, // dyes (+10%)
            1.00  // mob drops
    ),
    GOLD_RUSH(
            "Demam Emas & Mineral",
            "Eksplorasi geologis besar-besaran! Permintaan bijih logam dan permata melonjak +25%.",
            Material.RAW_GOLD_BLOCK,
            "#e67e22",
            "#d35400",
            1.00, // farming
            1.00, // food
            1.25, // ores (+25%)
            1.15, // blocks (+15%)
            1.00, // dyes
            1.00  // mob drops
    ),
    WAR_MOBILIZATION(
            "Mobilisasi Tempur Tiga Kerajaan",
            "Kerajaan memperkuat pertahanan realm! Drop monster, bubuk mesiu, dan logam dibeli mahal (+30%).",
            Material.NETHERITE_SWORD,
            "#e74c3c",
            "#c0392b",
            1.00, // farming
            1.15, // food (+15%)
            1.20, // ores (+20%)
            1.00, // blocks
            1.00, // dyes
            1.30  // mob drops (+30%)
    ),
    CONSTRUCTION_WAVE(
            "Era Rekonstruksi & Arsitektur",
            "Proyek peradaban megah dicanangkan! Permintaan batu, marmer, tuff, dan kayu bangunan melonjak +25%.",
            Material.CHISELED_TUFF_BRICKS,
            "#3498db",
            "#2980b9",
            1.00, // farming
            1.00, // food
            1.10, // ores (+10%)
            1.25, // blocks (+25%)
            1.15, // dyes (+15%)
            1.00  // mob drops
    );

    private final String displayName;
    private final String description;
    private final Material icon;
    private final String primaryColor;
    private final String secondaryColor;
    private final double farmingSellMultiplier;
    private final double foodSellMultiplier;
    private final double oresSellMultiplier;
    private final double blocksSellMultiplier;
    private final double dyesSellMultiplier;
    private final double mobDropsSellMultiplier;

    MarketEvent(String displayName, String description, Material icon, String primaryColor, String secondaryColor,
                double farmingSellMultiplier, double foodSellMultiplier, double oresSellMultiplier,
                double blocksSellMultiplier, double dyesSellMultiplier, double mobDropsSellMultiplier) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.farmingSellMultiplier = farmingSellMultiplier;
        this.foodSellMultiplier = foodSellMultiplier;
        this.oresSellMultiplier = oresSellMultiplier;
        this.blocksSellMultiplier = blocksSellMultiplier;
        this.dyesSellMultiplier = dyesSellMultiplier;
        this.mobDropsSellMultiplier = mobDropsSellMultiplier;
    }

    public double getSellMultiplier(ShopCategory category) {
        if (category == null) return 1.0;
        return switch (category) {
            case FARMING -> farmingSellMultiplier;
            case FOOD -> foodSellMultiplier;
            case ORES -> oresSellMultiplier;
            case BLOCKS -> blocksSellMultiplier;
            case DYES -> dyesSellMultiplier;
            case MOB_DROPS -> mobDropsSellMultiplier;
        };
    }

    public double getBuyMultiplier(ShopCategory category) {
        return 1.0;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Material getIcon() {
        return icon;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }
}
