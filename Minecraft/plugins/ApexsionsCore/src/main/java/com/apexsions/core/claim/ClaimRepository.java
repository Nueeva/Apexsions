package com.apexsions.core.claim;

import com.apexsions.core.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository for persisting and retrieving land claims from SQLite or PostgreSQL.
 */
public class ClaimRepository {

    private final DatabaseManager databaseManager;
    private final Logger logger;

    public ClaimRepository(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public CompletableFuture<List<ClaimChunk>> loadAllClaims() {
        return databaseManager.supplyAsync(() -> {
            List<ClaimChunk> list = new ArrayList<>();
            String sql = "SELECT id, owner_id, owner_name, world, chunk_x, chunk_z, trusted_players, created_at FROM apexsions_claims";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("id"));
                    UUID ownerId = UUID.fromString(rs.getString("owner_id"));
                    String ownerName = rs.getString("owner_name");
                    String world = rs.getString("world");
                    int chunkX = rs.getInt("chunk_x");
                    int chunkZ = rs.getInt("chunk_z");
                    String trustedStr = rs.getString("trusted_players");
                    Timestamp ts = rs.getTimestamp("created_at");
                    long createdAt = ts != null ? ts.getTime() : System.currentTimeMillis();

                    Set<UUID> trusted = ClaimChunk.deserializeTrustedPlayers(trustedStr);
                    list.add(new ClaimChunk(id, ownerId, ownerName, world, chunkX, chunkZ, trusted, createdAt));
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed loading land claims from database", e);
            }
            return list;
        });
    }

    public CompletableFuture<Void> saveClaim(ClaimChunk claim) {
        return databaseManager.supplyAsync(() -> {
            String sql = "INSERT INTO apexsions_claims (id, owner_id, owner_name, world, chunk_x, chunk_z, trusted_players, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(world, chunk_x, chunk_z) DO UPDATE SET " +
                    "owner_id = EXCLUDED.owner_id, " +
                    "owner_name = EXCLUDED.owner_name, " +
                    "trusted_players = EXCLUDED.trusted_players";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, claim.getId().toString());
                ps.setString(2, claim.getOwnerId().toString());
                ps.setString(3, claim.getOwnerName());
                ps.setString(4, claim.getWorld());
                ps.setInt(5, claim.getChunkX());
                ps.setInt(6, claim.getChunkZ());
                ps.setString(7, claim.getTrustedPlayersSerialized());
                ps.setTimestamp(8, new Timestamp(claim.getCreatedAt()));
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed saving land claim " + claim.getChunkKey(), e);
            }
            return null;
        });
    }

    public CompletableFuture<Void> updateTrustedPlayers(ClaimChunk claim) {
        return databaseManager.supplyAsync(() -> {
            String sql = "UPDATE apexsions_claims SET trusted_players = ? WHERE id = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, claim.getTrustedPlayersSerialized());
                ps.setString(2, claim.getId().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed updating trusted players for claim " + claim.getChunkKey(), e);
            }
            return null;
        });
    }

    public CompletableFuture<Void> deleteClaim(String world, int chunkX, int chunkZ) {
        return databaseManager.supplyAsync(() -> {
            String sql = "DELETE FROM apexsions_claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, world);
                ps.setInt(2, chunkX);
                ps.setInt(3, chunkZ);
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed deleting claim at " + world + ":" + chunkX + ":" + chunkZ, e);
            }
            return null;
        });
    }

    public CompletableFuture<Void> deleteClaimsByOwner(UUID ownerId) {
        return databaseManager.supplyAsync(() -> {
            String sql = "DELETE FROM apexsions_claims WHERE owner_id = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ownerId.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed deleting claims for owner " + ownerId, e);
            }
            return null;
        });
    }
}
