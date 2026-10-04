package com.apexsions.core.database;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Asynchronous repository for PlayerData persistence supporting PostgreSQL and SQLite.
 */
public class PlayerRepository {

    private final ApexsionsCorePlugin plugin;
    private final DatabaseManager db;

    public PlayerRepository(ApexsionsCorePlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public CompletableFuture<Optional<PlayerData>> findByUuid(UUID uuid) {
        return db.supplyAsync(() -> {
            String sql = "SELECT uuid, username, level, xp, region_id, claimed_rewards, created_at, updated_at FROM players WHERE uuid = ?";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                if (db.isUsingFallback()) {
                    ps.setString(1, uuid.toString());
                } else {
                    ps.setObject(1, uuid);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Object uuidObj = rs.getObject("uuid");
                        UUID pUuid = (uuidObj instanceof UUID u) ? u : UUID.fromString(uuidObj.toString());
                        String username = rs.getString("username");
                        int level = rs.getInt("level");
                        long xp = rs.getLong("xp");

                        Object regObj = rs.getObject("region_id");
                        UUID regionId = null;
                        if (regObj != null && !regObj.toString().trim().isEmpty()) {
                            regionId = (regObj instanceof UUID u) ? u : UUID.fromString(regObj.toString().trim());
                        }

                        String claimedRewardsStr = rs.getString("claimed_rewards");

                        Instant createdAt = DatabaseManager.readInstant(rs, "created_at", Instant.now());
                        Instant updatedAt = DatabaseManager.readInstant(rs, "updated_at", Instant.now());

                        PlayerData data = new PlayerData(pUuid, username, level, xp, regionId, createdAt, updatedAt);
                        if (claimedRewardsStr != null) {
                            data.setClaimedRewardsFromString(claimedRewardsStr);
                        }
                        return Optional.of(data);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed finding player data for UUID: " + uuid, e);
                throw new java.util.concurrent.CompletionException(e);
            }
            return Optional.empty();
        });
    }

    /**
     * Inserts a new default PlayerData row only if no row for {@code uuid} exists yet.
     * Prevents accidental overwrite of existing level, XP, or kingdom data.
     */
    public CompletableFuture<Boolean> insertIfAbsent(PlayerData data) {
        return db.supplyAsync(() -> {
            String sql = "INSERT INTO players (uuid, username, level, xp, region_id, claimed_rewards, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (uuid) DO NOTHING";

            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                if (db.isUsingFallback()) {
                    ps.setString(1, data.getUuid().toString());
                    ps.setString(2, data.getUsername());
                    ps.setInt(3, data.getLevel());
                    ps.setLong(4, data.getXp());
                    if (data.getRegionId() != null) {
                        ps.setString(5, data.getRegionId().toString());
                    } else {
                        ps.setNull(5, Types.VARCHAR);
                    }
                    ps.setString(6, data.getClaimedRewardsString());
                    ps.setTimestamp(7, Timestamp.from(data.getCreatedAt()));
                    ps.setTimestamp(8, Timestamp.from(data.getUpdatedAt()));
                } else {
                    ps.setObject(1, data.getUuid());
                    ps.setString(2, data.getUsername());
                    ps.setInt(3, data.getLevel());
                    ps.setLong(4, data.getXp());
                    ps.setObject(5, data.getRegionId());
                    ps.setString(6, data.getClaimedRewardsString());
                    ps.setTimestamp(7, Timestamp.from(data.getCreatedAt()));
                    ps.setTimestamp(8, Timestamp.from(data.getUpdatedAt()));
                }
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed inserting initial player data for UUID: " + data.getUuid(), e);
                return false;
            }
        });
    }

