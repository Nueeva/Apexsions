package com.apexsions.quests.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final Plugin plugin;
    private HikariDataSource dataSource;
    private boolean isPostgre = false;

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        FileConfiguration config = plugin.getConfig();
        String type = config.getString("database.type", "sqlite").toLowerCase();
        HikariConfig hikariConfig = new HikariConfig();

        if (type.equals("postgresql")) {
            isPostgre = true;
            String host = config.getString("database.postgresql.host", "localhost");
            int port = config.getInt("database.postgresql.port", 5432);
            String db = config.getString("database.postgresql.database", "apexsions");
            String user = config.getString("database.postgresql.username", "postgres");
            String pass = config.getString("database.postgresql.password", "password");
            boolean ssl = config.getBoolean("database.postgresql.ssl", false);

            hikariConfig.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + db + "?ssl=" + ssl);
            hikariConfig.setUsername(user);
            hikariConfig.setPassword(pass);
            hikariConfig.setDriverClassName("org.postgresql.Driver");
        } else {
            isPostgre = false;
            String fileName = config.getString("database.sqlite.file", "quests.db");
            File dbFile = new File(plugin.getDataFolder(), fileName);
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
            hikariConfig.setConnectionTestQuery("SELECT 1");
        }

        hikariConfig.setMaximumPoolSize(config.getInt("database.pool.maximum-pool-size", 10));
        hikariConfig.setMinimumIdle(config.getInt("database.pool.minimum-idle", 2));
        hikariConfig.setConnectionTimeout(config.getLong("database.pool.connection-timeout", 30000));
        hikariConfig.setPoolName("ApexsionsQuests-Pool");

        this.dataSource = new HikariDataSource(hikariConfig);
        createTables();
    }

    private void createTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if (isPostgre) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS apx_login_streaks (
                        uuid VARCHAR(36) PRIMARY KEY,
                        current_streak INT DEFAULT 0,
                        highest_streak INT DEFAULT 0,
                        last_login_date VARCHAR(10),
                        last_claim_date VARCHAR(10),
                        monthly_claimed_mask BIGINT DEFAULT 0,
                        claimed_milestones VARCHAR(255) DEFAULT '',
                        streak_freeze_count INT DEFAULT 0,
                        rerolls_used_today INT DEFAULT 0,
                        last_reroll_date VARCHAR(10),
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """);
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS apx_player_quests (
                        id VARCHAR(64) PRIMARY KEY,
                        uuid VARCHAR(36) NOT NULL,
                        quest_id VARCHAR(64) NOT NULL,
                        quest_date VARCHAR(10) NOT NULL,
                        current_progress INT DEFAULT 0,
                        target_progress INT NOT NULL,
                        is_completed BOOLEAN DEFAULT FALSE,
                        is_claimed BOOLEAN DEFAULT FALSE
                    );
                """);
            } else {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS apx_login_streaks (
                        uuid VARCHAR(36) PRIMARY KEY,
                        current_streak INTEGER DEFAULT 0,
                        highest_streak INTEGER DEFAULT 0,
                        last_login_date TEXT,
                        last_claim_date TEXT,
                        monthly_claimed_mask INTEGER DEFAULT 0,
                        claimed_milestones TEXT DEFAULT '',
                        streak_freeze_count INTEGER DEFAULT 0,
                        rerolls_used_today INTEGER DEFAULT 0,
                        last_reroll_date TEXT,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """);
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS apx_player_quests (
                        id TEXT PRIMARY KEY,
                        uuid TEXT NOT NULL,
                        quest_id TEXT NOT NULL,
                        quest_date TEXT NOT NULL,
                        current_progress INTEGER DEFAULT 0,
                        target_progress INTEGER NOT NULL,
                        is_completed INTEGER DEFAULT 0,
                        is_claimed INTEGER DEFAULT 0
                    );
                """);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Gagal menginisialisasi tabel database ApexsionsQuests: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("HikariDataSource belum diinisialisasi.");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
