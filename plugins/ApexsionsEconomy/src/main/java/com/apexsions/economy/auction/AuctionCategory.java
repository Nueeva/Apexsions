package com.apexsions.economy.auction;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum AuctionCategory {
    ALL("Semua Item", Material.NETHER_STAR),
    WEAPONS_ARMOR("Senjata & Armor", Material.DIAMOND_SWORD),
    TOOLS("Alat & Perkakas", Material.DIAMOND_PICKAXE),
    MINERALS("Mineral & Bijih", Material.DIAMOND),
    BLOCKS("Blok & Bangunan", Material.BRICKS),
    FOOD_POTIONS("Makanan & Potion", Material.GOLDEN_APPLE),
    MISC("Lainnya / Spesial", Material.CHEST);

    private final String displayName;
    private final Material icon;

    AuctionCategory(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public static AuctionCategory fromItemStack(ItemStack is) {
        if (is == null || is.getType() == Material.AIR) return MISC;
        Material mat = is.getType();
        String name = mat.name();

        if (name.endsWith("_SWORD") || name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") || name.equals("SHIELD")
                || name.equals("BOW") || name.equals("CROSSBOW") || name.equals("TRIDENT") || name.equals("MACE")) {
            return WEAPONS_ARMOR;
        }

        if (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL")
                || name.endsWith("_HOE") || name.equals("FISHING_ROD") || name.equals("SHEARS") || name.equals("FLINT_AND_STEEL")) {
            return TOOLS;
        }

        if (name.endsWith("_INGOT") || name.endsWith("_ORE") || name.endsWith("_RAW") || name.startsWith("RAW_")
                || (name.endsWith("_BLOCK") && (name.contains("DIAMOND") || name.contains("GOLD") || name.contains("IRON") || name.contains("EMERALD") || name.contains("NETHERITE")))
                || name.equals("DIAMOND") || name.equals("EMERALD") || name.equals("NETHERITE_SCRAP") || name.equals("NETHERITE_INGOT")
                || name.equals("GOLD_INGOT") || name.equals("IRON_INGOT") || name.equals("COPPER_INGOT")
                || name.equals("LAPIS_LAZULI") || name.equals("REDSTONE") || name.equals("COAL") || name.equals("AMETHYST_SHARD")) {
            return MINERALS;
        }

        if (mat.isBlock()) {
            return BLOCKS;
        }

        if (mat.isEdible() || name.contains("POTION") || name.equals("MILK_BUCKET") || name.contains("STEW") || name.contains("SOUP")) {
            return FOOD_POTIONS;
        }

        return MISC;
    }
}
