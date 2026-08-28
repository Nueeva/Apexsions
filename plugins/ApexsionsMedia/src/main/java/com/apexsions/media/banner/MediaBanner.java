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
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final BlockFace facing;
    private final int widthTiles;
    private final int heightTiles;
    private final String source;
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
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public BlockFace getFacing() { return facing; }
    public int getWidthTiles() { return widthTiles; }
    public int getHeightTiles() { return heightTiles; }
    public String getSource() { return source; }
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

    public BoundingBox getBoundingBox() {
        double minX = x;
        double minY = y - heightTiles + 1.0;
        double minZ = z;

        double maxX = x;
        double maxY = y + 1.0;
        double maxZ = z;

        // Facing direction expansions
        switch (facing) {
            case NORTH -> {
                maxX = x + widthTiles;
                maxZ = z + 0.2;
                minZ = z - 0.2;
            }
            case SOUTH -> {
                minX = x - widthTiles + 1.0;
                maxX = x + 1.0;
                maxZ = z + 0.2;
                minZ = z - 0.2;
            }
            case WEST -> {
                maxZ = z - widthTiles + 1.0;
                minZ = z - widthTiles + 1.0;
                maxX = x + 0.2;
                minX = x - 0.2;
            }
            case EAST -> {
                maxZ = z + widthTiles;
                maxX = x + 0.2;
                minX = x - 0.2;
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
