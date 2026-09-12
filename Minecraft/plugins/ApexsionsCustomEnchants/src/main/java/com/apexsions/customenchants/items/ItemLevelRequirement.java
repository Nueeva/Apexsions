package com.apexsions.customenchants.items;

import com.apexsions.core.api.ApexsionsCoreProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages item minimum level requirements synchronized with ApexsionsCore player levels.
 */
public final class ItemLevelRequirement {

    public static final NamespacedKey KEY_MIN_LEVEL = new NamespacedKey("apexsions", "min_level");
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

    // Cooldown tracker to prevent sound & message spam when player repeatedly attacks or clicks (1.5s cooldown)
    private static final Map<UUID, Long> feedbackCooldown = new ConcurrentHashMap<>();

    private ItemLevelRequirement() {}

    /**
     * Gets the minimum required ApexsionsCore level to use this item.
     * Returns 0 if there is no level requirement.
     */
    public static int getRequiredLevel(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        return getRequiredLevel(meta);
    }

    /**
     * Gets the minimum required ApexsionsCore level from ItemMeta.
     */
    public static int getRequiredLevel(@Nullable ItemMeta meta) {
        if (meta == null) return 0;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(KEY_MIN_LEVEL, PersistentDataType.INTEGER)) {
            Integer val = pdc.get(KEY_MIN_LEVEL, PersistentDataType.INTEGER);
            return val != null ? Math.max(0, val) : 0;
        }
        return 0;
    }

    /**
     * Returns the styled Adventure Component for the level requirement lore line.
     */
    public static @NotNull Component formatLevelLore(int level) {
        return mm.deserialize("<gradient:#f39c12:#e67e22><bold>🎖 SYARAT PENGGUNAAN: </bold></gradient><yellow>Level </yellow><gold><bold>" + level + "+</bold></gold>");
    }

    /**
     * Checks whether a plain-text line represents a level requirement lore line.
     */
    public static boolean isLevelRequirementLore(@Nullable String plainUpper) {
        if (plainUpper == null || plainUpper.isBlank()) return false;
        return plainUpper.contains("SYARAT PENGGUNAAN") ||
               plainUpper.contains("SYARAT LEVEL") ||
               plainUpper.contains("MINIMAL LEVEL") ||
               plainUpper.contains("REQUIREMENT: LEVEL") ||
               plainUpper.contains("LEVEL REQUIREMENT");
    }

    /**
     * Checks whether a plain-text line represents a Set Bonus header.
     */
    public static boolean isSetBonusHeader(@Nullable String plainUpper) {
        if (plainUpper == null || plainUpper.isBlank()) return false;
        return plainUpper.contains("SET BONUS") ||
               plainUpper.contains("WEAPON SET BONUS") ||
               plainUpper.contains("TOOL SET BONUS");
    }

    /**
     * Checks whether a plain-text line represents any part of an Armor/Tool/Weapon Set Bonus section.
     */
    public static boolean isSetBonusLine(@Nullable String plainUpper) {
        if (plainUpper == null || plainUpper.isBlank()) return false;
        if (isLevelRequirementLore(plainUpper)) return false;
        return isSetBonusHeader(plainUpper) ||
               plainUpper.contains("HALF SET") ||
               plainUpper.contains("FULL SET") ||
               plainUpper.contains("PIECES") ||
               plainUpper.contains("MEMAKAI SET ARMOR") ||
               (plainUpper.contains("SYARAT") && (plainUpper.contains("PIECE") || plainUpper.contains("SET"))) ||
               plainUpper.contains("● EFEK") ||
               plainUpper.contains("EFEK:");
    }

    /**
     * Collapses consecutive empty lines and strips leading/trailing empty lines.
     */
    public static @NotNull List<Component> collapseDuplicateEmptyLines(@Nullable List<Component> lines) {
        if (lines == null || lines.isEmpty()) return new ArrayList<>();
        List<Component> result = new ArrayList<>();
        boolean lastWasBlank = true; // trims leading empty lines

        for (Component c : lines) {
            boolean isBlank = plainSerializer.serialize(c).trim().isEmpty();
            if (isBlank) {
                if (!lastWasBlank) {
                    result.add(Component.empty());
                    lastWasBlank = true;
                }
            } else {
                result.add(c);
                lastWasBlank = false;
            }
        }

        // Remove trailing empty lines
        while (!result.isEmpty() && plainSerializer.serialize(result.get(result.size() - 1)).trim().isEmpty()) {
            result.remove(result.size() - 1);
        }

        return result;
    }

    /**
     * Sets or removes the minimum required ApexsionsCore level on the given item.
     * If level <= 0, the requirement is removed completely.
     */
    public static @NotNull ItemStack setRequiredLevel(@NotNull ItemStack item, int level) {
        if (item.getType().isAir()) return item;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (level <= 0) {
            pdc.remove(KEY_MIN_LEVEL);
            cleanLevelRequirementLore(meta);
        } else {
            pdc.set(KEY_MIN_LEVEL, PersistentDataType.INTEGER, level);
            cleanLevelRequirementLore(meta);
            applyLevelRequirementLore(meta, level);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Removes all existing level requirement lore lines from the ItemMeta.
     */
    public static void cleanLevelRequirementLore(@NotNull ItemMeta meta) {
        if (!meta.hasLore() || meta.lore() == null) return;
        List<Component> lore = new ArrayList<>(meta.lore());
        List<Component> cleaned = new ArrayList<>();

        for (Component c : lore) {
            String plain = plainSerializer.serialize(c).trim().toUpperCase();
            if (isLevelRequirementLore(plain)) {
                continue;
            }
            cleaned.add(c);
        }

        meta.lore(collapseDuplicateEmptyLines(cleaned));
    }

    /**
     * Appends the styled level requirement lore line to the ItemMeta.
     * Guaranteed position: AFTER enchant list and BEFORE armor/tool set bonus.
     */
    public static void applyLevelRequirementLore(@NotNull ItemMeta meta, int level) {
        if (level <= 0) return;
        cleanLevelRequirementLore(meta);

        List<Component> currentLore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        Component reqLine = formatLevelLore(level);

        if (currentLore.isEmpty()) {
            currentLore.add(reqLine);
            meta.lore(currentLore);
            return;
        }

        // Find where the set bonus section begins, if present
        int setBonusStartIndex = -1;
        for (int i = 0; i < currentLore.size(); i++) {
            String plain = plainSerializer.serialize(currentLore.get(i)).trim().toUpperCase();
            if (isSetBonusLine(plain)) {
                setBonusStartIndex = i;
                break;
            }
        }

        List<Component> finalLore = new ArrayList<>();

        if (setBonusStartIndex >= 0) {
            // 1. Add everything before the set bonus (enchants, base description, etc.)
            for (int i = 0; i < setBonusStartIndex; i++) {
                finalLore.add(currentLore.get(i));
            }

            // Remove trailing blank lines before inserting level requirement
            while (!finalLore.isEmpty() && plainSerializer.serialize(finalLore.get(finalLore.size() - 1)).trim().isEmpty()) {
                finalLore.remove(finalLore.size() - 1);
            }

            // If there were lines before (e.g. enchants), add a blank separator line
            if (!finalLore.isEmpty()) {
                finalLore.add(Component.empty());
            }

            // 2. Add level requirement line
            finalLore.add(reqLine);

            // 3. Add a blank line separator before set bonus
            finalLore.add(Component.empty());

            // 4. Add the set bonus lines
            for (int i = setBonusStartIndex; i < currentLore.size(); i++) {
                finalLore.add(currentLore.get(i));
            }
        } else {
            // No set bonus section: add after enchants/content
            finalLore.addAll(currentLore);

            while (!finalLore.isEmpty() && plainSerializer.serialize(finalLore.get(finalLore.size() - 1)).trim().isEmpty()) {
                finalLore.remove(finalLore.size() - 1);
            }

            if (!finalLore.isEmpty()) {
                finalLore.add(Component.empty());
            }

            finalLore.add(reqLine);
        }

        meta.lore(collapseDuplicateEmptyLines(finalLore));
    }

    /**
     * Retrieves the player's progression level from ApexsionsCore.
     * Gracefully falls back to vanilla player level if ApexsionsCore is not available.
     */
    public static int getPlayerCoreLevel(@NotNull Player player) {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsCore") && ApexsionsCoreProvider.isAvailable()) {
                return ApexsionsCoreProvider.get().getLevel(player.getUniqueId());
            }
        } catch (Throwable ignored) {}
        return player.getLevel();
    }

    /**
     * Checks if a player has sufficient level to equip/use the given item.
     * Players with permission 'apexsions.itemlevel.bypass' or OP can always use any item.
     */
    public static boolean canUseItem(@NotNull Player player, @Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return true;
        if (player.isOp() || player.hasPermission("apexsions.itemlevel.bypass")) return true;

        int requiredLevel = getRequiredLevel(item);
        if (requiredLevel <= 0) return true;

        int playerLevel = getPlayerCoreLevel(player);
        return playerLevel >= requiredLevel;
    }

    /**
     * Sends rate-limited sound, actionbar, and chat feedback when player is denied from using an item.
     */
    public static void sendDenyFeedback(@NotNull Player player, int requiredLevel, @NotNull String itemNoun) {
        long now = System.currentTimeMillis();
        Long lastFeedback = feedbackCooldown.get(player.getUniqueId());
        if (lastFeedback != null && (now - lastFeedback) < 1500) {
            return; // Throttle to 1 message every 1.5 seconds
        }
        feedbackCooldown.put(player.getUniqueId(), now);

        int playerLevel = getPlayerCoreLevel(player);

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.9f, 1.0f);
        player.sendActionBar(mm.deserialize("<red><bold>✖ BUTUH LEVEL " + requiredLevel + "+</bold></red> <dark_gray>•</dark_gray> <gray>Levelmu saat ini: <gold>" + playerLevel + "</gold></gray>"));
        player.sendMessage(mm.deserialize("<red>⛔ Kamu belum memenuhi syarat level! Butuh <gold><bold>Level " + requiredLevel + "+</bold></gold> di <gradient:#c0392b:#8e1b1b><bold>Apexsions</bold></gradient> untuk memakai <yellow>" + itemNoun + "</yellow> (Levelmu: <gold>Lv. " + playerLevel + "</gold>).</red>"));
    }

    /**
     * Identifies a friendly descriptive noun for the given item type.
     */
    public static String getItemNoun(@Nullable ItemStack item) {
        if (item == null) return "item ini";
        String name = item.getType().name();
        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") || item.getType().name().equals("ELYTRA")) {
            return "armor ini";
        }
        if (name.endsWith("_SWORD") || name.endsWith("_AXE") || name.equals("BOW") || name.equals("CROSSBOW") || name.equals("TRIDENT") || name.equals("MACE")) {
            return "senjata ini";
        }
        if (name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE") || name.equals("FISHING_ROD") || name.equals("SHEARS")) {
            return "peralatan ini";
        }
        return "item ini";
    }
}
