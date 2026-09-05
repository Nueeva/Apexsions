package com.apexsions.core.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.awt.Color;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain model representing a Region / Kingdom with BlueMap boundary polygon and spawn data.
 */
public class Region {

    private final UUID id;
    private final String key;
    private String displayName;
    private String worldName;
    private Double spawnX;
    private Double spawnY;
    private Double spawnZ;
    private Float spawnYaw;
    private Float spawnPitch;
    private boolean enabled;
    private TerritoryPolygon polygon;
    private Color lineColor;
    private Color fillColor;

    public Region(UUID id, String key, String displayName, String worldName,
                  Double spawnX, Double spawnY, Double spawnZ, Float spawnYaw, Float spawnPitch,
                  boolean enabled) {
        this.id = Objects.requireNonNull(id, "Region ID cannot be null");
        this.key = Objects.requireNonNull(key, "Region key cannot be null").toUpperCase();
        this.displayName = Objects.requireNonNull(displayName, "Display name cannot be null");
        this.worldName = Objects.requireNonNull(worldName, "World name cannot be null");
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        this.spawnYaw = spawnYaw != null ? spawnYaw : 0.0f;
        this.spawnPitch = spawnPitch != null ? spawnPitch : 0.0f;
        this.enabled = enabled;
    }

    public UUID getId() {
        return id;
    }

    public String getKey() {
        return key;
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

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public Double getSpawnX() {
        return spawnX;
    }

    public void setSpawnX(Double spawnX) {
        this.spawnX = spawnX;
    }

    public Double getSpawnY() {
        return spawnY;
    }

    public void setSpawnY(Double spawnY) {
        this.spawnY = spawnY;
    }

    public Double getSpawnZ() {
        return spawnZ;
    }

    public void setSpawnZ(Double spawnZ) {
        this.spawnZ = spawnZ;
    }

    public Float getSpawnYaw() {
        return spawnYaw;
    }

    public void setSpawnYaw(Float spawnYaw) {
        this.spawnYaw = spawnYaw;
    }

    public Float getSpawnPitch() {
        return spawnPitch;
    }

    public void setSpawnPitch(Float spawnPitch) {
        this.spawnPitch = spawnPitch;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public TerritoryPolygon getPolygon() {
        return polygon;
    }

    public void setPolygon(TerritoryPolygon polygon) {
        this.polygon = polygon;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public boolean containsLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equalsIgnoreCase(worldName)) return false;
        if (polygon == null) return false;
        return polygon.contains(loc.getX(), loc.getY(), loc.getZ());
    }

    public void setSpawn(String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;
        this.spawnYaw = yaw;
        this.spawnPitch = pitch;
    }

    /**
     * Resolves Bukkit location for this region's spawn.
     */
    public Optional<Location> getBukkitSpawnLocation() {
        if (worldName == null || spawnX == null || spawnY == null || spawnZ == null) {
            return Optional.empty();
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return Optional.empty();
        }
        return Optional.of(new Location(world, spawnX, spawnY, spawnZ,
                spawnYaw != null ? spawnYaw : 0f,
                spawnPitch != null ? spawnPitch : 0f));
    }
}
