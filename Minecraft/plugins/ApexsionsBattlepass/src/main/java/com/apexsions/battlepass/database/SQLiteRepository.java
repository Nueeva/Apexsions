package com.apexsions.battlepass.database;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.player.PlayerData;
import com.apexsions.battlepass.shop.ShopCategory;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SQLiteRepository implements PlayerDataRepository {

    private final ApexsionsBattlepass plugin;
    private final File dbFile;
    private String connectionUrl;

    public SQLiteRepository(ApexsionsBattlepass plugin, String fileName) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), fileName);
        this.connectionUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionUrl);
    }

    @Override
    public void init() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Player data table
            stmt.execute("CREATE TABLE IF NOT EXISTS abp_player_data (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "season_id INT NOT NULL, " +
                    "level INT NOT NULL, " +
                    "xp INT NOT NULL, " +
                    "currency INT NOT NULL, " +
                    "passes TEXT NOT NULL, " +
                    "claimed_rewards TEXT NOT NULL, " +
                    "last_daily_reset BIGINT NOT NULL, " +
                    "last_weekly_reset BIGINT NOT NULL, " +
                    "last_monthly_reset BIGINT NOT NULL, " +
                    "daily_refresh_count INT DEFAULT 0, " +
                    "total_refresh_count INT DEFAULT 0, " +
                    "shop_rotations TEXT DEFAULT ''" +
                    ");");

            // Migration safety: Add new columns if older database version exists
            try { stmt.execute("ALTER TABLE abp_player_data ADD COLUMN daily_refresh_count INT DEFAULT 0;"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE abp_player_data ADD COLUMN total_refresh_count INT DEFAULT 0;"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE abp_player_data ADD COLUMN shop_rotations TEXT DEFAULT '';"); } catch (SQLException ignored) {}

            // Quest progress table
            stmt.execute("CREATE TABLE IF NOT EXISTS abp_quest_progress (" +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "quest_id VARCHAR(64) NOT NULL, " +
                    "progress INT NOT NULL, " +
                    "completed INT NOT NULL, " +
                    "PRIMARY KEY (uuid, quest_id)" +
                    ");");

            // Shop purchases table
            stmt.execute("CREATE TABLE IF NOT EXISTS abp_shop_purchases (" +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "shop_item_id VARCHAR(64) NOT NULL, " +
                    "purchase_count INT NOT NULL, " +
                    "PRIMARY KEY (uuid, shop_item_id)" +
                    ");");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQLite database", e);
        }
    }

    @Override
    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid, int currentSeasonId) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerData data = new PlayerData(uuid, currentSeasonId);

            try (Connection conn = getConnection()) {
                // Load player base data
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM abp_player_data WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int seasonId = rs.getInt("season_id");
                            data.setSeasonId(seasonId);
                            data.setLevel(rs.getInt("level"));
                            data.setXp(rs.getInt("xp"));
                            data.setCurrency(rs.getInt("currency"));

                            String passesStr = rs.getString("passes");
                            if (passesStr != null && !passesStr.isEmpty()) {
                                for (String p : passesStr.split(",")) {
                                    if (!p.isBlank()) data.addPass(p);
                                }
                            }

                            String rewardsStr = rs.getString("claimed_rewards");
                            if (rewardsStr != null && !rewardsStr.isEmpty()) {
                                for (String r : rewardsStr.split(",")) {
                                    if (!r.isBlank()) data.getClaimedRewards().add(r);
                                }
                            }

                            data.setLastDailyReset(rs.getLong("last_daily_reset"));
                            data.setLastWeeklyReset(rs.getLong("last_weekly_reset"));
                            data.setLastMonthlyReset(rs.getLong("last_monthly_reset"));

                            try {
                                data.setDailyRefreshCount(rs.getInt("daily_refresh_count"));
                                data.setTotalRefreshCount(rs.getInt("total_refresh_count"));
                                String rotationsStr = rs.getString("shop_rotations");
                                deserializeRotations(data, rotationsStr);
                            } catch (SQLException ignored) {}

                            // If player was in an old season, auto-reset progress for current season
                            if (seasonId != currentSeasonId) {
                                data.resetProgressForNewSeason(currentSeasonId);
                            }
                        }
                    }
                }

                // Load quest progress
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM abp_quest_progress WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String questId = rs.getString("quest_id");
                            int progress = rs.getInt("progress");
                            boolean completed = rs.getInt("completed") == 1;
                            data.getQuestProgress().put(questId, progress);
                            data.getQuestCompleted().put(questId, completed);
                        }
                    }
                }

                // Load shop purchases
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM abp_shop_purchases WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String shopItemId = rs.getString("shop_item_id");
                            int count = rs.getInt("purchase_count");
                            data.getShopPurchases().put(shopItemId, count);
                        }
                    }
                }

            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error loading player data for " + uuid, e);
            }

            return data;
        });
    }

    @Override
    public CompletableFuture<Void> savePlayerData(PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                // Save base player data
                String sqlPlayer = "INSERT INTO abp_player_data (uuid, season_id, level, xp, currency, passes, claimed_rewards, last_daily_reset, last_weekly_reset, last_monthly_reset, daily_refresh_count, total_refresh_count, shop_rotations) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET " +
                        "season_id=excluded.season_id, level=excluded.level, xp=excluded.xp, currency=excluded.currency, " +
                        "passes=excluded.passes, claimed_rewards=excluded.claimed_rewards, " +
                        "last_daily_reset=excluded.last_daily_reset, last_weekly_reset=excluded.last_weekly_reset, last_monthly_reset=excluded.last_monthly_reset, " +
                        "daily_refresh_count=excluded.daily_refresh_count, total_refresh_count=excluded.total_refresh_count, shop_rotations=excluded.shop_rotations;";

                try (PreparedStatement ps = conn.prepareStatement(sqlPlayer)) {
                    ps.setString(1, data.getUuid().toString());
                    ps.setInt(2, data.getSeasonId());
                    ps.setInt(3, data.getLevel());
                    ps.setInt(4, data.getXp());
                    ps.setInt(5, data.getCurrency());
                    ps.setString(6, String.join(",", data.getPasses()));
                    ps.setString(7, String.join(",", data.getClaimedRewards()));
                    ps.setLong(8, data.getLastDailyReset());
                    ps.setLong(9, data.getLastWeeklyReset());
                    ps.setLong(10, data.getLastMonthlyReset());
                    ps.setInt(11, data.getDailyRefreshCount());
                    ps.setInt(12, data.getTotalRefreshCount());
                    ps.setString(13, serializeRotations(data));
                    ps.executeUpdate();
                }

                // Save quest progress
                String sqlQuest = "INSERT INTO abp_quest_progress (uuid, quest_id, progress, completed) VALUES (?, ?, ?, ?) " +
                        "ON CONFLICT(uuid, quest_id) DO UPDATE SET progress=excluded.progress, completed=excluded.completed;";

                try (PreparedStatement ps = conn.prepareStatement(sqlQuest)) {
                    for (var entry : data.getQuestProgress().entrySet()) {
                        String questId = entry.getKey();
                        int progress = entry.getValue();
                        boolean completed = data.isQuestCompleted(questId);

                        ps.setString(1, data.getUuid().toString());
                        ps.setString(2, questId);
                        ps.setInt(3, progress);
                        ps.setInt(4, completed ? 1 : 0);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // Save shop purchases
                String sqlShop = "INSERT INTO abp_shop_purchases (uuid, shop_item_id, purchase_count) VALUES (?, ?, ?) " +
                        "ON CONFLICT(uuid, shop_item_id) DO UPDATE SET purchase_count=excluded.purchase_count;";

                try (PreparedStatement ps = conn.prepareStatement(sqlShop)) {
                    for (var entry : data.getShopPurchases().entrySet()) {
                        ps.setString(1, data.getUuid().toString());
                        ps.setString(2, entry.getKey());
                        ps.setInt(3, entry.getValue());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error saving player data for " + data.getUuid(), e);
            }
        });
    }

    private static String serializeRotations(PlayerData data) {
        StringBuilder sb = new StringBuilder();
        for (var entry : data.getActiveRotations().entrySet()) {
            if (sb.length() > 0) sb.append(";");
            sb.append(entry.getKey().name()).append(":").append(String.join(",", entry.getValue()));
        }
        return sb.toString();
    }

    private static void deserializeRotations(PlayerData data, String str) {
        if (str == null || str.isBlank()) return;
        String[] parts = str.split(";");
        for (String part : parts) {
            String[] kv = part.split(":");
            if (kv.length == 2) {
                try {
                    ShopCategory cat = ShopCategory.valueOf(kv[0]);
                    String[] ids = kv[1].split(",");
                    List<String> list = new ArrayList<>();
                    for (String id : ids) {
                        if (!id.isBlank()) list.add(id);
                    }
                    data.setRotation(cat, list);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    @Override
    public CompletableFuture<List<PlayerData>> loadAllPlayerData(int currentSeasonId) {
        return CompletableFuture.supplyAsync(() -> {
            List<PlayerData> list = new ArrayList<>();
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM abp_player_data")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        int seasonId = rs.getInt("season_id");
                        PlayerData data = new PlayerData(uuid, seasonId);
                        data.setLevel(rs.getInt("level"));
                        data.setXp(rs.getInt("xp"));
                        data.setCurrency(rs.getInt("currency"));

                        String passesStr = rs.getString("passes");
                        if (passesStr != null && !passesStr.isEmpty()) {
                            for (String p : passesStr.split(",")) {
                                if (!p.isBlank()) data.addPass(p);
                            }
                        }

                        String rewardsStr = rs.getString("claimed_rewards");
                        if (rewardsStr != null && !rewardsStr.isEmpty()) {
                            for (String r : rewardsStr.split(",")) {
                                if (!r.isBlank()) data.getClaimedRewards().add(r);
                            }
                        }

                        data.setLastDailyReset(rs.getLong("last_daily_reset"));
                        data.setLastWeeklyReset(rs.getLong("last_weekly_reset"));
                        data.setLastMonthlyReset(rs.getLong("last_monthly_reset"));

                        try {
                            data.setDailyRefreshCount(rs.getInt("daily_refresh_count"));
                            data.setTotalRefreshCount(rs.getInt("total_refresh_count"));
                            String rotationsStr = rs.getString("shop_rotations");
                            deserializeRotations(data, rotationsStr);
                        } catch (SQLException ignored) {}

                        if (seasonId != currentSeasonId) {
                            data.resetProgressForNewSeason(currentSeasonId);
                        }
                        list.add(data);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error loading all player data", e);
            }
            return list;
        });
    }

    @Override
    public void close() {
        // SQLite file connection closes automatically per query statement
    }
}

