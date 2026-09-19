package com.apexsions.core.player;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Immutable record representing a player's death event details.
 */
public record DeathRecord(
        @NotNull UUID playerUuid,
        @NotNull String worldName,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        @Nullable String deathCause,
        long timestamp
) {

    @Nullable
    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Calculates block distance from a given current location.
     * Returns -1 if worlds differ or current location is null.
     */
    public double getDistance(@Nullable Location currentLoc) {
        if (currentLoc == null || currentLoc.getWorld() == null) {
            return -1;
        }
        if (!currentLoc.getWorld().getName().equalsIgnoreCase(worldName)) {
            return -1;
        }
        return currentLoc.distance(new Location(currentLoc.getWorld(), x, y, z));
    }

    /**
     * Returns user-friendly Indonesian time-ago representation.
     */
    @NotNull
    public String getTimeAgoFormatted() {
        if (timestamp <= 0) {
            return "Terekam dari NBT Kematian Lampau";
        }
        long diffSeconds = (System.currentTimeMillis() - timestamp) / 1000;
        if (diffSeconds < 60) {
            return diffSeconds + " detik yang lalu";
        }
        long diffMinutes = diffSeconds / 60;
        if (diffMinutes < 60) {
            return diffMinutes + " menit yang lalu";
        }
        long diffHours = diffMinutes / 60;
        if (diffHours < 24) {
            return diffHours + " jam yang lalu";
        }
        long diffDays = diffHours / 24;
        return diffDays + " hari yang lalu";
    }

    /**
     * Returns human-readable realm/dimension name.
     */
    @NotNull
    public String getDimensionDisplay() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return worldName;
        }
        return switch (world.getEnvironment()) {
            case NETHER -> "Nether (Dimensi Api)";
            case THE_END -> "The End (Dimensi Hampa)";
            case NORMAL -> "Overworld (Dunia Utama)";
            case CUSTOM -> "Alam Kustom (" + world.getName() + ")";
        };
    }
}
