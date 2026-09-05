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

    public CustomEnchant(String id, String displayName, EnchantmentGroup group, int maxLevel, String appliesTo, String description) {
        this.id = id.toLowerCase().trim();
        this.displayName = displayName;
        this.group = group;
        this.maxLevel = Math.max(1, maxLevel);
        this.appliesTo = appliesTo != null ? appliesTo.toUpperCase().trim() : "ALL";
        this.description = description;
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

    public String getFormattedName() {
        return "<color:" + group.getColor() + ">" + displayName + "</color>";
    }

    public String getTargets() {
        return appliesTo;
    }

    public boolean canApplyTo(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        String name = item.getType().name();

        return switch (appliesTo) {
            case "SWORD" -> name.endsWith("_SWORD");
            case "AXE" -> name.endsWith("_AXE");
            case "PICKAXE" -> name.endsWith("_PICKAXE");
            case "SHOVEL" -> name.endsWith("_SHOVEL");
            case "HOE" -> name.endsWith("_HOE");
            case "BOW" -> item.getType() == Material.BOW || item.getType() == Material.CROSSBOW;
            case "HELMET" -> name.endsWith("_HELMET") || item.getType() == Material.TURTLE_HELMET;
            case "CHESTPLATE" -> name.endsWith("_CHESTPLATE") || item.getType() == Material.ELYTRA;
            case "LEGGINGS" -> name.endsWith("_LEGGINGS");
            case "BOOTS" -> name.endsWith("_BOOTS");
            case "ARMOR" -> name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") || item.getType() == Material.ELYTRA;
            case "TOOLS" -> name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE");
            case "WEAPONS" -> name.endsWith("_SWORD") || name.endsWith("_AXE") || item.getType() == Material.BOW || item.getType() == Material.CROSSBOW || item.getType() == Material.TRIDENT || item.getType() == Material.MACE;
            case "ALL" -> true;
            default -> true;
        };
    }

    public static String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(number);
        };
    }
}
