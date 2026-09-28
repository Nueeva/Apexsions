package com.apexsions.jobs.database;

import com.apexsions.jobs.model.PlayerJobData;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class JobsRepository {

    private final Plugin plugin;
    private final JobsDatabaseManager databaseManager;

    public JobsRepository(Plugin plugin, JobsDatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public CompletableFuture<Map<String, PlayerJobData>> loadPlayerJobs(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, PlayerJobData> map = new HashMap<>();
            String sql = "SELECT * FROM apx_player_jobs WHERE uuid = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String jobId = rs.getString("job_id");
                        int level = rs.getInt("job_level");
                        double exp = rs.getDouble("job_exp");
                        double daily = rs.getDouble("daily_earnings");
                        String lastDate = rs.getString("last_earning_date");
                        boolean active = rs.getInt("is_active") == 1 || rs.getBoolean("is_active");

                        map.put(jobId.toLowerCase(), new PlayerJobData(uuid, jobId, level, exp, daily, lastDate, active));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Error loading jobs for " + uuid + ": " + e.getMessage());
            }
            return map;
        });
    }

    public CompletableFuture<Void> savePlayerJob(PlayerJobData data) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO apx_player_jobs (uuid, job_id, job_level, job_exp, daily_earnings, last_earning_date, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(uuid, job_id) DO UPDATE SET
                    job_level = excluded.job_level,
                    job_exp = excluded.job_exp,
                    daily_earnings = excluded.daily_earnings,
                    last_earning_date = excluded.last_earning_date,
                    is_active = excluded.is_active;
            """;
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, data.getUuid().toString());
                ps.setString(2, data.getJobId());
                ps.setInt(3, data.getLevel());
                ps.setDouble(4, data.getExp());
                ps.setDouble(5, data.getDailyEarnings());
                ps.setString(6, data.getLastEarningDate());
                ps.setInt(7, data.isActive() ? 1 : 0);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Error saving job " + data.getJobId() + " for " + data.getUuid() + ": " + e.getMessage());
            }
        });
    }

    public void recordPlacedBlock(String world, int x, int y, int z) {
        CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO apx_placed_blocks (world_id, block_x, block_y, block_z, placed_at)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(world_id, block_x, block_y, block_z) DO UPDATE SET
                    placed_at = excluded.placed_at;
            """;
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, world);
                ps.setInt(2, x);
                ps.setInt(3, y);
                ps.setInt(4, z);
                ps.setLong(5, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException ignored) {}
        });
    }

    public boolean isPlacedBlock(String world, int x, int y, int z) {
        String sql = "SELECT 1 FROM apx_placed_blocks WHERE world_id = ? AND block_x = ? AND block_y = ? AND block_z = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ignored) {
            return false;
        }
    }

    public void removePlacedBlock(String world, int x, int y, int z) {
        CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM apx_placed_blocks WHERE world_id = ? AND block_x = ? AND block_y = ? AND block_z = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, world);
                ps.setInt(2, x);
                ps.setInt(3, y);
                ps.setInt(4, z);
                ps.executeUpdate();
            } catch (SQLException ignored) {}
        });
    }
}
