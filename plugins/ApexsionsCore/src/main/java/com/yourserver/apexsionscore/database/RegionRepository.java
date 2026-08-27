package com.yourserver.apexsionscore.database;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.region.Region;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Asynchronous repository for Region persistence supporting PostgreSQL and SQLite.
 */
public class RegionRepository {

    private final ApexsionsCorePlugin plugin;
    private final DatabaseManager db;

    public RegionRepository(ApexsionsCorePlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public CompletableFuture<List<Region>> findAll() {
        return db.supplyAsync(() -> {
            List<Region> list = new ArrayList<>();
            String sql = "SELECT id, key, display_name, world_name, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch, enabled FROM regions";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object idObj = rs.getObject("id");
                    UUID id = (idObj instanceof UUID u) ? u : UUID.fromString(idObj.toString());
                    String key = rs.getString("key");
                    String displayName = rs.getString("display_name");
                    String worldName = rs.getString("world_name");
                    Double spawnX = rs.getObject("spawn_x") != null ? rs.getDouble("spawn_x") : null;
                    Double spawnY = rs.getObject("spawn_y") != null ? rs.getDouble("spawn_y") : null;
                    Double spawnZ = rs.getObject("spawn_z") != null ? rs.getDouble("spawn_z") : null;
                    Float spawnYaw = rs.getObject("spawn_yaw") != null ? rs.getFloat("spawn_yaw") : null;
                    Float spawnPitch = rs.getObject("spawn_pitch") != null ? rs.getFloat("spawn_pitch") : null;
                    boolean enabled = rs.getBoolean("enabled");

                    list.add(new Region(id, key, displayName, worldName, spawnX, spawnY, spawnZ, spawnYaw, spawnPitch, enabled));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed loading regions from database", e);
            }
            return list;
        });
    }

    public CompletableFuture<Void> save(Region region) {
        return db.runAsync(() -> {
            String sql = "INSERT INTO regions (id, key, display_name, world_name, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch, enabled) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (key) DO UPDATE SET " +
                    "display_name = EXCLUDED.display_name, " +
                    "world_name = EXCLUDED.world_name, " +
                    "spawn_x = EXCLUDED.spawn_x, " +
                    "spawn_y = EXCLUDED.spawn_y, " +
                    "spawn_z = EXCLUDED.spawn_z, " +
                    "spawn_yaw = EXCLUDED.spawn_yaw, " +
                    "spawn_pitch = EXCLUDED.spawn_pitch, " +
                    "enabled = EXCLUDED.enabled";

            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                if (db.isUsingFallback()) {
                    ps.setString(1, region.getId().toString());
                } else {
                    ps.setObject(1, region.getId());
                }
                ps.setString(2, region.getKey());
                ps.setString(3, region.getDisplayName());
                ps.setString(4, region.getWorldName());
                ps.setObject(5, region.getSpawnX());
                ps.setObject(6, region.getSpawnY());
                ps.setObject(7, region.getSpawnZ());
                ps.setObject(8, region.getSpawnYaw());
                ps.setObject(9, region.getSpawnPitch());
                ps.setBoolean(10, region.isEnabled());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed saving region: " + region.getKey(), e);
            }
        });
    }
}