    public CompletableFuture<Void> save(PlayerData data) {
        return db.runAsync(() -> {
            String sql = "INSERT INTO players (uuid, username, level, xp, region_id, claimed_rewards, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (uuid) DO UPDATE SET " +
                    "username = EXCLUDED.username, " +
                    "level = EXCLUDED.level, " +
                    "xp = EXCLUDED.xp, " +
                    "region_id = EXCLUDED.region_id, " +
                    "claimed_rewards = EXCLUDED.claimed_rewards, " +
                    "updated_at = EXCLUDED.updated_at";

            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                if (db.isUsingFallback()) {
                    ps.setString(1, data.getUuid().toString());
                    ps.setString(2, data.getUsername());
                    ps.setInt(3, data.getLevel());
                    ps.setLong(4, data.getXp());
                    if (data.getRegionId() != null) {
                        ps.setString(5, data.getRegionId().toString());
                    } else {
                        ps.setNull(5, Types.VARCHAR);
                    }
                    ps.setString(6, data.getClaimedRewardsString());
                    ps.setTimestamp(7, Timestamp.from(data.getCreatedAt()));
                    ps.setTimestamp(8, Timestamp.from(data.getUpdatedAt()));
                } else {
                    ps.setObject(1, data.getUuid());
                    ps.setString(2, data.getUsername());
                    ps.setInt(3, data.getLevel());
                    ps.setLong(4, data.getXp());
                    ps.setObject(5, data.getRegionId());
                    ps.setString(6, data.getClaimedRewardsString());
                    ps.setTimestamp(7, Timestamp.from(data.getCreatedAt()));
                    ps.setTimestamp(8, Timestamp.from(data.getUpdatedAt()));
                }
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed saving player data for UUID: " + data.getUuid(), e);
            }
        });
    }

    public CompletableFuture<Void> updateRegion(UUID playerUuid, UUID regionId) {
        return db.runAsync(() -> {
            String sql = "UPDATE players SET region_id = ?, updated_at = ? WHERE uuid = ?";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                if (db.isUsingFallback()) {
                    if (regionId != null) {
                        ps.setString(1, regionId.toString());
                    } else {
                        ps.setNull(1, Types.VARCHAR);
                    }
                    ps.setTimestamp(2, Timestamp.from(Instant.now()));
                    ps.setString(3, playerUuid.toString());
                } else {
                    ps.setObject(1, regionId);
                    ps.setTimestamp(2, Timestamp.from(Instant.now()));
                    ps.setObject(3, playerUuid);
                }
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed updating region for player: " + playerUuid, e);
            }
        });
    }

    public CompletableFuture<java.util.List<PlayerData>> getTopPlayersByRegionAsync(UUID regionId, int limit) {
        return db.supplyAsync(() -> {
            java.util.List<PlayerData> list = new java.util.ArrayList<>();
            int candidateLimit = Math.max(50, limit * 3);
            String sql = "SELECT uuid, username, level, xp, region_id, claimed_rewards, created_at, updated_at " +
                    "FROM players WHERE region_id = ? ORDER BY level DESC, xp DESC LIMIT ?";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                if (db.isUsingFallback()) {
                    ps.setString(1, regionId.toString());
                } else {
                    ps.setObject(1, regionId);
                }
                ps.setInt(2, candidateLimit);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object uuidObj = rs.getObject("uuid");
                        UUID pUuid = (uuidObj instanceof UUID u) ? u : UUID.fromString(uuidObj.toString());

                        // Exclude server admins / staff from leaderboard
                        if ((plugin.getApi() != null && plugin.getApi().isLeaderboardExempt(pUuid))
                                || (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isStaffOrAdmin(pUuid))) {
                            continue;
                        }

                        String username = rs.getString("username");
                        int level = rs.getInt("level");
                        long xp = rs.getLong("xp");

                        Object regObj = rs.getObject("region_id");
                        UUID rId = null;
                        if (regObj != null && !regObj.toString().trim().isEmpty()) {
                            rId = (regObj instanceof UUID u) ? u : UUID.fromString(regObj.toString().trim());
                        }

                        String claimedRewardsStr = rs.getString("claimed_rewards");
                        Instant createdAt = DatabaseManager.readInstant(rs, "created_at", Instant.now());
                        Instant updatedAt = DatabaseManager.readInstant(rs, "updated_at", Instant.now());

                        PlayerData data = new PlayerData(pUuid, username, level, xp, rId, createdAt, updatedAt);
                        if (claimedRewardsStr != null) {
                            data.setClaimedRewardsFromString(claimedRewardsStr);
                        }
                        list.add(data);
                        if (list.size() >= limit) {
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed fetching top players for region: " + regionId, e);
            }
            return list;
        });
    }

    public CompletableFuture<Integer> getPlayerRankInRegionAsync(UUID playerUuid, UUID regionId) {
        return db.supplyAsync(() -> {
            if ((plugin.getApi() != null && plugin.getApi().isLeaderboardExempt(playerUuid))
                    || (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isStaffOrAdmin(playerUuid))) {
                return -1; // Admin / Staff is exempt from public leaderboards
            }

            String sql = "SELECT p1.uuid FROM players p1 " +
                    "INNER JOIN players p2 ON p2.uuid = ? " +
                    "WHERE p1.region_id = ? AND (p1.level > p2.level OR (p1.level = p2.level AND p1.xp > p2.xp))";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                if (db.isUsingFallback()) {
                    ps.setString(1, playerUuid.toString());
                    ps.setString(2, regionId.toString());
                } else {
                    ps.setObject(1, playerUuid);
                    ps.setObject(2, regionId);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    int rank = 1;
                    while (rs.next()) {
                        Object uuidObj = rs.getObject("uuid");
                        UUID higherUuid = (uuidObj instanceof UUID u) ? u : UUID.fromString(uuidObj.toString());
                        if ((plugin.getApi() != null && plugin.getApi().isLeaderboardExempt(higherUuid))
                                || (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isStaffOrAdmin(higherUuid))) {
                            continue; // Skip admin from rank position counting
                        }
                        rank++;
                    }
                    return rank;
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed calculating rank for player: " + playerUuid, e);
            }
            return 1;
        });
    }

    public CompletableFuture<Integer> getTotalPlayersInRegionAsync(UUID regionId) {
        return db.supplyAsync(() -> {
            String sql = "SELECT uuid FROM players WHERE region_id = ?";
            int count = 0;
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                if (db.isUsingFallback()) {
                    ps.setString(1, regionId.toString());
                } else {
                    ps.setObject(1, regionId);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object uuidObj = rs.getObject("uuid");
                        UUID pUuid = (uuidObj instanceof UUID u) ? u : UUID.fromString(uuidObj.toString());
                        if ((plugin.getApi() != null && plugin.getApi().isLeaderboardExempt(pUuid))
                                || (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isStaffOrAdmin(pUuid))) {
                            continue;
                        }
                        count++;
                    }
                }
                return count;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed counting players for region: " + regionId, e);
            }
            return 0;
        });
    }

    public CompletableFuture<java.util.List<PlayerData>> getAllPlayersAsync() {
        return db.supplyAsync(() -> {
            java.util.List<PlayerData> list = new java.util.ArrayList<>();
            String sql = "SELECT uuid, username, level, xp, region_id, claimed_rewards, created_at, updated_at FROM players";
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object uuidObj = rs.getObject("uuid");
                    UUID pUuid = (uuidObj instanceof UUID u) ? u : UUID.fromString(uuidObj.toString());
                    String username = rs.getString("username");
                    int level = rs.getInt("level");
                    long xp = rs.getLong("xp");
                    Object regObj = rs.getObject("region_id");
                    UUID rId = (regObj != null && !regObj.toString().trim().isEmpty())
                            ? ((regObj instanceof UUID u) ? u : UUID.fromString(regObj.toString().trim()))
                            : null;
                    String claimed = rs.getString("claimed_rewards");
                    Instant createdAt = DatabaseManager.readInstant(rs, "created_at", Instant.now());
                    Instant updatedAt = DatabaseManager.readInstant(rs, "updated_at", Instant.now());
                    PlayerData data = new PlayerData(pUuid, username, level, xp, rId, createdAt, updatedAt);
                    if (claimed != null) {
                        data.setClaimedRewardsFromString(claimed);
                    }
                    list.add(data);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed fetching all players from repository", e);
            }
            return list;
        });
    }
}

