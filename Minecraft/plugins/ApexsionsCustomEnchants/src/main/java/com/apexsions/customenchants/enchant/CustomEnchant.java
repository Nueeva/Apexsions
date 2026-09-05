package com.apexsions.customenchants.enchant;

import com.apexsions.customenchants.group.EnchantmentGroup;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Model representing an individual custom enchantment.
 */
public class CustomEnchant {

    private final String id;
    private final String displayName;
    private final EnchantmentGroup group;
    private final int maxLevel;
    private final String appliesTo;
    private final String description;
    private final boolean purchasable;
    private final boolean enchantable;

    public CustomEnchant(String id, String displayName, EnchantmentGroup group, int maxLevel, String appliesTo, String description, boolean purchasable, boolean enchantable) {
        this.id = id.toLowerCase().trim();
        this.displayName = displayName;
        this.group = group;
        this.maxLevel = Math.max(1, maxLevel);
        this.appliesTo = appliesTo != null ? appliesTo.toUpperCase().trim() : "ALL";
        this.description = description;
        this.purchasable = purchasable;
        this.enchantable = enchantable;
    }

    public CustomEnchant(String id, String displayName, EnchantmentGroup group, int maxLevel, String appliesTo, String description) {
        this(id, displayName, group, maxLevel, appliesTo, description, !id.equalsIgnoreCase("wings"), !id.equalsIgnoreCase("wings"));
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EnchantmentGroup getGroup() {
        return group;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public String getAppliesTo() {
        return appliesTo;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPurchasable() {
        return purchasable;
    }

    public boolean isEnchantable() {
        return enchantable;
    }

    public String getFormattedName() {
        return "<color:" + group.getColor() + ">" + displayName + "</color>";
    }

    public String getTargets() {
        return appliesTo;
    }

    public boolean canApplyTo(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        String name = item.getType().name();
        String target = appliesTo.toUpperCase();

        if (target.contains("ALL")) return true;
        if (target.contains("ARMOR")) {
            if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") || item.getType() == Material.ELYTRA) return true;
        }
        if (target.contains("HELMET") && (name.endsWith("_HELMET") || item.getType() == Material.TURTLE_HELMET)) return true;
        if (target.contains("CHESTPLATE") && (name.endsWith("_CHESTPLATE") || item.getType() == Material.ELYTRA)) return true;
        if (target.contains("LEGGING") && name.endsWith("_LEGGINGS")) return true;
        if (target.contains("BOOT") && name.endsWith("_BOOTS")) return true;
        if (target.contains("ELYTRA") && item.getType() == Material.ELYTRA) return true;

        if (target.contains("WEAPON") && (name.endsWith("_SWORD") || name.endsWith("_AXE") || item.getType() == Material.BOW || item.getType() == Material.CROSSBOW || item.getType() == Material.TRIDENT || item.getType() == Material.MACE)) return true;
        if (target.contains("SWORD") && name.endsWith("_SWORD")) return true;
        if (target.contains("AXE") && name.endsWith("_AXE")) return true;
        if (target.contains("BOW") && (item.getType() == Material.BOW || item.getType() == Material.CROSSBOW)) return true;
        if (target.contains("CROSSBOW") && item.getType() == Material.CROSSBOW) return true;
        if (target.contains("TRIDENT") && item.getType() == Material.TRIDENT) return true;

        if (target.contains("TOOL") && (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE") || item.getType() == Material.SHEARS || item.getType() == Material.FISHING_ROD)) return true;
        if (target.contains("PICKAXE") && name.endsWith("_PICKAXE")) return true;
        if (target.contains("SHOVEL") && name.endsWith("_SHOVEL")) return true;
        if (target.contains("HOE") && name.endsWith("_HOE")) return true;
        if (target.contains("FISHING") && item.getType() == Material.FISHING_ROD) return true;

        return false;
    }

    public static String toRoman(int number) {
        if (number <= 0) return String.valueOf(number);
        final int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        final String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        int temp = number;
        for (int i = 0; i < values.length; i++) {
            while (temp >= values[i]) {
                temp -= values[i];
                sb.append(symbols[i]);
            }
        }
        return sb.toString();
    }
}
