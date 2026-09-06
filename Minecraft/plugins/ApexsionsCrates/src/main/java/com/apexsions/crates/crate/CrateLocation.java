package com.apexsions.crates.crate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class CrateLocation {

    private final String world;
    private final int x;
    private final int y;
    private final int z;
    private final String crateId;

    public CrateLocation(String world, int x, int y, int z, String crateId) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.crateId = crateId;
    }

    public static CrateLocation fromLocation(Location loc, String crateId) {
        return new CrateLocation(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), crateId);
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getCrateId() {
        return crateId;
    }

    public Location toBukkitLocation() {
        World w = Bukkit.getWorld(world);
        if (w == null) return null;
        return new Location(w, x, y, z);
    }

    public boolean matches(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        return loc.getWorld().getName().equals(world) &&
                loc.getBlockX() == x &&
                loc.getBlockY() == y &&
                loc.getBlockZ() == z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrateLocation that = (CrateLocation) o;
        return x == that.x && y == that.y && z == that.z && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }
}
