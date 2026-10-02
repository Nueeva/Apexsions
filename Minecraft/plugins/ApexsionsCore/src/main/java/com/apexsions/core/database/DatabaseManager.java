package com.apexsions.core.database;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
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
            runSqliteMigrationsDirect();
            plugin.getLogger().info("Local SQLite database initialized successfully at " + dbFile.getName());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQLite database fallback!", e);
            throw new RuntimeException("SQLite fallback database initialization failed", e);
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
            runMigrations();
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
            // C-13: Flyway adalah satu-satunya sumber skema — kegagalan migrasi = fail-fast,
            // bukan DDL bayangan. RuntimeException ini ditangkap onEnable -> log SEVERE + disable plugin.
            plugin.getLogger().log(Level.SEVERE, "Flyway database migration failed! ApexsionsCore refuses to start " +
                    "with an unverified schema. Check the migration scripts in db/migration/ and the database " +
                    "connection settings, then restart the server.", e);
            throw new RuntimeException("Database migration failed - aborting startup to prevent schema drift", e);
        }
    }

    /**
     * Runs migration SQL files directly via JDBC for SQLite fallback.
     * Bypasses Flyway (which may not support the bundled SQLite version).
     * Only for fallback; production PostgreSQL uses strict Flyway via runMigrations().
     */
    private void runSqliteMigrationsDirect() {
        String[] migrations = {
            "db/migration/V1__create_players.sql",
            "db/migration/V2__create_regions.sql",
            "db/migration/V3__create_indexes.sql",
            "db/migration/V4__add_claimed_rewards.sql",
            "db/migration/V5__create_land_claims.sql",
            "db/migration/V6__create_unified_bans.sql",
            "db/migration/V7__create_claim_tax_and_flags.sql"
        };
        try (Connection conn = dataSource.getConnection()) {
            for (String path : migrations) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
                    if (in == null) {
                        plugin.getLogger().warning("Migration not found: " + path + " (skipped)");
                        continue;
                    }
                    String sql = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    // Convert PostgreSQL syntax to SQLite-compatible
                    sql = sql.replace("TIMESTAMPTZ", "TEXT");
                    sql = sql.replace("NOW()", "CURRENT_TIMESTAMP");
                    sql = sql.replace("ADD COLUMN IF NOT EXISTS", "ADD COLUMN");
                    // UUID type works in SQLite via type affinity (treated as TEXT), keep it
                    // Split by semicolon, execute each statement
                    for (String stmt : sql.split(";")) {
                        stmt = stmt.trim();
                        if (stmt.isEmpty()) continue;
                        // Skip full-line comments
                        String[] lines = stmt.split("\n");
                        StringBuilder clean = new StringBuilder();
                        for (String line : lines) {
                            String t = line.trim();
                            if (!t.startsWith("--")) {
                                clean.append(line).append("\n");
                            }
                        }
                        stmt = clean.toString().trim();
                        if (stmt.isEmpty()) continue;
                        try (java.sql.Statement st = conn.createStatement()) {
                            st.execute(stmt);
                        } catch (java.sql.SQLException se) {
                            // Ignore "already exists" errors (idempotent)
                            String msg = se.getMessage();
                            if (msg != null && (msg.contains("already exists") || msg.contains("duplicate"))) {
                                continue;
                            }
                            throw se;
                        }
                    }
                    plugin.getLogger().info("Applied migration: " + path);
                }
            }
            plugin.getLogger().info("SQLite direct migrations applied successfully.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING,
                    "Direct SQLite migrations failed (" + e.getMessage() + "). Continuing with best-effort.", e);
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
}
