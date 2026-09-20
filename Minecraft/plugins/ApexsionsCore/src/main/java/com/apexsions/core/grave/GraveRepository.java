package com.apexsions.core.grave;

import com.apexsions.core.database.DatabaseManager;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Async repository for player graves. Uses parameterized SQL only and is
 * compatible with both PostgreSQL and SQLite / H2.
 */
public class GraveRepository {

    private final DatabaseManager databaseManager;
    private final Logger logger;

    public GraveRepository(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public CompletableFuture<Void> save(GraveRecord grave) {
        return databaseManager.runAsync(() -> {
            String sql = "INSERT INTO apexsions_graves (id, owner_uuid, owner_name, world, x, y, z, yaw, pitch, " +
                    "items_data, xp, cause, created_at, expires_at, collected) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(id) DO UPDATE SET owner_name = EXCLUDED.owner_name, world = EXCLUDED.world, " +
                    "x = EXCLUDED.x, y = EXCLUDED.y, z = EXCLUDED.z, yaw = EXCLUDED.yaw, pitch = EXCLUDED.pitch, " +
                    "items_data = EXCLUDED.items_data, xp = EXCLUDED.xp, cause = EXCLUDED.cause, " +
                    "expires_at = EXCLUDED.expires_at, collected = EXCLUDED.collected";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, grave.getId());
                ps.setString(2, grave.getOwnerUuid().toString());
                ps.setString(3, grave.getOwnerName());
                ps.setString(4, grave.getWorldName());
                ps.setDouble(5, grave.getX());
                ps.setDouble(6, grave.getY());
                ps.setDouble(7, grave.getZ());
                ps.setFloat(8, grave.getYaw());
                ps.setFloat(9, grave.getPitch());
                ps.setString(10, GraveItems.serialize(grave.getItems()));
                ps.setInt(11, grave.getXp());
                ps.setString(12, grave.getCause());
                ps.setTimestamp(13, new Timestamp(grave.getCreatedAt()));
                ps.setTimestamp(14, grave.getExpiresAt() > 0 ? new Timestamp(grave.getExpiresAt()) : null);
                ps.setBoolean(15, grave.isCollected());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed saving grave " + grave.getId(), e);
            }
        });
    }

    public CompletableFuture<Void> markCollected(String graveId) {
        return databaseManager.runAsync(() -> {
            String sql = "UPDATE apexsions_graves SET collected = ? WHERE id = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, true);
                ps.setString(2, graveId);
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed marking grave " + graveId + " collected", e);
            }
        });
    }

    public CompletableFuture<Void> delete(String graveId) {
        return databaseManager.runAsync(() -> {
            String sql = "DELETE FROM apexsions_graves WHERE id = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, graveId);
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed deleting grave " + graveId, e);
            }
        });
    }

    public CompletableFuture<List<GraveRecord>> loadActive() {
        return databaseManager.supplyAsync(() -> {
            List<GraveRecord> list = new ArrayList<>();
            String sql = "SELECT id, owner_uuid, owner_name, world, x, y, z, yaw, pitch, items_data, xp, cause, " +
                    "created_at, expires_at FROM apexsions_graves WHERE collected = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, false);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(readRow(rs));
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed loading active graves", e);
            }
            return list;
        });
    }

    private GraveRecord readRow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        UUID owner = UUID.fromString(rs.getString("owner_uuid"));
        String ownerName = rs.getString("owner_name");
        String world = rs.getString("world");
        double x = rs.getDouble("x");
        double y = rs.getDouble("y");
        double z = rs.getDouble("z");
        float yaw = rs.getFloat("yaw");
        float pitch = rs.getFloat("pitch");
        int xp = rs.getInt("xp");
        String cause = rs.getString("cause");

        List<ItemStack> items;
        try {
            items = GraveItems.deserialize(rs.getString("items_data"));
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Corrupt items_data for grave " + id + "; treating as empty", ex);
            items = new ArrayList<>();
        }

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp expires = rs.getTimestamp("expires_at");
        return new GraveRecord(id, owner, ownerName, world, x, y, z, yaw, pitch, items, xp, cause,
                created != null ? created.getTime() : System.currentTimeMillis(),
                expires != null ? expires.getTime() : 0L, false);
    }
}