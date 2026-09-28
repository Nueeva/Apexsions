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
    private final Map<String, Map<Material, Integer>> kingdomMarketVolume = new ConcurrentHashMap<>();
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
        kingdomMarketVolume.clear();
        recentMarketVolume.clear();
    }

    private String normalizeKingdom(String kingdom) {
        if (kingdom == null || kingdom.trim().isEmpty() || kingdom.equalsIgnoreCase("NONE")) {
            return "GLOBAL";
        }
        return kingdom.trim().toUpperCase();
    }

    private Map<Material, Integer> getVolumeMap(String kingdom) {
        return kingdomMarketVolume.computeIfAbsent(normalizeKingdom(kingdom), k -> new ConcurrentHashMap<>());
    }

    /**
     * Records an item sale (supply influx) into the market volume tracker.
     */
    public void recordSale(String kingdom, Material material, int amount) {
        if (material == null || amount <= 0) return;
        getVolumeMap(kingdom).merge(material, amount, Integer::sum);
        recentMarketVolume.merge(material, amount, Integer::sum);
    }

    public void recordSale(Material material, int amount) {
        recordSale("GLOBAL", material, amount);
    }

    /**
     * Records an item purchase (demand absorption) into the market volume tracker.
     * Decreases net market volume (creating scarcity when heavily bought).
     */
    public void recordPurchase(String kingdom, Material material, int amount) {
        if (material == null || amount <= 0) return;
        getVolumeMap(kingdom).merge(material, -amount, Integer::sum);
        recentMarketVolume.merge(material, -amount, Integer::sum);
    }

    public void recordPurchase(Material material, int amount) {
        recordPurchase("GLOBAL", material, amount);
    }

    /**
     * Calculates saturation/scarcity multiplier for commodities based on kingdom & global volume.
     * Uses smooth logarithmic curve:
     * - Positive volume (Excess Supply) -> Decreases sell price (down to min-sell-multiplier)
     * - Negative volume (High Demand / Scarcity) -> Increases sell price (incentivizing suppliers)
     */
    public double getSupplySellMultiplier(ShopItem item, Player player, int quantityToSell) {
        String kingdom = (plugin.getKingdomMarketService() != null && player != null)
                ? plugin.getKingdomMarketService().resolveKingdom(player, null)
                : "GLOBAL";
        return getSupplySellMultiplier(item, kingdom);
    }

    public double getSupplySellMultiplier(ShopItem item) {
        return getSupplySellMultiplier(item, "GLOBAL");
    }

    public double getSupplySellMultiplier(ShopItem item, String kingdom) {
        if (item == null) return 1.00;

        boolean enabled = plugin.getConfigManager().getMarketsConfig().getBoolean("supply-market.enabled",
                plugin.getConfigManager().getMarketsConfig().getBoolean("supply-scanner.enabled", true));
        if (!enabled) {
            return 1.00;
        }

        int volume = getNetVolume(item.getMaterial(), kingdom);
        int baseThreshold = plugin.getConfigManager().getMarketsConfig().getInt("supply-market.base-volume-threshold", 2304);
        double sensitivity = plugin.getConfigManager().getMarketsConfig().getDouble("supply-market.sensitivity", 0.025);

        if (volume > baseThreshold) {
            // Over-supply (Saturation) -> Sell price drops
            double maxDrop = plugin.getConfigManager().getMarketsConfig().getDouble("supply-market.max-saturation-drop", 0.35);
            double minMultiplier = plugin.getConfigManager().getMarketsConfig().getDouble("supply-market.min-sell-multiplier", 0.65);

            int excessVolume = volume - baseThreshold;
            double ratio = (double) excessVolume / (double) baseThreshold;
            double drop = Math.min(maxDrop, Math.log1p(ratio) * sensitivity);
            return Math.max(minMultiplier, 1.00 - drop);
        } else if (volume < -baseThreshold) {
            // High Demand / Scarcity -> Suppliers are paid extra bonuses
            double maxSurge = plugin.getConfigManager().getMarketsConfig().getDouble("supply-market.max-scarcity-surge", 0.35);
            double maxMultiplier = plugin.getConfigManager().getMarketsConfig().getDouble("supply-market.max-sell-multiplier", 1.35);

            int deficitVolume = Math.abs(volume) - baseThreshold;
            double ratio = (double) deficitVolume / (double) baseThreshold;
            double surge = Math.min(maxSurge, Math.log1p(ratio) * sensitivity);
            return Math.min(maxMultiplier, 1.00 + surge);
        }

        return 1.00;
    }

    public double getSupplyBuyMultiplier(ShopItem item) {
        return getSupplyBuyMultiplier(item, "GLOBAL");
    }

    public double getSupplyBuyMultiplier(ShopItem item, String kingdom) {
        if (item == null) return 1.00;

        boolean enabled = plugin.getConfigManager().getMarketsConfig().getBoolean("supply-market.enabled",
                plugin.getConfigManager().getMarketsConfig().getBoolean("supply-scanner.enabled", true));
        if (!enabled) {
            return 1.00;
        }

        int volume = getNetVolume(item.getMaterial(), kingdom);
        int baseThreshold = plugin.getConfigManager().getMarketsConfig().getInt("supply-market.base-volume-threshold", 2304);
        double sensitivity = plugin.getConfigManager().getMarketsConfig().getDouble("supply-market.sensitivity", 0.025);

        if (volume > baseThreshold) {
            // High supply slightly decreases buy cost for buyers (rewarding abundance)
            double sellMult = getSupplySellMultiplier(item, kingdom);
            if (sellMult < 1.00) {
                double discount = (1.00 - sellMult) * 0.5; // Half of supply drop reflected as discount
                return Math.max(0.85, 1.00 - discount);
            }
        } else if (volume < -baseThreshold) {
            // High demand / Scarcity -> Buyers pay a surge premium due to limited stock
            double maxSurge = plugin.getConfigManager().getMarketsConfig().getDouble("supply-market.max-scarcity-surge", 0.35);
            double maxMultiplier = plugin.getConfigManager().getMarketsConfig().getDouble("supply-market.max-buy-multiplier", 1.35);

            int deficitVolume = Math.abs(volume) - baseThreshold;
            double ratio = (double) deficitVolume / (double) baseThreshold;
            double surge = Math.min(maxSurge, Math.log1p(ratio) * sensitivity);
            return Math.min(maxMultiplier, 1.00 + surge);
        }

        return 1.00;
    }

    /**
     * Retrieves the blended net transaction volume for a material:
     * 70% regional kingdom volume + 30% global server volume.
     */
    public int getNetVolume(Material material, String kingdom) {
        if (material == null) return 0;
        String norm = normalizeKingdom(kingdom);
        int regVol = getVolumeMap(norm).getOrDefault(material, 0);
        if (norm.equals("GLOBAL")) {
            return regVol;
        }
        int globVol = recentMarketVolume.getOrDefault(material, 0);
        return (int) Math.round((regVol * 0.70) + (globVol * 0.30));
    }

    /**
     * Periodic natural market recovery: decays recent volume back to baseline (0 equilibrium).
     */
    private void decayMarketVolume() {
        double decayPercent = plugin.getConfigManager().getMarketsConfig().getDouble("supply-market.recovery-percent-per-interval", 25.0);
        double retainRatio = Math.max(0.0, 1.0 - (decayPercent / 100.0));

        // 1. Decay regional maps
        for (Map<Material, Integer> map : kingdomMarketVolume.values()) {
            map.replaceAll((mat, count) -> {
                int newCount = (int) (count * retainRatio);
                return Math.abs(newCount) > 10 ? newCount : 0;
            });
            map.entrySet().removeIf(entry -> entry.getValue() == 0);
        }

        // 2. Decay global map
        recentMarketVolume.replaceAll((mat, count) -> {
            int newCount = (int) (count * retainRatio);
            return Math.abs(newCount) > 10 ? newCount : 0;
        });
        recentMarketVolume.entrySet().removeIf(entry -> entry.getValue() == 0);
    }

    public int getRecentMarketVolume(Material material) {
        return recentMarketVolume.getOrDefault(material, 0);
    }

    public int getRecentMarketVolume(String kingdom, Material material) {
        return getNetVolume(material, kingdom);
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
