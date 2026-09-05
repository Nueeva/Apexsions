package com.apexsions.shop.dynamic;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages server-wide dynamic commodity market supply, transaction volume,
 * and smooth logarithmic price elasticity for a multiplayer environment.
 */
public class SupplyScannerService {

    private final ApexsionsShop plugin;
    private final Map<Material, Integer> recentMarketVolume = new ConcurrentHashMap<>();
    private BukkitTask recoveryTask;

    public SupplyScannerService(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (recoveryTask != null) {
            recoveryTask.cancel();
        }

        long intervalMinutes = plugin.getConfigManager().getMarketsConfig().getLong("supply-market.recovery-interval-minutes", 10L);
        long intervalTicks = Math.max(20L * 60L, intervalMinutes * 60L * 20L);

        recoveryTask = Bukkit.getScheduler().runTaskTimer(plugin, this::decayMarketVolume, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (recoveryTask != null) {
            recoveryTask.cancel();
            recoveryTask = null;
        }
        recentMarketVolume.clear();
    }

    /**
     * Records an item sale into the global market volume tracker.
     */
    public void recordSale(Material material, int amount) {
        if (material == null || amount <= 0) return;
        recentMarketVolume.merge(material, amount, Integer::sum);
    }

    /**
     * Calculates saturation multiplier for commodities based on server-wide recent volume.
     * Uses smooth logarithmic curve so massive selling causes gentle, realistic market shifts
     * without punishing individual players or causing erratic GUI price jumps.
     */
    public double getSupplySellMultiplier(ShopItem item, Player player, int quantityToSell) {
        return getSupplySellMultiplier(item);
    }

    public double getSupplySellMultiplier(ShopItem item) {
        if (item == null) return 1.00;

        boolean enabled = plugin.getConfigManager().getMarketsConfig().getBoolean("supply-market.enabled",
                plugin.getConfigManager().getMarketsConfig().getBoolean("supply-scanner.enabled", true));
        if (!enabled) {
            return 1.00;
        }

        int volume = recentMarketVolume.getOrDefault(item.getMaterial(), 0);
        int baseThreshold = plugin.getConfigManager().getMarketsConfig().getInt("supply-market.base-volume-threshold", 2304);
        if (volume <= baseThreshold) {
            return 1.00;
        }

        double sensitivity = plugin.getConfigManager().getMarketsConfig().getDouble("supply-market.sensitivity", 0.025);
        double maxDrop = plugin.getConfigManager().getMarketsConfig().getDouble("supply-market.max-saturation-drop", 0.10);
        double minMultiplier = plugin.getConfigManager().getMarketsConfig().getDouble("supply-market.min-sell-multiplier", 0.90);

        int excessVolume = volume - baseThreshold;
        double ratio = (double) excessVolume / (double) baseThreshold;

        // Smooth logarithmic elasticity: drop = min(maxDrop, ln(1 + ratio) * sensitivity)
        double drop = Math.min(maxDrop, Math.log1p(ratio) * sensitivity);
        return Math.max(minMultiplier, 1.00 - drop);
    }

    public double getSupplyBuyMultiplier(ShopItem item) {
        // High supply slightly decreases buy cost for buyers (rewarding abundance)
        double sellMult = getSupplySellMultiplier(item);
        if (sellMult < 1.00) {
            double discount = (1.00 - sellMult) * 0.5; // Half of supply drop reflected as discount
            return Math.max(0.90, 1.00 - discount);
        }
        return 1.00;
    }

    /**
     * Periodic natural market recovery: decays recent volume back to baseline.
     */
    private void decayMarketVolume() {
        if (recentMarketVolume.isEmpty()) return;

        double decayPercent = plugin.getConfigManager().getMarketsConfig().getDouble("supply-market.recovery-percent-per-interval", 25.0);
        double retainRatio = Math.max(0.0, 1.0 - (decayPercent / 100.0));

        recentMarketVolume.replaceAll((mat, count) -> {
            int newCount = (int) (count * retainRatio);
            return newCount > 10 ? newCount : 0;
        });

        recentMarketVolume.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    public int getRecentMarketVolume(Material material) {
        return recentMarketVolume.getOrDefault(material, 0);
    }

    /**
     * Scans nearby chests in radius (optional utility for territory inspections)
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
