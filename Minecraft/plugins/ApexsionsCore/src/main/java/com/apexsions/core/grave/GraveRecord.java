package com.apexsions.core.grave;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Persistent record of a player's grave: location, stored items/exp, owner and expiry.
 */
public class GraveRecord {

    private final String id;
    private final UUID ownerUuid;
    private final String ownerName;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private List<ItemStack> items;
    private int xp;
    private final String cause;
    private final long createdAt;
    private long expiresAt;
    private boolean collected;

    public GraveRecord(String id, UUID ownerUuid, String ownerName, String worldName,
                       double x, double y, double z, float yaw, float pitch,
                       List<ItemStack> items, int xp, @Nullable String cause,
                       long createdAt, long expiresAt, boolean collected) {
        this.id = id;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.items = items != null ? items : new ArrayList<>();
        this.xp = xp;
        this.cause = cause;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.collected = collected;
    }

    public static GraveRecord create(UUID owner, String ownerName, Location loc,
                                     List<ItemStack> items, int xp, @Nullable String cause, long durationMillis) {
        long now = System.currentTimeMillis();
        return new GraveRecord(UUID.randomUUID().toString(), owner, ownerName,
                loc.getWorld() != null ? loc.getWorld().getName() : "world",
                loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(),
                items, xp, cause, now, now + durationMillis, false);
    }

    @Nullable
    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    public double distanceTo(@Nullable Location loc) {
        if (loc == null || loc.getWorld() == null || !loc.getWorld().getName().equalsIgnoreCase(worldName)) {
            return -1;
        }
        return loc.distance(new Location(loc.getWorld(), x, y, z));
    }

    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt;
    }

    public long remainingMillis() {
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }

    @NotNull
    public String remainingFormatted() {
        long sec = remainingMillis() / 1000;
        long min = sec / 60;
        if (min >= 60) {
            return (min / 60) + " jam " + (min % 60) + " menit";
        }
        if (min > 0) {
            return min + " menit " + (sec % 60) + " detik";
        }
        return sec + " detik";
    }

    public String getId() { return id; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public String getOwnerName() { return ownerName; }
    public String getWorldName() { return worldName; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public List<ItemStack> getItems() { return items; }
    public void setItems(List<ItemStack> items) { this.items = items != null ? items : new ArrayList<>(); }
    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }
    @Nullable public String getCause() { return cause; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiresAt() { return expiresAt; }
    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }
}