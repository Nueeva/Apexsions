package com.apexsions.fishing.model;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a defined cuboid fishing zone integrated with WorldEdit.
 * Zones provide:
 * - AFK Fishing permissions
 * - Boosted catch rates (faster strike speed / auto-catch delay reduction)
 * - Boosted rarity chances (higher likelihood of Rare, Epic, Legendary, Secret fish)
 * - Boosted fish weight (kg)
 * - Boosted XP rewards
 */
public class FishingZone {

    private final String name;
    private String displayName;
    private final String worldName;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;
    private boolean afkAllowed;
    private double rateMultiplier;
    private double rarityMultiplier;
    private double weightMultiplier;
    private double xpMultiplier;

    public FishingZone(
            @NotNull String name,
            @NotNull String displayName,
            @NotNull String worldName,
            int x1, int y1, int z1,
            int x2, int y2, int z2,
            boolean afkAllowed,
            double rateMultiplier,
            double rarityMultiplier,
            double weightMultiplier,
            double xpMultiplier
    ) {
        this.name = name.toLowerCase();
        this.displayName = displayName;
        this.worldName = worldName;
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
        this.afkAllowed = afkAllowed;
        this.rateMultiplier = Math.max(1.0, rateMultiplier);
        this.rarityMultiplier = Math.max(1.0, rarityMultiplier);
        this.weightMultiplier = Math.max(1.0, weightMultiplier);
        this.xpMultiplier = Math.max(1.0, xpMultiplier);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public boolean isAfkAllowed() {
        return afkAllowed;
    }

    public void setAfkAllowed(boolean afkAllowed) {
        this.afkAllowed = afkAllowed;
    }

    public double getRateMultiplier() {
        return rateMultiplier;
    }

    public void setRateMultiplier(double rateMultiplier) {
        this.rateMultiplier = Math.max(1.0, rateMultiplier);
    }

    public double getRarityMultiplier() {
        return rarityMultiplier;
    }

    public void setRarityMultiplier(double rarityMultiplier) {
        this.rarityMultiplier = Math.max(1.0, rarityMultiplier);
    }

    public double getWeightMultiplier() {
        return weightMultiplier;
    }

    public void setWeightMultiplier(double weightMultiplier) {
        this.weightMultiplier = Math.max(1.0, weightMultiplier);
    }

    public double getXpMultiplier() {
        return xpMultiplier;
    }

    public void setXpMultiplier(double xpMultiplier) {
        this.xpMultiplier = Math.max(1.0, xpMultiplier);
    }

    public int getVolume() {
        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }

    public boolean contains(@Nullable Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        if (!location.getWorld().getName().equalsIgnoreCase(worldName)) {
            return false;
        }
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    public Location getCenter(@NotNull org.bukkit.Server server) {
        org.bukkit.World world = server.getWorld(worldName);
        if (world == null) return null;
        double cx = (minX + maxX) / 2.0;
        double cy = (minY + maxY) / 2.0;
        double cz = (minZ + maxZ) / 2.0;
        return new Location(world, cx, cy, cz);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("display-name", displayName);
        map.put("world", worldName);
        map.put("min-x", minX);
        map.put("min-y", minY);
        map.put("min-z", minZ);
        map.put("max-x", maxX);
        map.put("max-y", maxY);
        map.put("max-z", maxZ);
        map.put("afk-allowed", afkAllowed);
        map.put("rate-multiplier", rateMultiplier);
        map.put("rarity-multiplier", rarityMultiplier);
        map.put("weight-multiplier", weightMultiplier);
        map.put("xp-multiplier", xpMultiplier);
        return map;
    }

    @Nullable
    public static FishingZone deserialize(@NotNull String name, @NotNull Map<String, Object> map) {
        try {
            String displayName = (String) map.getOrDefault("display-name", "<gradient:#00c6ff:#0072ff>" + name + "</gradient>");
            String world = (String) map.get("world");
            if (world == null) return null;

            int minX = ((Number) map.getOrDefault("min-x", 0)).intValue();
            int minY = ((Number) map.getOrDefault("min-y", 0)).intValue();
            int minZ = ((Number) map.getOrDefault("min-z", 0)).intValue();
            int maxX = ((Number) map.getOrDefault("max-x", 0)).intValue();
            int maxY = ((Number) map.getOrDefault("max-y", 0)).intValue();
            int maxZ = ((Number) map.getOrDefault("max-z", 0)).intValue();

            boolean afkAllowed = (boolean) map.getOrDefault("afk-allowed", true);
            double rateMultiplier = ((Number) map.getOrDefault("rate-multiplier", 1.5)).doubleValue();
            double rarityMultiplier = ((Number) map.getOrDefault("rarity-multiplier", 1.5)).doubleValue();
            double weightMultiplier = ((Number) map.getOrDefault("weight-multiplier", 1.2)).doubleValue();
            double xpMultiplier = ((Number) map.getOrDefault("xp-multiplier", 1.5)).doubleValue();

            return new FishingZone(name, displayName, world, minX, minY, minZ, maxX, maxY, maxZ,
                    afkAllowed, rateMultiplier, rarityMultiplier, weightMultiplier, xpMultiplier);
        } catch (Throwable t) {
            return null;
        }
    }
}
