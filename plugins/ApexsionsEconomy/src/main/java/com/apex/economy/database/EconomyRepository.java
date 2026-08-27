package com.apex.economy.database;

import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.auction.AuctionListing;
import com.apex.economy.auction.AuctionStatus;
import com.apex.economy.leaderboard.EconomyLeaderboardEntry;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class EconomyRepository {

    private final ApexsionsEconomy plugin;
    private final File dbFile;
    private Connection connection;
    private final Object dbLock = new Object();

    public EconomyRepository(ApexsionsEconomy plugin, String dbFileName) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), dbFileName);
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
                    // 1. Balances table
                    stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS economy_balances (
                            uuid VARCHAR(36) NOT NULL,
                            currency_id VARCHAR(32) NOT NULL,
                            balance DOUBLE NOT NULL DEFAULT 0,
                            PRIMARY KEY (uuid, currency_id)
                        );
                    """);

                    // 2. Transactions table
                    stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS economy_transactions (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            timestamp BIGINT NOT NULL,
                            sender_uuid VARCHAR(36),
                            receiver_uuid VARCHAR(36),
                            currency_id VARCHAR(32) NOT NULL,
                            amount DOUBLE NOT NULL,
                            type VARCHAR(16) NOT NULL,
                            details TEXT
                        );
                    """);

                    // 3. Auctions table
                    stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS economy_auctions (
                            id VARCHAR(36) PRIMARY KEY,
                            seller_uuid VARCHAR(36) NOT NULL,
                            seller_name VARCHAR(32) NOT NULL,
                            currency_id VARCHAR(32) NOT NULL,
                            price DOUBLE NOT NULL,
                            item_data TEXT NOT NULL,
                            created_at BIGINT NOT NULL,
                            expires_at BIGINT NOT NULL,
                            status VARCHAR(16) NOT NULL,
                            buyer_uuid VARCHAR(36)
                        );
                    """);

                    // 4. Pending Claims table (Money from sales or returned items)
                    stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS economy_pending_claims (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            uuid VARCHAR(36) NOT NULL,
                            type VARCHAR(16) NOT NULL,
                            currency_id VARCHAR(32),
                            amount DOUBLE DEFAULT 0,
                            item_data TEXT,
                            claimed BOOLEAN DEFAULT 0
                        );
                    """);

                    // 5. Trade Settings table
                    stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS economy_trade_settings (
                            uuid VARCHAR(36) PRIMARY KEY,
                            trade_enabled BOOLEAN NOT NULL DEFAULT 1
                        );
                    """);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to initialize economy database", e);
            }
        }
    }


    public void close() {
        synchronized (dbLock) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    // --- BALANCES ---

    public CompletableFuture<Double> loadBalance(UUID uuid, String currencyId, double defaultStarting) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (dbLock) {
                String sql = "SELECT balance FROM economy_balances WHERE uuid = ? AND currency_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, currencyId.toLowerCase());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getDouble("balance");
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error loading balance for " + uuid, e);
                }
                return defaultStarting;
            }
        });
    }

    public CompletableFuture<Void> saveBalance(UUID uuid, String currencyId, double balance) {
        return CompletableFuture.runAsync(() -> {
            synchronized (dbLock) {
                String sql = """
                    INSERT INTO economy_balances (uuid, currency_id, balance)
                    VALUES (?, ?, ?)
                    ON CONFLICT(uuid, currency_id) DO UPDATE SET balance = excluded.balance;
                """;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, currencyId.toLowerCase());
                    ps.setDouble(3, balance);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error saving balance for " + uuid, e);
                }
            }
        });
    }

    public CompletableFuture<List<EconomyLeaderboardEntry>> loadTopBalances(String currencyId, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (dbLock) {
                List<EconomyLeaderboardEntry> list = new ArrayList<>();
                String sql = "SELECT uuid, balance FROM economy_balances WHERE currency_id = ? ORDER BY balance DESC, uuid ASC LIMIT ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, currencyId.toLowerCase());
                    ps.setInt(2, limit);
                    try (ResultSet rs = ps.executeQuery()) {
                        int rank = 1;
                        while (rs.next()) {
                            UUID u = UUID.fromString(rs.getString("uuid"));
                            double bal = rs.getDouble("balance");
                            list.add(new EconomyLeaderboardEntry(rank++, u, currencyId, bal));
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error loading leaderboard for " + currencyId, e);
                }
                return list;
            }
        });
    }

    // --- AUCTIONS ---

    public CompletableFuture<Void> saveAuction(AuctionListing listing) {
        return CompletableFuture.runAsync(() -> {
            synchronized (dbLock) {
                String sql = """
                    INSERT INTO economy_auctions (id, seller_uuid, seller_name, currency_id, price, item_data, created_at, expires_at, status, buyer_uuid)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT(id) DO UPDATE SET
                        status = excluded.status,
                        buyer_uuid = excluded.buyer_uuid;
                """;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, listing.getId());
                    ps.setString(2, listing.getSellerUuid().toString());
                    ps.setString(3, listing.getSellerName());
                    ps.setString(4, listing.getCurrencyId());
                    ps.setDouble(5, listing.getPrice());
                    ps.setString(6, listing.getItemData());
                    ps.setLong(7, listing.getCreatedAt());
                    ps.setLong(8, listing.getExpiresAt());
                    ps.setString(9, listing.getStatus().name());
                    ps.setString(10, listing.getBuyerUuid() != null ? listing.getBuyerUuid().toString() : null);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error saving auction listing " + listing.getId(), e);
                }
            }
        });
    }

    public CompletableFuture<List<AuctionListing>> loadActiveAuctions() {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (dbLock) {
                List<AuctionListing> list = new ArrayList<>();
                String sql = "SELECT * FROM economy_auctions WHERE status = 'ACTIVE' ORDER BY created_at DESC";
                try (PreparedStatement ps = connection.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapAuction(rs));
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error loading active auctions", e);
                }
                return list;
            }
        });
    }

    public CompletableFuture<List<AuctionListing>> loadPlayerAuctions(UUID sellerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (dbLock) {
                List<AuctionListing> list = new ArrayList<>();
                String sql = "SELECT * FROM economy_auctions WHERE seller_uuid = ? ORDER BY created_at DESC";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, sellerUuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(mapAuction(rs));
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error loading player auctions for " + sellerUuid, e);
                }
                return list;
            }
        });
    }

    private AuctionListing mapAuction(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        UUID sellerUuid = UUID.fromString(rs.getString("seller_uuid"));
        String sellerName = rs.getString("seller_name");
        String currencyId = rs.getString("currency_id");
        double price = rs.getDouble("price");
        String itemData = rs.getString("item_data");
        long createdAt = rs.getLong("created_at");
        long expiresAt = rs.getLong("expires_at");
        AuctionStatus status = AuctionStatus.valueOf(rs.getString("status"));
        String bStr = rs.getString("buyer_uuid");
        UUID buyerUuid = bStr != null ? UUID.fromString(bStr) : null;

        return new AuctionListing(id, sellerUuid, sellerName, currencyId, price, itemData, createdAt, expiresAt, status, buyerUuid);
    }

    // --- TRADE SETTINGS ---

    public CompletableFuture<Boolean> loadTradeEnabled(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (dbLock) {
                String sql = "SELECT trade_enabled FROM economy_trade_settings WHERE uuid = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, uuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getBoolean("trade_enabled");
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error loading trade settings for " + uuid, e);
                }
                return true; // default true
            }
        });
    }

    public CompletableFuture<Void> saveTradeEnabled(UUID uuid, boolean enabled) {
        return CompletableFuture.runAsync(() -> {
            synchronized (dbLock) {
                String sql = """
                    INSERT INTO economy_trade_settings (uuid, trade_enabled)
                    VALUES (?, ?)
                    ON CONFLICT(uuid) DO UPDATE SET trade_enabled = excluded.trade_enabled;
                """;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, uuid.toString());
                    ps.setBoolean(2, enabled);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error saving trade settings for " + uuid, e);
                }
            }
        });
    }
}

