package com.apexsions.core.bounty;

import com.apexsions.core.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Async repository for player bounties and bounty-hunter statistics.
 * All queries are parameterized and compatible with PostgreSQL, SQLite and H2.
 */
public class BountyRepository {

    public record HunterStat(UUID uuid, String name, double totalClaimed, int kills) {
    }

    private final DatabaseManager databaseManager;
    private final Logger logger;

    public BountyRepository(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    /** Loads every still-active bounty contribution row. */
    public CompletableFuture<List<Bounty>> loadActiveBounties() {
        return databaseManager.supplyAsync(() -> {
            List<Bounty> list = new ArrayList<>();
            String sql = "SELECT id, target_uuid, target_name, placer_uuid, placer_name, amount, active, " +
                    "claimed_by, claimed_at FROM apexsions_bounties WHERE active = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, true);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String claimedBy = rs.getString("claimed_by");
                        Timestamp claimedAt = rs.getTimestamp("claimed_at");
                        list.add(new Bounty(
                                rs.getString("id"),
                                UUID.fromString(rs.getString("target_uuid")),
                                rs.getString("target_name"),
                                UUID.fromString(rs.getString("placer_uuid")),
                                rs.getString("placer_name"),
                                rs.getDouble("amount"),
                                rs.getBoolean("active"),
                                claimedBy != null ? UUID.fromString(claimedBy) : null,
                                claimedAt != null ? claimedAt.getTime() : 0L
                        ));
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed loading active bounties", e);
            }
            return list;
        });
    }

    /** Inserts a single contribution row. */
    public CompletableFuture<Void> insertBounty(Bounty bounty) {
        return databaseManager.runAsync(() -> {
            String sql = "INSERT INTO apexsions_bounties (id, target_uuid, target_name, placer_uuid, " +
                    "placer_name, amount, created_at, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, bounty.id());
                ps.setString(2, bounty.targetUuid().toString());
                ps.setString(3, bounty.targetName());
                ps.setString(4, bounty.placerUuid().toString());
                ps.setString(5, bounty.placerName());
                ps.setDouble(6, bounty.amount());
                ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
                ps.setBoolean(8, true);
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed inserting bounty on " + bounty.targetName(), e);
            }
        });
    }

    /** Marks all active contributions on a target as claimed/removed. */
    public CompletableFuture<Void> deactivateTarget(UUID targetUuid, UUID claimedBy) {
        return databaseManager.runAsync(() -> {
            String sql = "UPDATE apexsions_bounties SET active = ?, claimed_by = ?, claimed_at = ? " +
                    "WHERE target_uuid = ? AND active = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, false);
                if (claimedBy != null) {
                    ps.setString(2, claimedBy.toString());
                } else {
                    ps.setNull(2, java.sql.Types.VARCHAR);
                }
                ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                ps.setString(4, targetUuid.toString());
                ps.setBoolean(5, true);
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed deactivating bounties for " + targetUuid, e);
            }
        });
    }

    /** Increments a hunter's lifetime collected total and kill count. */
    public CompletableFuture<Void> addHunterClaim(UUID hunterUuid, String hunterName, double amount) {
        return databaseManager.runAsync(() -> {
            String sql = "INSERT INTO apexsions_bounty_hunters (hunter_uuid, hunter_name, total_claimed, " +
                    "kills, updated_at) VALUES (?, ?, ?, ?, ?) " +
                    "ON CONFLICT(hunter_uuid) DO UPDATE SET " +
                    "hunter_name = EXCLUDED.hunter_name, " +
                    "total_claimed = apexsions_bounty_hunters.total_claimed + EXCLUDED.total_claimed, " +
                    "kills = apexsions_bounty_hunters.kills + 1, " +
                    "updated_at = EXCLUDED.updated_at";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, hunterUuid.toString());
                ps.setString(2, hunterName);
                ps.setDouble(3, amount);
                ps.setInt(4, 1);
                ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                ps.executeUpdate();
            } catch (SQLException e) {
                // SQLite lacks ON CONFLICT for some legacy builds — fall back to manual upsert.
                fallbackHunterClaim(hunterUuid, hunterName, amount);
            }
        });
    }

    private void fallbackHunterClaim(UUID hunterUuid, String hunterName, double amount) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement update = conn.prepareStatement(
                     "UPDATE apexsions_bounty_hunters SET total_claimed = total_claimed + ?, kills = kills + 1, " +
                             "hunter_name = ?, updated_at = ? WHERE hunter_uuid = ?")) {
            update.setDouble(1, amount);
            update.setString(2, hunterName);
            update.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            update.setString(4, hunterUuid.toString());
            if (update.executeUpdate() > 0) return;

            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO apexsions_bounty_hunters (hunter_uuid, hunter_name, total_claimed, kills, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?)")) {
                insert.setString(1, hunterUuid.toString());
                insert.setString(2, hunterName);
                insert.setDouble(3, amount);
                insert.setInt(4, 1);
                insert.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                insert.executeUpdate();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed recording hunter claim for " + hunterName, e);
        }
    }

    /** Returns hunter leaderboard ordered by lifetime collected amount. */
    public CompletableFuture<List<HunterStat>> loadHunterLeaderboard(int limit) {
        return databaseManager.supplyAsync(() -> {
            List<HunterStat> list = new ArrayList<>();
            String sql = "SELECT hunter_uuid, hunter_name, total_claimed, kills FROM apexsions_bounty_hunters " +
                    "ORDER BY total_claimed DESC LIMIT ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, Math.max(1, limit));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new HunterStat(
                                UUID.fromString(rs.getString("hunter_uuid")),
                                rs.getString("hunter_name"),
                                rs.getDouble("total_claimed"),
                                rs.getInt("kills")
                        ));
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed loading bounty hunter leaderboard", e);
            }
            return list;
        });
    }

    /** Aggregated target totals computed in SQL. */
    public CompletableFuture<Map<String, Double>> loadTargetTotals(int limit) {
        return databaseManager.supplyAsync(() -> {
            Map<String, Double> map = new LinkedHashMap<>();
            String sql = "SELECT target_name, SUM(amount) AS total FROM apexsions_bounties WHERE active = ? " +
                    "GROUP BY target_uuid, target_name ORDER BY total DESC LIMIT ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, true);
                ps.setInt(2, Math.max(1, limit));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        map.put(rs.getString("target_name"), rs.getDouble("total"));
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed loading bounty target totals", e);
            }
            return map;
        });
    }
}
