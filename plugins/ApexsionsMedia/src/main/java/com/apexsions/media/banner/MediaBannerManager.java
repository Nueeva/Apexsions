package com.apexsions.media.banner;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.engine.ImageRenderer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MediaBannerManager {

    private final ApexsionsMediaPlugin plugin;
    private final Map<String, MediaBanner> banners = new ConcurrentHashMap<>();
    private final Map<UUID, MediaBanner> frameToBannerMap = new ConcurrentHashMap<>();
    private final Map<Integer, MapView> registeredMapViews = new ConcurrentHashMap<>();
    private HikariDataSource dataSource;

    public MediaBannerManager(ApexsionsMediaPlugin plugin) {
        this.plugin = plugin;
        initDatabase();
        loadAllBanners();
    }

    private void initDatabase() {
        try {
            HikariConfig config = new HikariConfig();
            File dbFile = new File(plugin.getDataFolder(), "banners.db");
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(5);
            config.setConnectionTimeout(5000);

            this.dataSource = new HikariDataSource(config);

            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS media_banners (
                        id TEXT PRIMARY KEY,
                        world TEXT NOT NULL,
                        x REAL NOT NULL,
                        y REAL NOT NULL,
                        z REAL NOT NULL,
                        facing TEXT NOT NULL,
                        width INTEGER NOT NULL,
                        height INTEGER NOT NULL,
                        source TEXT NOT NULL,
                        link_url TEXT,
                        click_mode TEXT NOT NULL
                    );
                """);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize SQLite banners database: " + e.getMessage());
        }
    }

    public void loadAllBanners() {
        banners.clear();
        frameToBannerMap.clear();
        registeredMapViews.clear();

        if (dataSource == null) return;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM media_banners");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String world = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                BlockFace facing = BlockFace.valueOf(rs.getString("facing"));
                int w = rs.getInt("width");
                int h = rs.getInt("height");
                String src = rs.getString("source");
                String link = rs.getString("link_url");
                MediaBanner.ClickMode mode = MediaBanner.ClickMode.valueOf(rs.getString("click_mode"));

                MediaBanner banner = new MediaBanner(id, world, x, y, z, facing, w, h, src, link, mode);
                banners.put(id.toLowerCase(), banner);

                Bukkit.getScheduler().runTask(plugin, () -> spawnBannerFrames(banner));
            }
            plugin.getLogger().info("Loaded " + banners.size() + " media banners from database.");
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading media banners: " + e.getMessage());
        }
    }

    public CompletableFuture<Boolean> createAndSpawnBanner(String id, Location loc, BlockFace facing,
                                                           int width, int height, String source,
                                                           String linkUrl, MediaBanner.ClickMode mode) {
        MediaBanner banner = new MediaBanner(id, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(),
                facing, width, height, source, linkUrl, mode);

        banners.put(id.toLowerCase(), banner);
        return saveBannerToDb(banner);
    }

    public CompletableFuture<Boolean> moveBanner(String id, Location newLoc, BlockFace newFacing) {
        MediaBanner banner = banners.get(id.toLowerCase());
        if (banner == null) return CompletableFuture.completedFuture(false);

        banner.updateLocation(newLoc, newFacing);
        return saveBannerToDb(banner);
    }

    public CompletableFuture<Boolean> cloneBanner(String sourceId, String targetId, Location loc, BlockFace facing) {
        MediaBanner sourceBanner = banners.get(sourceId.toLowerCase());
        if (sourceBanner == null) return CompletableFuture.completedFuture(false);

        return createAndSpawnBanner(targetId, loc, facing,
                sourceBanner.getWidthTiles(), sourceBanner.getHeightTiles(),
                sourceBanner.getSource(), sourceBanner.getLinkUrl(), sourceBanner.getClickMode());
    }

    public CompletableFuture<Boolean> updateBannerLink(String id, String linkUrl, MediaBanner.ClickMode mode) {
        MediaBanner banner = banners.get(id.toLowerCase());
        if (banner == null) return CompletableFuture.completedFuture(false);

        banner.setLinkUrl(linkUrl);
        if (mode != null) banner.setClickMode(mode);
        return saveBannerToDb(banner);
    }

    public CompletableFuture<Boolean> updateBannerSize(String id, int width, int height) {
        MediaBanner banner = banners.get(id.toLowerCase());
        if (banner == null) return CompletableFuture.completedFuture(false);

        banner.setWidthTiles(width);
        banner.setHeightTiles(height);
        return saveBannerToDb(banner);
    }

    private CompletableFuture<Boolean> saveBannerToDb(MediaBanner banner) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                     INSERT INTO media_banners (id, world, x, y, z, facing, width, height, source, link_url, click_mode)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                     ON CONFLICT(id) DO UPDATE SET
                         world=excluded.world, x=excluded.x, y=excluded.y, z=excluded.z,
                         facing=excluded.facing, width=excluded.width, height=excluded.height,
                         source=excluded.source, link_url=excluded.link_url, click_mode=excluded.click_mode
                 """)) {
                ps.setString(1, banner.getId());
                ps.setString(2, banner.getWorldName());
                ps.setDouble(3, banner.getX());
                ps.setDouble(4, banner.getY());
                ps.setDouble(5, banner.getZ());
                ps.setString(6, banner.getFacing().name());
                ps.setInt(7, banner.getWidthTiles());
                ps.setInt(8, banner.getHeightTiles());
                ps.setString(9, banner.getSource());
                ps.setString(10, banner.getLinkUrl());
                ps.setString(11, banner.getClickMode().name());
                ps.executeUpdate();
                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save banner to database: " + e.getMessage());
                return false;
            }
        }).thenApply(success -> {
            if (success) {
                Bukkit.getScheduler().runTask(plugin, () -> spawnBannerFrames(banner));
            }
            return success;
        });
    }

    public void spawnBannerFrames(MediaBanner banner) {
        World world = banner.getWorld();
        if (world == null) return;

        removeBannerFrames(banner);

        plugin.getImageRenderer().loadAndProcessTiles(banner.getSource(), banner.getWidthTiles(), banner.getHeightTiles(), plugin.getDataFolder())
                .thenAccept(tiles -> Bukkit.getScheduler().runTask(plugin, () -> {
                    int w = banner.getWidthTiles();
                    int h = banner.getHeightTiles();

                    for (int tileX = 0; tileX < w; tileX++) {
                        for (int tileY = 0; tileY < h; tileY++) {
                            Location frameLoc = calculateTileLocation(banner, tileX, tileY);
                            if (frameLoc == null) continue;

                            try {
                                ItemFrame frame = world.spawn(frameLoc, ItemFrame.class, f -> {
                                    f.setFacingDirection(banner.getFacing(), true);
                                    f.setVisible(false);
                                    f.setFixed(true);
                                    f.setSilent(true);
                                    f.setInvulnerable(true);
                                    f.setPersistent(true);
                                });

                                MapView mapView = Bukkit.createMap(world);
                                mapView.setScale(MapView.Scale.NORMAL);
                                mapView.setTrackingPosition(false);
                                mapView.setUnlimitedTracking(false);
                                mapView.setLocked(true);
                                mapView.getRenderers().clear();
                                mapView.addRenderer(new ImageRenderer.CustomMapRenderer(tiles[tileX][tileY]));

                                ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                                MapMeta meta = (MapMeta) mapItem.getItemMeta();
                                if (meta != null) {
                                    meta.setMapView(mapView);
                                    mapItem.setItemMeta(meta);
                                }

                                frame.setItem(mapItem, false);
                                banner.getItemFrameUuids().add(frame.getUniqueId());
                                banner.getMapIds().add(mapView.getId());
                                frameToBannerMap.put(frame.getUniqueId(), banner);
                                registeredMapViews.put(mapView.getId(), mapView);

                                for (Player p : world.getPlayers()) {
                                    p.sendMap(mapView);
                                }
                            } catch (Exception e) {
                                plugin.getLogger().warning("Could not place ItemFrame at " + frameLoc + ": " + e.getMessage());
                            }
                        }
                    }
                })).exceptionally(ex -> {
                    plugin.getLogger().severe("Error rendering banner tiles for " + banner.getId() + ": " + ex.getMessage());
                    return null;
                });
    }

    public void sendAllBannersToPlayer(Player player) {
        if (player == null || !player.isOnline()) return;
        World playerWorld = player.getWorld();

        for (MediaBanner banner : banners.values()) {
            if (playerWorld.getName().equalsIgnoreCase(banner.getWorldName())) {
                for (int mapId : banner.getMapIds()) {
                    MapView mv = registeredMapViews.get(mapId);
                    if (mv == null) mv = Bukkit.getMap(mapId);
                    if (mv != null) {
                        player.sendMap(mv);
                    }
                }
            }
        }
    }

    public Location calculateTileLocation(MediaBanner banner, int tileX, int tileY) {
        World world = banner.getWorld();
        if (world == null) return null;

        double x = banner.getX();
        double y = banner.getY() - tileY;
        double z = banner.getZ();

        switch (banner.getFacing()) {
            case NORTH -> x -= tileX;
            case SOUTH -> x += tileX;
            case WEST -> z += tileX;
            case EAST -> z -= tileX;
            default -> x += tileX;
        }

        return new Location(world, x, y, z);
    }

    public void removeBannerFrames(MediaBanner banner) {
        World world = banner.getWorld();
        if (world == null) return;

        for (UUID uuid : banner.getItemFrameUuids()) {
            Entity ent = world.getEntity(uuid);
            if (ent != null) ent.remove();
            frameToBannerMap.remove(uuid);
        }
        for (int mapId : banner.getMapIds()) {
            registeredMapViews.remove(mapId);
        }
        banner.getItemFrameUuids().clear();
        banner.getMapIds().clear();
    }

    public boolean deleteBanner(String id) {
        MediaBanner banner = banners.remove(id.toLowerCase());
        if (banner != null) {
            removeBannerFrames(banner);
            CompletableFuture.runAsync(() -> {
                try (Connection conn = dataSource.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM media_banners WHERE id = ?")) {
                    ps.setString(1, banner.getId());
                    ps.executeUpdate();
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to delete banner from DB: " + e.getMessage());
                }
            });
            return true;
        }
        return false;
    }

    public MediaBanner getBanner(String id) {
        if (id == null) return null;
        return banners.get(id.toLowerCase());
    }

    public MediaBanner getBannerByFrame(UUID frameUuid) {
        return frameToBannerMap.get(frameUuid);
    }

    public Collection<MediaBanner> getBanners() {
        return Collections.unmodifiableCollection(banners.values());
    }

    public Collection<MediaBanner> getAllBanners() {
        return getBanners();
    }

    public void reload() {
        loadAllBanners();
    }

    public void shutdown() {
        for (MediaBanner b : banners.values()) {
            removeBannerFrames(b);
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
