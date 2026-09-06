package com.apexsions.crates.reward;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;

public enum RewardRarity {
    COMMON("<gray>Common</gray>", Material.GRAY_STAINED_GLASS_PANE, "#95a5a6", 1),
    UNCOMMON("<green>Uncommon</green>", Material.LIME_STAINED_GLASS_PANE, "#2ecc71", 2),
    RARE("<blue>Rare</blue>", Material.LIGHT_BLUE_STAINED_GLASS_PANE, "#3498db", 3),
    EPIC("<light_purple>Epic</light_purple>", Material.PURPLE_STAINED_GLASS_PANE, "#9b59b6", 4),
    LEGENDARY("<gold>Legendary</gold>", Material.ORANGE_STAINED_GLASS_PANE, "#f39c12", 5),
    MYTHIC("<red>Mythic</red>", Material.RED_STAINED_GLASS_PANE, "#e74c3c", 6),
    APEX("<gradient:#ff4757:#2ed573><bold>APEX</bold></gradient>", Material.MAGENTA_STAINED_GLASS_PANE, "#ff4757", 7);

    private final String formattedName;
    private final Material glassMaterial;
    private final String hexColor;
    private final int tier;

    RewardRarity(String formattedName, Material glassMaterial, String hexColor, int tier) {
        this.formattedName = formattedName;
        this.glassMaterial = glassMaterial;
        this.hexColor = hexColor;
        this.tier = tier;
    }

    public Component getDisplayName() {
        return MiniMessage.miniMessage().deserialize(formattedName);
    }

    public String getFormattedName() {
        return formattedName;
    }

    public Material getGlassMaterial() {
        return glassMaterial;
    }

    public String getHexColor() {
        return hexColor;
    }

    public int getTier() {
        return tier;
    }

    public static RewardRarity fromString(String name) {
        if (name == null) return COMMON;
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return COMMON;
        }
    }
}
