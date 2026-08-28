package com.apexsions.shop.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class InventoryUtil {

    private InventoryUtil() {}

    public static int countItems(Player player, Material material) {
        if (player == null || material == null) return 0;
        int count = 0;
        for (ItemStack is : player.getInventory().getStorageContents()) {
            if (is != null && is.getType() == material) {
                count += is.getAmount();
            }
        }
        return count;
    }

    public static boolean hasEnoughSpace(Player player, ItemStack item) {
        if (player == null || item == null) return false;
        int free = 0;
        for (ItemStack is : player.getInventory().getStorageContents()) {
            if (is == null || is.getType() == Material.AIR) {
                free += item.getMaxStackSize();
            } else if (is.isSimilar(item)) {
                free += Math.max(0, is.getMaxStackSize() - is.getAmount());
            }
        }
        return free >= item.getAmount();
    }

    public static void removeItems(Player player, Material material, int amount) {
        if (player == null || material == null || amount <= 0) return;
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack is = contents[i];
            if (is != null && is.getType() == material) {
                if (is.getAmount() <= remaining) {
                    remaining -= is.getAmount();
                    contents[i] = null;
                } else {
                    is.setAmount(is.getAmount() - remaining);
                    remaining = 0;
                    break;
                }
            }
        }
        player.getInventory().setStorageContents(contents);
    }
}
