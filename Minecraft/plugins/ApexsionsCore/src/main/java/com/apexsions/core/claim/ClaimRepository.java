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
 * Repository for persisting and retrieving land claims, financials, flags, and roles.
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
            String sql = "SELECT id, owner_id, owner_name, world, chunk_x, chunk_z, trusted_players, " +
                    "bank_balance, daily_upkeep, status, grace_period_until, last_tax_collected_at, " +
                    "flags, roles, kingdom_id, created_at FROM apexsions_claims";
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

                    double bankBalance = rs.getDouble("bank_balance");
                    double dailyUpkeep = rs.getDouble("daily_upkeep");
                    String statusStr = rs.getString("status");
                    ClaimStatus status = ClaimStatus.fromString(statusStr);

                    Timestamp gpTs = rs.getTimestamp("grace_period_until");
                    long gracePeriodUntil = gpTs != null ? gpTs.getTime() : 0L;

                    Timestamp taxTs = rs.getTimestamp("last_tax_collected_at");
                    long lastTaxCollectedAt = taxTs != null ? taxTs.getTime() : 0L;

                    String flagsStr = rs.getString("flags");
                    String rolesStr = rs.getString("roles");
                    String kingdomId = rs.getString("kingdom_id");

                    Timestamp ts = rs.getTimestamp("created_at");
                    long createdAt = ts != null ? ts.getTime() : System.currentTimeMillis();

                    Set<UUID> trusted = ClaimChunk.deserializeTrustedPlayers(trustedStr);
                    Map<UUID, ClaimRole> roles = ClaimChunk.deserializeRoles(rolesStr);
                    Map<String, String> flags = ClaimChunk.deserializeFlags(flagsStr);

                    list.add(new ClaimChunk(id, ownerId, ownerName, world, chunkX, chunkZ,
                            trusted, roles, flags, bankBalance, dailyUpkeep, status,
                            gracePeriodUntil, lastTaxCollectedAt, kingdomId, createdAt));
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed loading land claims from database", e);
            }
            return list;
        });
    }

    public CompletableFuture<Void> saveClaim(ClaimChunk claim) {
        return databaseManager.supplyAsync(() -> {
            String sql = "INSERT INTO apexsions_claims (id, owner_id, owner_name, world, chunk_x, chunk_z, " +
                    "trusted_players, bank_balance, daily_upkeep, status, grace_period_until, last_tax_collected_at, " +
                    "flags, roles, kingdom_id, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(world, chunk_x, chunk_z) DO UPDATE SET " +
                    "owner_id = EXCLUDED.owner_id, " +
                    "owner_name = EXCLUDED.owner_name, " +
                    "trusted_players = EXCLUDED.trusted_players, " +
                    "bank_balance = EXCLUDED.bank_balance, " +
                    "daily_upkeep = EXCLUDED.daily_upkeep, " +
                    "status = EXCLUDED.status, " +
                    "grace_period_until = EXCLUDED.grace_period_until, " +
                    "last_tax_collected_at = EXCLUDED.last_tax_collected_at, " +
                    "flags = EXCLUDED.flags, " +
                    "roles = EXCLUDED.roles, " +
                    "kingdom_id = EXCLUDED.kingdom_id";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, claim.getId().toString());
                ps.setString(2, claim.getOwnerId().toString());
                ps.setString(3, claim.getOwnerName());
                ps.setString(4, claim.getWorld());
                ps.setInt(5, claim.getChunkX());
                ps.setInt(6, claim.getChunkZ());
                ps.setString(7, claim.getTrustedPlayersSerialized());
                ps.setDouble(8, claim.getBankBalance());
                ps.setDouble(9, claim.getDailyUpkeep());
                ps.setString(10, claim.getStatus().name());
                ps.setTimestamp(11, claim.getGracePeriodUntil() > 0 ? new Timestamp(claim.getGracePeriodUntil()) : null);
                ps.setTimestamp(12, claim.getLastTaxCollectedAt() > 0 ? new Timestamp(claim.getLastTaxCollectedAt()) : null);
                ps.setString(13, claim.getFlagsSerialized());
                ps.setString(14, claim.getRolesSerialized());
                ps.setString(15, claim.getKingdomId());
                ps.setTimestamp(16, new Timestamp(claim.getCreatedAt()));
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed saving land claim " + claim.getChunkKey(), e);
            }
            return null;
        });
    }

    public CompletableFuture<Void> updateClaimFinancials(ClaimChunk claim) {
        return databaseManager.supplyAsync(() -> {
            String sql = "UPDATE apexsions_claims SET bank_balance = ?, daily_upkeep = ?, status = ?, " +
                    "grace_period_until = ?, last_tax_collected_at = ? WHERE id = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, claim.getBankBalance());
                ps.setDouble(2, claim.getDailyUpkeep());
                ps.setString(3, claim.getStatus().name());
                ps.setTimestamp(4, claim.getGracePeriodUntil() > 0 ? new Timestamp(claim.getGracePeriodUntil()) : null);
                ps.setTimestamp(5, claim.getLastTaxCollectedAt() > 0 ? new Timestamp(claim.getLastTaxCollectedAt()) : null);
                ps.setString(6, claim.getId().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed updating financials for claim " + claim.getChunkKey(), e);
            }
            return null;
        });
    }

    public CompletableFuture<Void> updateClaimFlags(ClaimChunk claim) {
        return databaseManager.supplyAsync(() -> {
            String sql = "UPDATE apexsions_claims SET flags = ? WHERE id = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, claim.getFlagsSerialized());
                ps.setString(2, claim.getId().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed updating flags for claim " + claim.getChunkKey(), e);
            }
            return null;
        });
    }

    public CompletableFuture<Void> updateClaimRoles(ClaimChunk claim) {
        return databaseManager.supplyAsync(() -> {
            String sql = "UPDATE apexsions_claims SET roles = ?, trusted_players = ? WHERE id = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, claim.getRolesSerialized());
                ps.setString(2, claim.getTrustedPlayersSerialized());
                ps.setString(3, claim.getId().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed updating roles for claim " + claim.getChunkKey(), e);
            }
            return null;
        });
    }

    public CompletableFuture<Void> updateTrustedPlayers(ClaimChunk claim) {
        return updateClaimRoles(claim);
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
