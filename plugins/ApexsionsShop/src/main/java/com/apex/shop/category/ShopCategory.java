package com.apex.shop.category;

import org.bukkit.Material;

public enum ShopCategory {
    BLOCKS("blocks", "Blocks & Alam", Material.BRICKS, 10, "<gray>Blok konstruksi, bebatuan, dan kayu alam.</gray>"),
    FOOD("food", "Makanan & Masakan", Material.COOKED_BEEF, 12, "<yellow>Makanan olahan, ransum, dan daging matang.</yellow>"),
    FARMING("farming", "Pertanian & Tani", Material.WHEAT, 14, "<green>Hasil panen kebun, bibit, dan tanaman agraris.</green>"),
    ORES("ores", "Pertambangan & Mineral", Material.DIAMOND, 16, "<aqua>Bijih logam murni, permata, dan mineral langka.</aqua>"),
    MOB_DROPS("mob_drops", "Mob Drops & Berburu", Material.ROTTEN_FLESH, 28, "<red>Jarahan monster ganas dan hewan buruan.</red>"),
    DYES("dyes", "Dyes & Pewarna", Material.RED_DYE, 30, "<light_purple>Aneka pewarna alami dan sintetis dekorasi.</light_purple>");

    private final String id;
    private final String displayName;
    private final Material icon;
    private final int slot;
    private final String description;

    ShopCategory(String id, String displayName, Material icon, int slot, String description) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.slot = slot;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public int getSlot() {
        return slot;
    }

    public String getDescription() {
        return description;
    }

    public static ShopCategory fromId(String id) {
        if (id == null) return null;
        for (ShopCategory cat : values()) {
            if (cat.id.equalsIgnoreCase(id) || cat.name().equalsIgnoreCase(id)) {
                return cat;
            }
        }
        return null;
    }
}
