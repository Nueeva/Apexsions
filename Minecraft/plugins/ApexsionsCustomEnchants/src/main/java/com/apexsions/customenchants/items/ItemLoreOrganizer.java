package com.apexsions.customenchants.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized organizer for item lore across ApexsionsCustomEnchants.
 * Strictly guarantees the standardized lore ordering:
 * 1. {list enchant} (Vanilla & Custom Enchants / Base Description)
 *    [1 Empty Line Spacing]
 * 2. {syarat level} (🎖 SYARAT PENGGUNAAN: Level X+)
 *    [1 Empty Line Spacing]
 * 3. {keterangan bonus armor/tools set} (★ SET BONUS ... ★)
 */
public final class ItemLoreOrganizer {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private ItemLoreOrganizer() {}

    /**
     * Reorganizes the item's current lore to match the standardized ordering.
     */
    public static void organizeLore(@NotNull ItemMeta meta) {
        int level = ItemLevelRequirement.getRequiredLevel(meta);
        organizeLore(meta, level);
    }

    /**
     * Reorganizes the item's lore with an explicit minimum level requirement.
     */
    public static void organizeLore(@NotNull ItemMeta meta, int level) {
        List<Component> currentLore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        if (currentLore.isEmpty() && level <= 0) {
            return;
        }

        List<Component> enchantAndBaseLines = new ArrayList<>();
        List<Component> setBonusLines = new ArrayList<>();
        boolean foundSetBonus = false;

        for (Component c : currentLore) {
            String plain = PLAIN.serialize(c).trim().toUpperCase();
            // Skip existing level requirement line as we will insert it in its dedicated position
            if (ItemLevelRequirement.isLevelRequirementLore(plain)) {
                continue;
            }

            if (!foundSetBonus && ItemLevelRequirement.isSetBonusLine(plain)) {
                foundSetBonus = true;
            }

            if (foundSetBonus) {
                setBonusLines.add(c);
            } else {
                enchantAndBaseLines.add(c);
            }
        }

        assembleAndApply(meta, enchantAndBaseLines, level, setBonusLines);
    }

    /**
     * Assembles lore from explicit enchant lines, base lore, and level requirement.
     * Used by EnchantmentRegistry when enchants are updated.
     */
    public static void organizeWithEnchants(@NotNull ItemMeta meta, @NotNull List<Component> enchantLines, @NotNull List<Component> otherBaseLines, int level) {
        List<Component> enchantAndBase = new ArrayList<>(enchantLines);
        List<Component> setBonusLines = new ArrayList<>();
        boolean foundSetBonus = false;

        for (Component c : otherBaseLines) {
            String plain = PLAIN.serialize(c).trim().toUpperCase();
            if (ItemLevelRequirement.isLevelRequirementLore(plain)) {
                continue;
            }

            if (!foundSetBonus && ItemLevelRequirement.isSetBonusLine(plain)) {
                foundSetBonus = true;
            }

            if (foundSetBonus) {
                setBonusLines.add(c);
            } else {
                enchantAndBase.add(c);
            }
        }

        assembleAndApply(meta, enchantAndBase, level, setBonusLines);
    }

    /**
     * Replaces or sets the Set Bonus section while strictly maintaining:
     * {enchant list} -> [empty line] -> {syarat level} -> [empty line] -> {set bonus}
     */
    public static void applySetBonusSection(@NotNull ItemMeta meta, @Nullable List<Component> newSetBonusLines) {
        int level = ItemLevelRequirement.getRequiredLevel(meta);
        List<Component> currentLore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();

        List<Component> enchantAndBaseLines = new ArrayList<>();
        for (Component c : currentLore) {
            String plain = PLAIN.serialize(c).trim().toUpperCase();
            if (ItemLevelRequirement.isLevelRequirementLore(plain)) {
                continue;
            }
            if (ItemLevelRequirement.isSetBonusLine(plain)) {
                break; // Stop at set bonus start
            }
            enchantAndBaseLines.add(c);
        }

        assembleAndApply(meta, enchantAndBaseLines, level, newSetBonusLines != null ? newSetBonusLines : List.of());
    }

    private static void assembleAndApply(@NotNull ItemMeta meta, @NotNull List<Component> enchantAndBase, int level, @NotNull List<Component> setBonusLines) {
        // Trim trailing empty lines from enchantAndBase
        while (!enchantAndBase.isEmpty() && PLAIN.serialize(enchantAndBase.get(enchantAndBase.size() - 1)).trim().isEmpty()) {
            enchantAndBase.remove(enchantAndBase.size() - 1);
        }

        // Trim leading & trailing empty lines from setBonusLines
        List<Component> cleanSetBonus = new ArrayList<>();
        for (Component c : setBonusLines) {
            cleanSetBonus.add(c);
        }
        while (!cleanSetBonus.isEmpty() && PLAIN.serialize(cleanSetBonus.get(0)).trim().isEmpty()) {
            cleanSetBonus.remove(0);
        }
        while (!cleanSetBonus.isEmpty() && PLAIN.serialize(cleanSetBonus.get(cleanSetBonus.size() - 1)).trim().isEmpty()) {
            cleanSetBonus.remove(cleanSetBonus.size() - 1);
        }

        List<Component> finalLore = new ArrayList<>();

        // 1. {list enchant & base lines}
        if (!enchantAndBase.isEmpty()) {
            finalLore.addAll(enchantAndBase);
        }

        // 2. {syarat level}
        if (level > 0) {
            if (!finalLore.isEmpty()) {
                finalLore.add(Component.empty());
            }
            finalLore.add(ItemLevelRequirement.formatLevelLore(level));
        }

        // 3. {keterangan bonus armor/tools set}
        if (!cleanSetBonus.isEmpty()) {
            if (!finalLore.isEmpty()) {
                finalLore.add(Component.empty());
            }
            finalLore.addAll(cleanSetBonus);
        }

        meta.lore(ItemLevelRequirement.collapseDuplicateEmptyLines(finalLore));
    }
}
