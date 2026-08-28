package com.apexsions.shop.dynamic;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SupplyScannerService {

    private final ApexsionsShop plugin;

    public SupplyScannerService(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    /**
     * Calculates saturation multiplier for massive selling.
     * Small quantity (1..576 / 1..9 stacks): 1.00 (Normal).
     * Huge quantity (> 9 stacks): slight graceful drop down to min 0.75.
     */
    public double getSupplySellMultiplier(ShopItem item, Player player, int quantityToSell) {
        if (!plugin.getConfigManager().getMarketsConfig().getBoolean("supply-scanner.enabled", true)) {
            return 1.00;
        }

        double dropPerStack = plugin.getConfigManager().getMarketsConfig().getDouble("supply-scanner.saturation-drop-per-stack", 0.015);
        double minMultiplier = plugin.getConfigManager().getMarketsConfig().getDouble("supply-scanner.min-sell-multiplier", 0.75);

        int totalFound = quantityToSell;

        // Scan player's inventory
        if (player != null && player.getInventory() != null) {
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null && is.getType() == item.getMaterial()) {
                    totalFound += is.getAmount();
                }
            }
        }

        // 9 stacks = 576 items
        int excessItems = Math.max(0, totalFound - 576);
        int excessStacks = excessItems / 64;

        double reduction = excessStacks * dropPerStack;
        double multiplier = Math.max(minMultiplier, 1.00 - reduction);

        return multiplier;
    }

    /**
     * Scans nearby chests in radius (optional feature for chest market saturation audit)
     */
    public int countNearbyChestStock(Location loc, Material mat, int radius) {
        if (loc == null || loc.getWorld() == null) return 0;
        int count = 0;
        int minX = loc.getBlockX() - radius;
        int maxX = loc.getBlockX() + radius;
        int minY = Math.max(loc.getWorld().getMinHeight(), loc.getBlockY() - 3);
        int maxY = Math.min(loc.getWorld().getMaxHeight(), loc.getBlockY() + 3);
        int minZ = loc.getBlockZ() - radius;
        int maxZ = loc.getBlockZ() + radius;

        for (int x = minX; x <= maxX; x += 2) {
            for (int y = minY; y <= maxY; y += 2) {
                for (int z = minZ; z <= maxZ; z += 2) {
                    BlockState state = loc.getWorld().getBlockAt(x, y, z).getState();
                    if (state instanceof Chest chest) {
                        Inventory inv = chest.getInventory();
                        for (ItemStack is : inv.getContents()) {
                            if (is != null && is.getType() == mat) {
                                count += is.getAmount();
                            }
                        }
                    }
                }
            }
        }
        return count;
    }
}
