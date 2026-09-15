package com.apexsions.core.moderation;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.database.DatabaseManager;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Persistence layer for player bans supporting PostgreSQL and SQLite.
 */
public class BanRepository {

    private final ApexsionsCorePlugin plugin;
    private final DatabaseManager databaseManager;

    public BanRepository(ApexsionsCorePlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public CompletableFuture<Void> insertBan(BanRecord record) {
        return databaseManager.runAsync(() -> {
            String sql = "INSERT INTO apexsions_bans (id, player_uuid, player_name, ip_address, banned_by, reason, ban_type, banned_at, expires_at, active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, record.getId().toString());
                ps.setString(2, record.getPlayerUuid().toString());
                ps.setString(3, record.getPlayerName());
                ps.setString(4, record.getIpAddress());
                ps.setString(5, record.getBannedBy());
                ps.setString(6, record.getReason());
                ps.setString(7, record.getBanType().name());
                ps.setTimestamp(8, Timestamp.from(record.getBannedAt()));
                if (record.getExpiresAt() != null) {
                    ps.setTimestamp(9, Timestamp.from(record.getExpiresAt()));
                } else {
                    ps.setNull(9, Types.TIMESTAMP);
                }
                ps.setBoolean(10, record.isActive());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to insert ban record into database: " + record.getPlayerName(), e);
            }
        });
    }

    public CompletableFuture<Void> updateUnban(UUID banId, String unbannedBy, String unbanReason) {
        return databaseManager.runAsync(() -> {
            String sql = "UPDATE apexsions_bans SET active = ?, unbanned_by = ?, unban_reason = ?, unbanned_at = ? WHERE id = ?;";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, false);
                ps.setString(2, unbannedBy);
                ps.setString(3, unbanReason);
                ps.setTimestamp(4, Timestamp.from(Instant.now()));
                ps.setString(5, banId.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to update unban in database for ban ID: " + banId, e);
            }
        });
    }

    public CompletableFuture<List<BanRecord>> loadActiveBans() {
        return databaseManager.supplyAsync(() -> {
            List<BanRecord> list = new ArrayList<>();
            String sql = "SELECT * FROM apexsions_bans WHERE active = ?;";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, true);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        BanRecord record = mapRow(rs);
                        if (record != null && record.isActive()) {
                            list.add(record);
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed loading active bans from database", e);
            }
            return list;
        });
    }

    public CompletableFuture<List<BanRecord>> getBanHistory(UUID playerUuid) {
        return databaseManager.supplyAsync(() -> {
            List<BanRecord> list = new ArrayList<>();
            String sql = "SELECT * FROM apexsions_bans WHERE player_uuid = ? ORDER BY banned_at DESC LIMIT 20;";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        BanRecord record = mapRow(rs);
                        if (record != null) {
                            list.add(record);
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed querying ban history for: " + playerUuid, e);
            }
            return list;
        });
    }

    private BanRecord mapRow(ResultSet rs) throws SQLException {
        try {
            UUID id = UUID.fromString(rs.getString("id"));
            UUID playerUuid = UUID.fromString(rs.getString("player_uuid"));
            String playerName = rs.getString("player_name");
            String ipAddress = rs.getString("ip_address");
            String bannedBy = rs.getString("banned_by");
            String reason = rs.getString("reason");
            String typeStr = rs.getString("ban_type");
            BanRecord.BanType type = BanRecord.BanType.NAME;
            if (typeStr != null) {
                try {
                    type = BanRecord.BanType.valueOf(typeStr.toUpperCase());
                } catch (IllegalArgumentException ignored) {}
            }
            Timestamp bannedAtTs = rs.getTimestamp("banned_at");
            Instant bannedAt = bannedAtTs != null ? bannedAtTs.toInstant() : Instant.now();

            Timestamp expiresAtTs = rs.getTimestamp("expires_at");
            Instant expiresAt = expiresAtTs != null ? expiresAtTs.toInstant() : null;

            boolean active = rs.getBoolean("active");
            String unbannedBy = rs.getString("unbanned_by");
            String unbanReason = rs.getString("unban_reason");
            Timestamp unbannedAtTs = rs.getTimestamp("unbanned_at");
            Instant unbannedAt = unbannedAtTs != null ? unbannedAtTs.toInstant() : null;

            return new BanRecord(id, playerUuid, playerName, ipAddress, bannedBy, reason, type,
                    bannedAt, expiresAt, active, unbannedBy, unbanReason, unbannedAt);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Corrupt ban row encountered in database: " + e.getMessage());
            return null;
        }
    }
}
