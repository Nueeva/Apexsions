package com.apexsions.media.banner;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MediaBanner {

    public enum ClickMode {
        CHAT_PROMPT,
        GUI_CONFIRM,
        DIRECT_URL
    }

    private final String id;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private BlockFace facing;
    private int widthTiles;
    private int heightTiles;
    private String source;
    private String linkUrl;
    private ClickMode clickMode;
    private final List<UUID> itemFrameUuids = new ArrayList<>();
    private final List<Integer> mapIds = new ArrayList<>();

    public MediaBanner(String id, String worldName, double x, double y, double z,
                       BlockFace facing, int widthTiles, int heightTiles,
                       String source, String linkUrl, ClickMode clickMode) {
        this.id = id;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.facing = facing;
        this.widthTiles = widthTiles;
        this.heightTiles = heightTiles;
        this.source = source;
        this.linkUrl = linkUrl;
        this.clickMode = clickMode != null ? clickMode : ClickMode.CHAT_PROMPT;
    }

    public String getId() { return id; }
    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    public BlockFace getFacing() { return facing; }
    public void setFacing(BlockFace facing) { this.facing = facing; }
    public int getWidthTiles() { return widthTiles; }
    public void setWidthTiles(int widthTiles) { this.widthTiles = widthTiles; }
    public int getHeightTiles() { return heightTiles; }
    public void setHeightTiles(int heightTiles) { this.heightTiles = heightTiles; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public ClickMode getClickMode() { return clickMode; }
    public void setClickMode(ClickMode clickMode) { this.clickMode = clickMode; }

    public List<UUID> getItemFrameUuids() { return itemFrameUuids; }
    public List<Integer> getMapIds() { return mapIds; }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public Location getLocation() {
        World w = getWorld();
        return w != null ? new Location(w, x, y, z) : null;
    }

    public void updateLocation(Location loc, BlockFace facing) {
        if (loc == null || loc.getWorld() == null) return;
        this.worldName = loc.getWorld().getName();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.facing = facing;
    }

    public BoundingBox getBoundingBox() {
        double minX = x;
        double minY = y - heightTiles + 1.0;
        double minZ = z;

        double maxX = x;
        double maxY = y + 1.0;
        double maxZ = z;

        switch (facing) {
            case NORTH -> {
                minX = x - widthTiles + 1.0;
                maxX = x + 1.0;
                maxZ = z + 0.3;
                minZ = z - 0.3;
            }
            case SOUTH -> {
                minX = x;
                maxX = x + widthTiles;
                maxZ = z + 0.3;
                minZ = z - 0.3;
            }
            case WEST -> {
                minZ = z;
                maxZ = z + widthTiles;
                maxX = x + 0.3;
                minX = x - 0.3;
            }
            case EAST -> {
                minZ = z - widthTiles + 1.0;
                maxZ = z + 1.0;
                maxX = x + 0.3;
                minX = x - 0.3;
            }
            default -> {
                maxX = x + widthTiles;
                maxZ = z + heightTiles;
            }
        }

        return BoundingBox.of(
                new Vector(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ)),
                new Vector(Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ))
        );
    }
}
