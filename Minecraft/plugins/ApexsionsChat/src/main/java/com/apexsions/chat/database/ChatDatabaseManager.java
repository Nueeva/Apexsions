package com.apexsions.chat.database;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class ChatDatabaseManager {

    private final ApexsionsChatPlugin plugin;
    private HikariDataSource dataSource;
    private final ExecutorService asyncExecutor;

    public ChatDatabaseManager(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
        this.asyncExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "ApexsionsChat-Database-Worker");
            t.setDaemon(true);
            return t;
        });
    }

    public void initialize() {
        FileConfiguration config = plugin.getConfigManager().getMainConfig();
        String type = config.getString("database.type", "SQLITE").toUpperCase();

        HikariConfig hikariConfig = new HikariConfig();
        if ("POSTGRESQL".equals(type)) {
            String host = config.getString("database.postgresql.host", "localhost");
            int port = config.getInt("database.postgresql.port", 5432);
            String db = config.getString("database.postgresql.database", "apexsions_chat");
            String user = config.getString("database.postgresql.username", "postgres");
            String pass = config.getString("database.postgresql.password", "password");
            boolean ssl = config.getBoolean("database.postgresql.ssl", false);

            hikariConfig.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + db + "?ssl=" + ssl);
            hikariConfig.setUsername(user);
            hikariConfig.setPassword(pass);
            hikariConfig.setMaximumPoolSize(config.getInt("database.postgresql.maximum-pool-size", 10));
            hikariConfig.setConnectionTimeout(config.getLong("database.postgresql.connection-timeout-ms", 10000));
        } else {
            // SQLite
            File dbFile = new File(plugin.getDataFolder(), config.getString("database.sqlite.file", "chat_data.db"));
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikariConfig.setMaximumPoolSize(1);
            hikariConfig.setConnectionTimeout(10000);
        }

        hikariConfig.setPoolName("ApexsionsChat-Pool");
        this.dataSource = new HikariDataSource(hikariConfig);

        createTables();
        plugin.getLogger().info("Database connected successfully (" + type + ").");
    }

    private void createTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Reports Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS apexsions_reports (
                    report_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    reporter_uuid VARCHAR(36) NOT NULL,
                    reporter_name VARCHAR(32) NOT NULL,
                    reported_uuid VARCHAR(36) NOT NULL,
                    reported_name VARCHAR(32) NOT NULL,
                    reason TEXT NOT NULL,
                    server VARCHAR(64) NOT NULL,
                    world VARCHAR(64) NOT NULL,
                    timestamp BIGINT NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    moderator_uuid VARCHAR(36),
                    moderator_name VARCHAR(32),
                    resolution TEXT,
                    resolved_at BIGINT
                );
            """);

            // Mail Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS apexsions_mail (
                    mail_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sender_uuid VARCHAR(36) NOT NULL,
                    sender_name VARCHAR(32) NOT NULL,
                    recipient_uuid VARCHAR(36) NOT NULL,
                    recipient_name VARCHAR(32) NOT NULL,
                    subject VARCHAR(64) NOT NULL,
                    body TEXT NOT NULL,
                    created_at BIGINT NOT NULL,
                    read_at BIGINT,
                    is_read BOOLEAN NOT NULL DEFAULT 0,
                    is_archived BOOLEAN NOT NULL DEFAULT 0
                );
            """);

            // Moderation Logs Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS apexsions_moderation_logs (
                    event_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(32) NOT NULL,
                    message_snippet TEXT NOT NULL,
                    channel VARCHAR(32) NOT NULL,
                    rule_violated VARCHAR(64) NOT NULL,
                    action_taken VARCHAR(32) NOT NULL,
                    timestamp BIGINT NOT NULL
                );
            """);

            // Nicknames & Tokens Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS apexsions_nicknames (
                    player_uuid VARCHAR(36) PRIMARY KEY,
                    player_name VARCHAR(32) NOT NULL,
                    nickname_raw VARCHAR(32) NOT NULL,
                    color_style VARCHAR(32) NOT NULL DEFAULT 'default',
                    tokens INTEGER NOT NULL DEFAULT 0,
                    updated_at BIGINT NOT NULL
                );
            """);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database tables: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Data source is not initialized");
        }
        return dataSource.getConnection();
    }

    public void runAsync(Runnable runnable) {
        asyncExecutor.submit(runnable);
    }

    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncExecutor);
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        asyncExecutor.shutdown();
    }
}
