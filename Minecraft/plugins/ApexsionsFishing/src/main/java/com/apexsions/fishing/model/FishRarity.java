package com.apexsions.fishing.model;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public enum FishRarity {
    COMMON("Common", "<white>", 1.0, 1),
    UNCOMMON("Uncommon", "<green>", 1.8, 2),
    RARE("Rare", "<aqua>", 3.5, 3),
    EPIC("Epic", "<light_purple>", 7.0, 4),
    LEGENDARY("Legendary", "<gold>", 15.0, 5),
    SECRET("Secret", "<gradient:#ff007f:#7928ca><bold>✦ SECRET ✦</bold></gradient>", 35.0, 6);

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final String name;
    private final String formatTag;
    private final double priceMultiplier;
    private final int tierOrder;

    FishRarity(String name, String formatTag, double priceMultiplier, int tierOrder) {
        this.name = name;
        this.formatTag = formatTag;
        this.priceMultiplier = priceMultiplier;
        this.tierOrder = tierOrder;
    }

    public String getName() {
        return name;
    }

    public String getFormatTag() {
        return formatTag;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public int getTierOrder() {
        return tierOrder;
    }

    public Component getFormattedComponent() {
        if (this == SECRET) {
            return MM.deserialize(formatTag);
        }
        return MM.deserialize(formatTag + "<bold>" + name.toUpperCase() + "</bold>" + (formatTag.startsWith("<") ? "</" + formatTag.substring(1) : ""));
    }

    public static @NotNull FishRarity fromString(String raw) {
        if (raw == null) return COMMON;
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return COMMON;
        }
    }
}
