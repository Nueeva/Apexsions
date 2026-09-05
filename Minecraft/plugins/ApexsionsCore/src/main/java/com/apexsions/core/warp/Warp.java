package com.apexsions.core.warp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Warp {

    private final String id;
    private String name;
    private String category;
    private Material icon;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private String permission;
    private int delaySeconds;
    private boolean hidden;
    private String description;

    public Warp(String id, String name, String category, Material icon,
                String worldName, double x, double y, double z, float yaw, float pitch,
                String permission, int delaySeconds, boolean hidden, String description) {
        this.id = id.toLowerCase();
        this.name = name != null ? name : id;
        this.category = category != null ? category.toUpperCase() : "GENERAL";
        this.icon = icon != null ? icon : Material.COMPASS;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.permission = (permission != null && !permission.isBlank()) ? permission : null;
        this.delaySeconds = Math.max(0, delaySeconds);
        this.hidden = hidden;
        this.description = description != null ? description : "";
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category.toUpperCase(); }
    public Material getIcon() { return icon; }
    public void setIcon(Material icon) { this.icon = icon; }
    public String getWorldName() { return worldName; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = (permission != null && !permission.isBlank()) ? permission : null; }
    public int getDelaySeconds() { return delaySeconds; }
    public void setDelaySeconds(int delaySeconds) { this.delaySeconds = Math.max(0, delaySeconds); }
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public Location getLocation() {
        World w = getWorld();
        return w != null ? new Location(w, x, y, z, yaw, pitch) : null;
    }

    public void updateLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        this.worldName = loc.getWorld().getName();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }

    public boolean canAccess(Player player) {
        if (player == null) return false;
        if (player.hasPermission("apexsionscore.warp.admin") || player.hasPermission("kingdomcore.admin")) return true;
        if (permission == null || permission.isBlank()) return true;
        return player.hasPermission(permission) || player.hasPermission("apexsionscore.warp." + id);
    }
}
