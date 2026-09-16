package com.apexsions.core.database;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * Manages database connection pooling, async task execution, and fallback database support.
 */
public class DatabaseManager {

    private final ApexsionsCorePlugin plugin;
    private final ConfigManager configManager;
    private HikariDataSource dataSource;
    private final ExecutorService asyncExecutor;
    private boolean usingFallback = false;

    public DatabaseManager(ApexsionsCorePlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.asyncExecutor = new ThreadPoolExecutor(
                4, 16,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                r -> {
                    Thread t = new Thread(r, "KingdomCore-DB-Async");
                    t.setDaemon(true);
                    return t;
                }
        );
    }

    public void initialize() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String type = configManager.getDbType().toLowerCase();

        if (type.equals("postgresql")) {
            try {
                initPostgreSql();
                runMigrations();
                plugin.getLogger().info("PostgreSQL database connection pool initialized successfully.");
                return;
            } catch (Exception ex) {
                if (configManager.isDbAutoFallback()) {
                    plugin.getLogger().warning("Failed to connect to PostgreSQL: " + ex.getMessage() + ". Switching to local SQLite database...");
                    initFallbackSqlite();
                    return;
                } else {
                    plugin.getLogger().log(Level.SEVERE, "Could not initialize PostgreSQL database pool!", ex);
                    throw new RuntimeException("Database initialization failed", ex);
                }
            }
        } else if (type.equals("h2")) {
            initFallbackH2();
        } else {
            initFallbackSqlite();
        }
    }

    private void initPostgreSql() {
        HikariConfig hikari = new HikariConfig();
        hikari.setDriverClassName("org.postgresql.Driver");
        hikari.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s",
                configManager.getDbHost(),
                configManager.getDbPort(),
                configManager.getDbName()));
        hikari.setUsername(configManager.getDbUser());
        hikari.setPassword(configManager.getDbPassword());

        hikari.setMaximumPoolSize(configManager.getDbMaxPoolSize());
        hikari.setMinimumIdle(configManager.getDbMinIdle());
        hikari.setIdleTimeout(configManager.getDbIdleTimeout());
        hikari.setConnectionTimeout(configManager.getDbConnectionTimeout());
        hikari.setMaxLifetime(configManager.getDbMaxLifetime());
        hikari.setPoolName("KingdomCore-Postgres-Pool");

        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(hikari);
        this.usingFallback = false;
    }

    private void initFallbackSqlite() {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }

            File dbFile = new File(plugin.getDataFolder(), "database.db");
            HikariConfig hikari = new HikariConfig();
            hikari.setDriverClassName("org.sqlite.JDBC");
            hikari.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikari.setMaximumPoolSize(1); // SQLite performs best with single write connection pool
            hikari.setPoolName("KingdomCore-SQLite-Pool");
            hikari.setConnectionTimeout(15000);

            this.dataSource = new HikariDataSource(hikari);
            this.usingFallback = true;
            createTables();
            plugin.getLogger().info("Local SQLite database initialized successfully at " + dbFile.getName());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQLite database fallback!", e);
        }
    }

    private void initFallbackH2() {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
            File dbFile = new File(plugin.getDataFolder(), "database");
            HikariConfig hikari = new HikariConfig();
            hikari.setDriverClassName("org.h2.Driver");
            hikari.setJdbcUrl("jdbc:h2:" + dbFile.getAbsolutePath() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE");
            hikari.setUsername("sa");
            hikari.setPassword("");
            hikari.setMaximumPoolSize(5);
            hikari.setPoolName("KingdomCore-H2-Pool");

            this.dataSource = new HikariDataSource(hikari);
            this.usingFallback = true;
            createTables();
            plugin.getLogger().info("Local H2 database initialized successfully.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize H2 database fallback! Attempting SQLite...", e);
            initFallbackSqlite();
        }
    }

    private void runMigrations() {
        try {
            Flyway flyway = Flyway.configure(getClass().getClassLoader())
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load();
            flyway.migrate();
            plugin.getLogger().info("Flyway database migrations applied successfully.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Flyway migration encountered an issue: " + e.getMessage() + ". Running direct table setup...", e);
            createTables();
        }
    }

    private void createTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS regions (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "key VARCHAR(32) UNIQUE NOT NULL, " +
                    "display_name VARCHAR(64) NOT NULL, " +
                    "world_name VARCHAR(128) NOT NULL, " +
                    "spawn_x DOUBLE, " +
                    "spawn_y DOUBLE, " +
                    "spawn_z DOUBLE, " +
                    "spawn_yaw FLOAT, " +
                    "spawn_pitch FLOAT, " +
                    "enabled BOOLEAN NOT NULL DEFAULT TRUE);");

            stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "username VARCHAR(16) NOT NULL, " +
                    "level INTEGER NOT NULL DEFAULT 1, " +
                    "xp BIGINT NOT NULL DEFAULT 0, " +
                    "region_id VARCHAR(36), " +
                    "claimed_rewards TEXT NOT NULL DEFAULT '', " +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);");

            try {
                stmt.execute("ALTER TABLE players ADD COLUMN claimed_rewards TEXT NOT NULL DEFAULT '';");
            } catch (SQLException ignored) {
                // Column already exists
            }

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_players_region ON players(region_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_players_level ON players(level);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_regions_key ON regions(key);");

            // Sovereign Land Claims Table
            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS apexsions_claims (" +
                        "id VARCHAR(36) PRIMARY KEY, " +
                        "owner_id VARCHAR(36) NOT NULL, " +
                        "owner_name VARCHAR(32) NOT NULL, " +
                        "world VARCHAR(128) NOT NULL, " +
                        "chunk_x INTEGER NOT NULL, " +
                        "chunk_z INTEGER NOT NULL, " +
                        "trusted_players TEXT NOT NULL DEFAULT '', " +
                        "bank_balance REAL NOT NULL DEFAULT 0.0, " +
                        "daily_upkeep REAL NOT NULL DEFAULT 100.0, " +
                        "status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', " +
                        "grace_period_until TIMESTAMP NULL, " +
                        "last_tax_collected_at TIMESTAMP NULL, " +
                        "flags TEXT NOT NULL DEFAULT '{}', " +
                        "roles TEXT NOT NULL DEFAULT '{}', " +
                        "kingdom_id VARCHAR(64) NULL, " +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "CONSTRAINT uq_claim_chunk UNIQUE (world, chunk_x, chunk_z));");
            } catch (SQLException e) {
                plugin.getLogger().warning("Error ensuring apexsions_claims table: " + e.getMessage());
            }

            // Migration safety: MUST add missing columns BEFORE creating indexes on them
            addColumnIfNotExists(stmt, "apexsions_claims", "bank_balance", "REAL NOT NULL DEFAULT 0.0");
            addColumnIfNotExists(stmt, "apexsions_claims", "daily_upkeep", "REAL NOT NULL DEFAULT 100.0");
            addColumnIfNotExists(stmt, "apexsions_claims", "status", "VARCHAR(32) NOT NULL DEFAULT 'ACTIVE'");
            addColumnIfNotExists(stmt, "apexsions_claims", "grace_period_until", "TIMESTAMP NULL");
            addColumnIfNotExists(stmt, "apexsions_claims", "last_tax_collected_at", "TIMESTAMP NULL");
            addColumnIfNotExists(stmt, "apexsions_claims", "flags", "TEXT NOT NULL DEFAULT '{}'");
            addColumnIfNotExists(stmt, "apexsions_claims", "roles", "TEXT NOT NULL DEFAULT '{}'");
            addColumnIfNotExists(stmt, "apexsions_claims", "kingdom_id", "VARCHAR(64) NULL");

            // Indexes for claims (Columns now exist)
            createIndexSafe(stmt, "idx_claims_owner", "apexsions_claims(owner_id)");
            createIndexSafe(stmt, "idx_claims_world_chunk", "apexsions_claims(world, chunk_x, chunk_z)");
            createIndexSafe(stmt, "idx_claims_status", "apexsions_claims(status)");
            createIndexSafe(stmt, "idx_claims_kingdom", "apexsions_claims(kingdom_id)");

            // Unified Bans Table
            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS apexsions_bans (" +
                        "id VARCHAR(36) PRIMARY KEY, " +
                        "player_uuid VARCHAR(36) NOT NULL, " +
                        "player_name VARCHAR(32) NOT NULL, " +
                        "ip_address VARCHAR(45), " +
                        "banned_by VARCHAR(64) NOT NULL, " +
                        "reason TEXT NOT NULL, " +
                        "ban_type VARCHAR(16) NOT NULL DEFAULT 'NAME', " +
                        "banned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "expires_at TIMESTAMP NULL, " +
                        "active BOOLEAN NOT NULL DEFAULT TRUE, " +
                        "unbanned_by VARCHAR(64) NULL, " +
                        "unban_reason TEXT NULL, " +
                        "unbanned_at TIMESTAMP NULL);");
            } catch (SQLException e) {
                plugin.getLogger().warning("Error ensuring apexsions_bans table: " + e.getMessage());
            }

            createIndexSafe(stmt, "idx_bans_player_uuid", "apexsions_bans(player_uuid)");
            createIndexSafe(stmt, "idx_bans_player_name", "apexsions_bans(player_name)");
            createIndexSafe(stmt, "idx_bans_ip", "apexsions_bans(ip_address)");
            createIndexSafe(stmt, "idx_bans_active", "apexsions_bans(active)");

            // Seed initial starter kingdoms matching BlueMap world.conf
            try {
                stmt.execute("INSERT INTO regions (id, key, display_name, world_name, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch, enabled) " +
                        "VALUES ('a0000000-0000-0000-0000-000000000001', 'ZENITHAR', 'Zenithar', 'world', -3028.5, 64.0, -5597.5, 0.0, 0.0, 1) " +
                        "ON CONFLICT(key) DO UPDATE SET spawn_x = EXCLUDED.spawn_x, spawn_y = EXCLUDED.spawn_y, spawn_z = EXCLUDED.spawn_z, world_name = EXCLUDED.world_name;");
                stmt.execute("INSERT INTO regions (id, key, display_name, world_name, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch, enabled) " +
                        "VALUES ('a0000000-0000-0000-0000-000000000002', 'SOLTERRA', 'Solterra', 'world', -5843.5, 65.0, 889.5, 0.0, 0.0, 1) " +
                        "ON CONFLICT(key) DO UPDATE SET spawn_x = EXCLUDED.spawn_x, spawn_y = EXCLUDED.spawn_y, spawn_z = EXCLUDED.spawn_z, world_name = EXCLUDED.world_name;");
                stmt.execute("INSERT INTO regions (id, key, display_name, world_name, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch, enabled) " +
                        "VALUES ('a0000000-0000-0000-0000-000000000003', 'SYLVAMOOR', 'Sylvamoor', 'world', -9666.5, 64.0, -4812.5, 0.0, 0.0, 1) " +
                        "ON CONFLICT(key) DO UPDATE SET spawn_x = EXCLUDED.spawn_x, spawn_y = EXCLUDED.spawn_y, spawn_z = EXCLUDED.spawn_z, world_name = EXCLUDED.world_name;");
            } catch (SQLException seedEx) {
                plugin.getLogger().warning("Could not execute ON CONFLICT seed (already seeded or dialect variant): " + seedEx.getMessage());
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed executing table setup", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database connection pool is closed or not initialized");
        }
        return dataSource.getConnection();
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    public <T> CompletableFuture<T> supplyAsync(Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        asyncExecutor.submit(() -> {
            try {
                future.complete(callable.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, asyncExecutor);
    }

    public void shutdown() {
        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException ignored) {
            asyncExecutor.shutdownNow();
        }

        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed.");
        }
    }

    public boolean isUsingFallback() {
        return usingFallback;
    }

    private void addColumnIfNotExists(Statement stmt, String table, String column, String type) {
        try {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type + ";");
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg == null || (!msg.toLowerCase().contains("duplicate column") && !msg.toLowerCase().contains("already exists"))) {
                plugin.getLogger().fine("Column " + column + " already exists or note: " + msg);
            }
        }
    }

    private void createIndexSafe(Statement stmt, String indexName, String tableAndCols) {
        try {
            stmt.execute("CREATE INDEX IF NOT EXISTS " + indexName + " ON " + tableAndCols + ";");
        } catch (SQLException e) {
            plugin.getLogger().warning("Could not create index " + indexName + ": " + e.getMessage());
        }
    }
}
