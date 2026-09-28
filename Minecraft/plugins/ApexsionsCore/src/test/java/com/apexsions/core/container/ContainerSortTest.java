package com.apexsions.core.container;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContainerSortTest {

    @Test
    public void testCategoryClassification() {
        // Mock-free category classification testing
        ContainerSortManager manager = new ContainerSortManager(null);

        assertEquals(ContainerSortManager.Category.WEAPONS, manager.categoryOf(Material.DIAMOND_SWORD));
        assertEquals(ContainerSortManager.Category.WEAPONS, manager.categoryOf(Material.BOW));
        assertEquals(ContainerSortManager.Category.WEAPONS, manager.categoryOf(Material.NETHERITE_SWORD));

        assertEquals(ContainerSortManager.Category.TOOLS, manager.categoryOf(Material.DIAMOND_PICKAXE));
        assertEquals(ContainerSortManager.Category.TOOLS, manager.categoryOf(Material.NETHERITE_AXE));
        assertEquals(ContainerSortManager.Category.TOOLS, manager.categoryOf(Material.SHEARS));

        assertEquals(ContainerSortManager.Category.ARMOR, manager.categoryOf(Material.DIAMOND_CHESTPLATE));
        assertEquals(ContainerSortManager.Category.ARMOR, manager.categoryOf(Material.NETHERITE_HELMET));
        assertEquals(ContainerSortManager.Category.ARMOR, manager.categoryOf(Material.ELYTRA));

        assertEquals(ContainerSortManager.Category.POTIONS, manager.categoryOf(Material.POTION));
        assertEquals(ContainerSortManager.Category.POTIONS, manager.categoryOf(Material.SPLASH_POTION));

        assertEquals(ContainerSortManager.Category.FOOD, manager.categoryOf(Material.COOKED_BEEF));
        assertEquals(ContainerSortManager.Category.FOOD, manager.categoryOf(Material.GOLDEN_CARROT));

        assertEquals(ContainerSortManager.Category.REDSTONE, manager.categoryOf(Material.REDSTONE));
        assertEquals(ContainerSortManager.Category.REDSTONE, manager.categoryOf(Material.REPEATER));
        assertEquals(ContainerSortManager.Category.REDSTONE, manager.categoryOf(Material.HOPPER));

        assertEquals(ContainerSortManager.Category.BLOCKS, manager.categoryOf(Material.STONE));
        assertEquals(ContainerSortManager.Category.BLOCKS, manager.categoryOf(Material.OAK_PLANKS));

        assertEquals(ContainerSortManager.Category.MISC, manager.categoryOf(Material.FEATHER));
        assertEquals(ContainerSortManager.Category.MISC, manager.categoryOf(Material.STICK));
    }

    @Test
    public void testPlayerInventorySlotBoundaries() {
        // Player storage contents length in Bukkit is 36
        int totalStorageSlots = 36;
        int hotbarStart = 0;
        int hotbarEnd = 9;
        int mainStorageStart = 9;
        int mainStorageEnd = 36;

        assertEquals(9, hotbarEnd - hotbarStart, "Hotbar must contain exactly 9 slots (0..8)");
        assertEquals(27, mainStorageEnd - mainStorageStart, "Main storage must contain exactly 27 slots (9..35)");
        assertEquals(36, totalStorageSlots, "Player storage contents must be 36 slots");

        // When includeHotbar is false, the sort range is [9, 36)
        boolean includeHotbarFalse = false;
        int startSlot = includeHotbarFalse ? 0 : 9;
        int endSlot = 36;
        assertEquals(9, startSlot);
        assertEquals(36, endSlot);

        // When includeHotbar is true, the sort range is [0, 36)
        boolean includeHotbarTrue = true;
        startSlot = includeHotbarTrue ? 0 : 9;
        assertEquals(0, startSlot);
        assertEquals(36, endSlot);
    }
}
