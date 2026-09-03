package com.apexsions.media.creator.database;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.creator.model.CreatorClaim;
import com.apexsions.media.creator.model.CreatorProfile;
import com.apexsions.media.creator.model.Platform;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CreatorRepository {

    private final ApexsionsMediaPlugin plugin;
    private HikariDataSource dataSource;

    public CreatorRepository(ApexsionsMediaPlugin plugin) {
        this.plugin = plugin;
        initDatabase();
    }

    private void initDatabase() {
        try {
            HikariConfig config = new HikariConfig();
            String dbType = plugin.getConfig().getString("database.type", "SQLITE").toUpperCase();

            if ("POSTGRESQL".equals(dbType)) {
                String host = plugin.getConfig().getString("database.postgresql.host", "localhost");
                int port = plugin.getConfig().getInt("database.postgresql.port", 5432);
                String database = plugin.getConfig().getString("database.postgresql.database", "apexsions_media");
                String user = plugin.getConfig().getString("database.postgresql.username", "postgres");
                String pass = plugin.getConfig().getString("database.postgresql.password", "password");
                boolean ssl = plugin.getConfig().getBoolean("database.postgresql.ssl", false);

                config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database + "?ssl=" + ssl);
                config.setUsername(user);
                config.setPassword(pass);
                config.setDriverClassName("org.postgresql.Driver");
            } else {
                File dbFile = new File(plugin.getDataFolder(), "creator.db");
                if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

                config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
                config.setDriverClassName("org.sqlite.JDBC");
            }

            config.setMaximumPoolSize(5);
            config.setConnectionTimeout(5000);
            config.setPoolName("ApexsionsMedia-CreatorPool");

            this.dataSource = new HikariDataSource(config);

            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS creator_profiles (
                        uuid VARCHAR(36) PRIMARY KEY,
                        player_name VARCHAR(32) NOT NULL,
                        youtube_channel_id VARCHAR(64),
                        youtube_handle VARCHAR(64),
                        tiktok_username VARCHAR(64),
                        pending_platform VARCHAR(16),
                        pending_identifier VARCHAR(64),
                        pending_verify_code VARCHAR(32),
                        pending_verify_expiry BIGINT DEFAULT 0,
                        created_at BIGINT NOT NULL
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS creator_claims (
                        claim_id VARCHAR(64) PRIMARY KEY,
                        uuid VARCHAR(36) NOT NULL,
                        platform VARCHAR(16) NOT NULL,
                        video_id VARCHAR(64) NOT NULL UNIQUE,
                        video_url TEXT NOT NULL,
                        views BIGINT NOT NULL,
                        likes BIGINT NOT NULL,
                        tier_id VARCHAR(32) NOT NULL,
                        claimed_at BIGINT NOT NULL
                    );
                """);
            }
            plugin.getLogger().info("Creator database initialized successfully (" + dbType + ").");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize Creator database: " + e.getMessage());
        }
    }

    public CompletableFuture<CreatorProfile> loadProfile(UUID uuid, String defaultPlayerName) {
        return CompletableFuture.supplyAsync(() -> {
            if (dataSource == null) return new CreatorProfile(uuid, defaultPlayerName);

            String query = "SELECT * FROM creator_profiles WHERE uuid = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new CreatorProfile(
                                uuid,
                                rs.getString("player_name"),
                                rs.getString("youtube_channel_id"),
                                rs.getString("youtube_handle"),
                                rs.getString("tiktok_username"),
                                rs.getString("pending_platform"),
                                rs.getString("pending_identifier"),
                                rs.getString("pending_verify_code"),
                                rs.getLong("pending_verify_expiry"),
                                rs.getLong("created_at")
                        );
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load creator profile for " + uuid + ": " + e.getMessage());
            }
            return new CreatorProfile(uuid, defaultPlayerName);
        });
    }

    public CompletableFuture<Void> saveProfile(CreatorProfile profile) {
        return CompletableFuture.runAsync(() -> {
            if (dataSource == null) return;

            String query = """
                INSERT INTO creator_profiles (uuid, player_name, youtube_channel_id, youtube_handle, tiktok_username,
                                              pending_platform, pending_identifier, pending_verify_code, pending_verify_expiry, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    player_name = excluded.player_name,
                    youtube_channel_id = excluded.youtube_channel_id,
                    youtube_handle = excluded.youtube_handle,
                    tiktok_username = excluded.tiktok_username,
                    pending_platform = excluded.pending_platform,
                    pending_identifier = excluded.pending_identifier,
                    pending_verify_code = excluded.pending_verify_code,
                    pending_verify_expiry = excluded.pending_verify_expiry;
            """;

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, profile.getUuid().toString());
                ps.setString(2, profile.getPlayerName());
                ps.setString(3, profile.getYoutubeChannelId());
                ps.setString(4, profile.getYoutubeHandle());
                ps.setString(5, profile.getTiktokUsername());
                ps.setString(6, profile.getPendingPlatform());
                ps.setString(7, profile.getPendingIdentifier());
                ps.setString(8, profile.getPendingVerifyCode());
                ps.setLong(9, profile.getPendingVerifyExpiry());
                ps.setLong(10, profile.getCreatedAt());
                ps.executeUpdate();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save creator profile for " + profile.getUuid() + ": " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Boolean> isVideoClaimed(String videoId) {
        return CompletableFuture.supplyAsync(() -> {
            if (dataSource == null) return false;

            String query = "SELECT 1 FROM creator_claims WHERE video_id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, videoId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to check claimed video " + videoId + ": " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Void> saveClaim(CreatorClaim claim) {
        return CompletableFuture.runAsync(() -> {
            if (dataSource == null) return;

            String query = "INSERT INTO creator_claims (claim_id, uuid, platform, video_id, video_url, views, likes, tier_id, claimed_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, claim.getClaimId());
                ps.setString(2, claim.getUuid().toString());
                ps.setString(3, claim.getPlatform().name());
                ps.setString(4, claim.getVideoId());
                ps.setString(5, claim.getVideoUrl());
                ps.setLong(6, claim.getViews());
                ps.setLong(7, claim.getLikes());
                ps.setString(8, claim.getTierId());
                ps.setLong(9, claim.getClaimedAt());
                ps.executeUpdate();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save creator claim " + claim.getClaimId() + ": " + e.getMessage());
            }
        });
    }

    public CompletableFuture<List<CreatorClaim>> getClaims(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<CreatorClaim> list = new ArrayList<>();
            if (dataSource == null) return list;

            String query = "SELECT * FROM creator_claims WHERE uuid = ? ORDER BY claimed_at DESC LIMIT 50";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new CreatorClaim(
                                rs.getString("claim_id"),
                                uuid,
                                Platform.valueOf(rs.getString("platform")),
                                rs.getString("video_id"),
                                rs.getString("video_url"),
                                rs.getLong("views"),
                                rs.getLong("likes"),
                                rs.getString("tier_id"),
                                rs.getLong("claimed_at")
                        ));
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to get claims for " + uuid + ": " + e.getMessage());
            }
            return list;
        });
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
