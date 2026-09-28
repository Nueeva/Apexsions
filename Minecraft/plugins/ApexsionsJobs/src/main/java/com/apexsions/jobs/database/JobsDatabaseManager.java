package com.apexsions.jobs.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class JobsDatabaseManager {

    private final Plugin plugin;
    private HikariDataSource dataSource;
    private boolean isPostgre = false;

    public JobsDatabaseManager(Plugin plugin) {
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
            String fileName = config.getString("database.sqlite.file", "jobs.db");
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
        hikariConfig.setPoolName("ApexsionsJobs-Pool");

        this.dataSource = new HikariDataSource(hikariConfig);
        createTables();
    }

    private void createTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if (isPostgre) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS apx_player_jobs (
                        uuid VARCHAR(36) NOT NULL,
                        job_id VARCHAR(32) NOT NULL,
                        job_level INT DEFAULT 1,
                        job_exp DOUBLE PRECISION DEFAULT 0,
                        daily_earnings DOUBLE PRECISION DEFAULT 0,
                        last_earning_date VARCHAR(10),
                        is_active BOOLEAN DEFAULT TRUE,
                        PRIMARY KEY (uuid, job_id)
                    );
                """);
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS apx_placed_blocks (
                        world_id VARCHAR(64) NOT NULL,
                        block_x INT NOT NULL,
                        block_y INT NOT NULL,
                        block_z INT NOT NULL,
                        placed_at BIGINT NOT NULL,
                        PRIMARY KEY (world_id, block_x, block_y, block_z)
                    );
                """);
            } else {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS apx_player_jobs (
                        uuid TEXT NOT NULL,
                        job_id TEXT NOT NULL,
                        job_level INTEGER DEFAULT 1,
                        job_exp REAL DEFAULT 0,
                        daily_earnings REAL DEFAULT 0,
                        last_earning_date TEXT,
                        is_active INTEGER DEFAULT 1,
                        PRIMARY KEY (uuid, job_id)
                    );
                """);
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS apx_placed_blocks (
                        world_id TEXT NOT NULL,
                        block_x INTEGER NOT NULL,
                        block_y INTEGER NOT NULL,
                        block_z INTEGER NOT NULL,
                        placed_at INTEGER NOT NULL,
                        PRIMARY KEY (world_id, block_x, block_y, block_z)
                    );
                """);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Gagal menginisialisasi tabel database ApexsionsJobs: " + e.getMessage());
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
