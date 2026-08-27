package com.yourserver.apexsionschat.database;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import com.yourserver.apexsionschat.model.Report;
import com.yourserver.apexsionschat.model.ReportStatus;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ReportRepository {

    private final ApexsionsChatPlugin plugin;
    private final ChatDatabaseManager db;

    public ReportRepository(ApexsionsChatPlugin plugin, ChatDatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public CompletableFuture<Long> createReportAsync(Report report) {
        return db.supplyAsync(() -> {
            String sql = """
                INSERT INTO apexsions_reports (
                    reporter_uuid, reporter_name, reported_uuid, reported_name,
                    reason, server, world, timestamp, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, report.getReporterUuid().toString());
                ps.setString(2, report.getReporterName());
                ps.setString(3, report.getReportedUuid().toString());
                ps.setString(4, report.getReportedName());
                ps.setString(5, report.getReason());
                ps.setString(6, report.getServer());
                ps.setString(7, report.getWorld());
                ps.setLong(8, report.getTimestamp().toEpochMilli());
                ps.setString(9, report.getStatus().name());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        report.setReportId(id);
                        return id;
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to insert report: " + e.getMessage());
            }
            return -1L;
        });
    }

    public CompletableFuture<List<Report>> getOpenReportsAsync(int limit, int offset) {
        return db.supplyAsync(() -> {
            List<Report> list = new ArrayList<>();
            String sql = "SELECT * FROM apexsions_reports WHERE status IN ('OPEN', 'REVIEWING') ORDER BY timestamp DESC LIMIT ? OFFSET ?;";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limit);
                ps.setInt(2, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to fetch open reports: " + e.getMessage());
            }
            return list;
        });
    }

    public CompletableFuture<Integer> countOpenReportsAsync() {
        return db.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM apexsions_reports WHERE status IN ('OPEN', 'REVIEWING');";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to count open reports: " + e.getMessage());
            }
            return 0;
        });
    }

    public CompletableFuture<Optional<Report>> getReportByIdAsync(long reportId) {
        return db.supplyAsync(() -> {
            String sql = "SELECT * FROM apexsions_reports WHERE report_id = ?;";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, reportId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to fetch report by ID: " + e.getMessage());
            }
            return Optional.empty();
        });
    }

    public CompletableFuture<Boolean> updateReportStatusAsync(long reportId, ReportStatus status, UUID modUuid, String modName, String resolution) {
        return db.supplyAsync(() -> {
            String sql = """
                UPDATE apexsions_reports SET status = ?, moderator_uuid = ?, moderator_name = ?, resolution = ?, resolved_at = ?
                WHERE report_id = ?;
            """;
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status.name());
                ps.setString(2, modUuid != null ? modUuid.toString() : null);
                ps.setString(3, modName);
                ps.setString(4, resolution);
                ps.setLong(5, Instant.now().toEpochMilli());
                ps.setLong(6, reportId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to update report status: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Integer> countRecentReportsByPlayerAsync(UUID reporterUuid, long sinceMillis) {
        return db.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM apexsions_reports WHERE reporter_uuid = ? AND timestamp >= ?;";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, reporterUuid.toString());
                ps.setLong(2, sinceMillis);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to count player reports: " + e.getMessage());
            }
            return 0;
        });
    }

    private Report mapResultSet(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setReportId(rs.getLong("report_id"));
        report.setReporterUuid(UUID.fromString(rs.getString("reporter_uuid")));
        report.setReporterName(rs.getString("reporter_name"));
        report.setReportedUuid(UUID.fromString(rs.getString("reported_uuid")));
        report.setReportedName(rs.getString("reported_name"));
        report.setReason(rs.getString("reason"));
        report.setServer(rs.getString("server"));
        report.setWorld(rs.getString("world"));
        report.setTimestamp(Instant.ofEpochMilli(rs.getLong("timestamp")));
        report.setStatus(ReportStatus.fromString(rs.getString("status")));

        String modUuidStr = rs.getString("moderator_uuid");
        if (modUuidStr != null && !modUuidStr.isEmpty()) {
            report.setModeratorUuid(UUID.fromString(modUuidStr));
        }
        report.setModeratorName(rs.getString("moderator_name"));
        report.setResolution(rs.getString("resolution"));
        long resAt = rs.getLong("resolved_at");
        if (resAt > 0) {
            report.setResolvedAt(Instant.ofEpochMilli(resAt));
        }
        return report;
    }
}
