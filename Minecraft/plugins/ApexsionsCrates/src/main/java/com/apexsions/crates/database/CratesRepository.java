package com.apexsions.crates.database;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.crate.CrateLocation;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class CratesRepository {

    private final ApexsionsCratesPlugin plugin;
    private final File dbFile;
    private Connection connection;
    private final Object dbLock = new Object();

    public CratesRepository(ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
        String fileName = plugin.getConfig().getString("database.sqlite.file", "crates_data.db");
        this.dbFile = new File(plugin.getDataFolder(), fileName);
    }

    public void init() {
        synchronized (dbLock) {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            try {
                Class.forName("org.sqlite.JDBC");
                this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

                try (Statement stmt = connection.createStatement()) {
                    // 1. Virtual keys table
                    stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS crate_virtual_keys (
                            uuid VARCHAR(36) NOT NULL,
                            key_id VARCHAR(32) NOT NULL,
                            amount INTEGER NOT NULL DEFAULT 0,
                            PRIMARY KEY (uuid, key_id)
                        );
                    """);

                    // 2. Player stats & milestones
                    stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS crate_player_stats (
                            uuid VARCHAR(36) NOT NULL,
                            crate_id VARCHAR(32) NOT NULL,
                            open_count INTEGER NOT NULL DEFAULT 0,
                            PRIMARY KEY (uuid, crate_id)
                        );
                    """);

                    // 3. Physical crate block locations
                    stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS crate_locations (
                            world VARCHAR(64) NOT NULL,
                            x INTEGER NOT NULL,
                            y INTEGER NOT NULL,
                            z INTEGER NOT NULL,
                            crate_id VARCHAR(32) NOT NULL,
                            PRIMARY KEY (world, x, y, z)
                        );
                    """);
                }

                plugin.getLogger().info("SQLite Database initialized successfully for ApexsionsCrates.");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQLite database for ApexsionsCrates: " + e.getMessage(), e);
            }
        }
    }

    public void close() {
        synchronized (dbLock) {
            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException ignored) {}
            }
        }
    }

    // --- Virtual Keys Operations ---

    public CompletableFuture<Integer> getVirtualKeys(UUID uuid, String keyId) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (dbLock) {
                String sql = "SELECT amount FROM crate_virtual_keys WHERE uuid = ? AND key_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, keyId.toLowerCase());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("amount");
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error getting virtual keys: " + e.getMessage(), e);
                }
                return 0;
            }
        });
    }

    public CompletableFuture<Map<String, Integer>> getAllVirtualKeys(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Integer> result = new HashMap<>();
            synchronized (dbLock) {
                String sql = "SELECT key_id, amount FROM crate_virtual_keys WHERE uuid = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, uuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            result.put(rs.getString("key_id"), rs.getInt("amount"));
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error getting all virtual keys: " + e.getMessage(), e);
                }
            }
            return result;
        });
    }

    public CompletableFuture<Void> setVirtualKeys(UUID uuid, String keyId, int amount) {
        return CompletableFuture.runAsync(() -> {
            synchronized (dbLock) {
                String sql = """
                    INSERT INTO crate_virtual_keys (uuid, key_id, amount)
                    VALUES (?, ?, ?)
                    ON CONFLICT(uuid, key_id) DO UPDATE SET amount = excluded.amount;
                """;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, keyId.toLowerCase());
                    ps.setInt(3, Math.max(0, amount));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error setting virtual keys: " + e.getMessage(), e);
                }
            }
        });
    }

    public CompletableFuture<Void> addVirtualKeys(UUID uuid, String keyId, int amount) {
        return CompletableFuture.runAsync(() -> {
            synchronized (dbLock) {
                String sql = """
                    INSERT INTO crate_virtual_keys (uuid, key_id, amount)
                    VALUES (?, ?, ?)
                    ON CONFLICT(uuid, key_id) DO UPDATE SET amount = amount + excluded.amount;
                """;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, keyId.toLowerCase());
                    ps.setInt(3, Math.max(0, amount));
                    ps.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error adding virtual keys: " + e.getMessage(), e);
                }
            }
        });
    }

    public CompletableFuture<Boolean> takeVirtualKeys(UUID uuid, String keyId, int amount) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (dbLock) {
                try {
                    String checkSql = "SELECT amount FROM crate_virtual_keys WHERE uuid = ? AND key_id = ?";
                    int current = 0;
                    try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
                        ps.setString(1, uuid.toString());
                        ps.setString(2, keyId.toLowerCase());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                current = rs.getInt("amount");
                            }
                        }
                    }

                    if (current < amount) return false;

                    String updateSql = "UPDATE crate_virtual_keys SET amount = amount - ? WHERE uuid = ? AND key_id = ?";
                    try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                        ps.setInt(1, amount);
                        ps.setString(2, uuid.toString());
                        ps.setString(3, keyId.toLowerCase());
                        ps.executeUpdate();
                    }
                    return true;
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error taking virtual keys: " + e.getMessage(), e);
                    return false;
                }
            }
        });
    }

    // --- Crate Stats & Milestones ---

    public CompletableFuture<Integer> getCrateOpenCount(UUID uuid, String crateId) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (dbLock) {
                String sql = "SELECT open_count FROM crate_player_stats WHERE uuid = ? AND crate_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, crateId.toLowerCase());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("open_count");
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error getting crate open count: " + e.getMessage(), e);
                }
                return 0;
            }
        });
    }

    public CompletableFuture<Integer> incrementCrateOpenCount(UUID uuid, String crateId) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (dbLock) {
                String sql = """
                    INSERT INTO crate_player_stats (uuid, crate_id, open_count)
                    VALUES (?, ?, 1)
                    ON CONFLICT(uuid, crate_id) DO UPDATE SET open_count = open_count + 1;
                """;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, crateId.toLowerCase());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error incrementing crate open count: " + e.getMessage(), e);
                }

                // Return updated count
                String checkSql = "SELECT open_count FROM crate_player_stats WHERE uuid = ? AND crate_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, crateId.toLowerCase());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("open_count");
                        }
                    }
                } catch (SQLException ignored) {}
                return 1;
            }
        });
    }

    // --- Crate Locations ---

    public CompletableFuture<List<CrateLocation>> loadLocations() {
        return CompletableFuture.supplyAsync(() -> {
            List<CrateLocation> list = new ArrayList<>();
            synchronized (dbLock) {
                String sql = "SELECT world, x, y, z, crate_id FROM crate_locations";
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        list.add(new CrateLocation(
                                rs.getString("world"),
                                rs.getInt("x"),
                                rs.getInt("y"),
                                rs.getInt("z"),
                                rs.getString("crate_id")
                        ));
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error loading crate locations: " + e.getMessage(), e);
                }
            }
            return list;
        });
    }

    public CompletableFuture<Void> saveLocation(CrateLocation loc) {
        return CompletableFuture.runAsync(() -> {
            synchronized (dbLock) {
                String sql = "INSERT OR REPLACE INTO crate_locations (world, x, y, z, crate_id) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, loc.getWorld());
                    ps.setInt(2, loc.getX());
                    ps.setInt(3, loc.getY());
                    ps.setInt(4, loc.getZ());
                    ps.setString(5, loc.getCrateId().toLowerCase());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error saving crate location: " + e.getMessage(), e);
                }
            }
        });
    }

    public CompletableFuture<Void> deleteLocation(CrateLocation loc) {
        return CompletableFuture.runAsync(() -> {
            synchronized (dbLock) {
                String sql = "DELETE FROM crate_locations WHERE world = ? AND x = ? AND y = ? AND z = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, loc.getWorld());
                    ps.setInt(2, loc.getX());
                    ps.setInt(3, loc.getY());
                    ps.setInt(4, loc.getZ());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error deleting crate location: " + e.getMessage(), e);
                }
            }
        });
    }
}
