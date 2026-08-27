package com.yourserver.apexsionscore.config;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Manages modular configurations across dedicated YAML files:
 * config.yml, kingdoms.yml, titles.yml, xp.yml, rewards.yml, messages.yml, gui.yml, chat.yml.
 */
public class ConfigManager {

    private final ApexsionsCorePlugin plugin;

    // File Configurations
    private FileConfiguration mainConfig;
    private FileConfiguration kingdomsConfig;
    private FileConfiguration titlesConfig;
    private FileConfiguration xpConfig;
    private FileConfiguration rewardsConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration guiConfig;
    private FileConfiguration chatConfig;
    private FileConfiguration ranksConfig;

    // Parsed Main Config Fields
    private String serverName;
    private String dbType;
    private String dbHost;
    private int dbPort;
    private String dbName;
    private String dbUser;
    private String dbPassword;
    private int dbMaxPoolSize;
    private int dbMinIdle;
    private long dbIdleTimeout;
    private long dbConnectionTimeout;
    private long dbMaxLifetime;
    private boolean dbAutoFallback;

    // Lobby
    private Location lobbyLocation;

    // Level Formula
    private int levelMin;
    private int levelMax;
    private double formulaBase;
    private double formulaExponent;
    private boolean allowOverflow;

    public ConfigManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.mainConfig = loadCustomFile("config.yml");
        this.kingdomsConfig = loadCustomFile("kingdoms.yml");
        this.titlesConfig = loadCustomFile("titles.yml");
        this.xpConfig = loadCustomFile("xp.yml");
        this.rewardsConfig = loadCustomFile("rewards.yml");
        this.messagesConfig = loadCustomFile("messages.yml");
        this.guiConfig = loadCustomFile("gui.yml");
        this.chatConfig = loadCustomFile("chat.yml");
        this.ranksConfig = loadCustomFile("ranks.yml");

        // Server
        this.serverName = mainConfig.getString("server.name", "Apexsions");

        // Database with Hosting Environment Variables resolution
        this.dbType = getEnvOrDefault("DB_TYPE", mainConfig.getString("database.type", "postgresql"));
        this.dbHost = getEnvOrDefault("DB_HOST", getEnvOrDefault("DATABASE_HOST", getEnvOrDefault("POSTGRES_HOST", mainConfig.getString("database.host", "localhost"))));
        this.dbPort = getEnvIntOrDefault("DB_PORT", getEnvIntOrDefault("DATABASE_PORT", getEnvIntOrDefault("POSTGRES_PORT", mainConfig.getInt("database.port", 5432))));
        this.dbName = getEnvOrDefault("DB_NAME", getEnvOrDefault("DATABASE_NAME", getEnvOrDefault("POSTGRES_DB", mainConfig.getString("database.database", "kingdomcore"))));
        this.dbUser = getEnvOrDefault("DB_USER", getEnvOrDefault("DATABASE_USER", getEnvOrDefault("POSTGRES_USER", mainConfig.getString("database.username", "postgres"))));
        this.dbPassword = getEnvOrDefault("DB_PASSWORD", getEnvOrDefault("DATABASE_PASSWORD", getEnvOrDefault("POSTGRES_PASSWORD", mainConfig.getString("database.password", "password"))));

        // Check if DATABASE_URL or POSTGRES_URL connection string is provided
        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = System.getenv("POSTGRES_URL");
        }
        if (dbUrl != null && !dbUrl.isEmpty()) {
            parseDatabaseUrl(dbUrl);
        }

        this.dbMaxPoolSize = mainConfig.getInt("database.pool.maximum-pool-size", 10);
        this.dbMinIdle = mainConfig.getInt("database.pool.minimum-idle", 2);
        this.dbIdleTimeout = mainConfig.getLong("database.pool.idle-timeout", 600000);
        this.dbConnectionTimeout = mainConfig.getLong("database.pool.connection-timeout", 30000);
        this.dbMaxLifetime = mainConfig.getLong("database.pool.max-lifetime", 1800000);
        this.dbAutoFallback = mainConfig.getBoolean("database.auto-fallback", true);

        // Lobby
        String worldName = mainConfig.getString("locations.lobby.world", "world");
        double x = mainConfig.getDouble("locations.lobby.x", 0.5);
        double y = mainConfig.getDouble("locations.lobby.y", 100.0);
        double z = mainConfig.getDouble("locations.lobby.z", 0.5);
        float yaw = (float) mainConfig.getDouble("locations.lobby.yaw", 0.0);
        float pitch = (float) mainConfig.getDouble("locations.lobby.pitch", 0.0);
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            this.lobbyLocation = new Location(world, x, y, z, yaw, pitch);
        } else {
            this.lobbyLocation = null;
        }

        // Level
        this.levelMin = mainConfig.getInt("level.min", 1);
        this.levelMax = mainConfig.getInt("level.max", 100);
        this.formulaBase = mainConfig.getDouble("level.formula.base", 100.0);
        this.formulaExponent = mainConfig.getDouble("level.formula.exponent", 1.5);
        this.allowOverflow = mainConfig.getBoolean("level.allow-overflow", false);
    }

    private FileConfiguration loadCustomFile(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(fileName, false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Merge defaults
        InputStream defStream = plugin.getResource(fileName);
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8));
            config.setDefaults(defConfig);
        }
        return config;
    }

    public FileConfiguration getMainConfig() { return mainConfig; }
    public FileConfiguration getKingdomsConfig() { return kingdomsConfig; }
    public FileConfiguration getTitlesConfig() { return titlesConfig; }
    public FileConfiguration getXpConfig() { return xpConfig; }
    public FileConfiguration getRewardsConfig() { return rewardsConfig; }
    public FileConfiguration getMessagesConfig() { return messagesConfig; }
    public FileConfiguration getGuiConfig() { return guiConfig; }
    public FileConfiguration getChatConfig() { return chatConfig; }

    public String getServerName() { return serverName; }
    public String getDbType() { return dbType; }
    public String getDbHost() { return dbHost; }
    public int getDbPort() { return dbPort; }
    public String getDbName() { return dbName; }
    public String getDbUser() { return dbUser; }
    public String getDbPassword() { return dbPassword; }
    public int getDbMaxPoolSize() { return dbMaxPoolSize; }
    public int getDbMinIdle() { return dbMinIdle; }
    public long getDbIdleTimeout() { return dbIdleTimeout; }
    public long getDbConnectionTimeout() { return dbConnectionTimeout; }
    public long getDbMaxLifetime() { return dbMaxLifetime; }
    public boolean isDbAutoFallback() { return dbAutoFallback; }

    public Location getLobbyLocation() {
        if (lobbyLocation == null) {
            String worldName = mainConfig.getString("locations.lobby.world", "world");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                double x = mainConfig.getDouble("locations.lobby.x", 0.5);
                double y = mainConfig.getDouble("locations.lobby.y", 100.0);
                double z = mainConfig.getDouble("locations.lobby.z", 0.5);
                float yaw = (float) mainConfig.getDouble("locations.lobby.yaw", 0.0);
                float pitch = (float) mainConfig.getDouble("locations.lobby.pitch", 0.0);
                lobbyLocation = new Location(world, x, y, z, yaw, pitch);
            }
        }
        return lobbyLocation;
    }

    public int getLevelMin() { return levelMin; }
    public int getLevelMax() { return levelMax; }
    public double getFormulaBase() { return formulaBase; }
    public double getFormulaExponent() { return formulaExponent; }
    public boolean isAllowOverflow() { return allowOverflow; }

    public int getBlockTrackerCacheSize() { return xpConfig != null ? xpConfig.getInt("anti-abuse.block-tracker-cache-size", 50000) : 50000; }
    public int getBlockTrackerExpireHours() { return xpConfig != null ? xpConfig.getInt("anti-abuse.block-tracker-expire-hours", 24) : 24; }
    public int getPvpKillCooldownSeconds() { return xpConfig != null ? xpConfig.getInt("anti-abuse.pvp-kill-cooldown-seconds", 120) : 120; }
    public int getJumpCooldownSeconds() { return xpConfig != null ? xpConfig.getInt("anti-abuse.movement-jump-cooldown-seconds", 5) : 5; }

    public String getGuiRegionChooseTitle() {
        return guiConfig != null ? guiConfig.getString("kingdom-select.title", "<dark_gray><bold>⚔ Choose Your Kingdom ⚔</bold></dark_gray>") : "<dark_gray><bold>⚔ Choose Your Kingdom ⚔</bold></dark_gray>";
    }

    public int getGuiRegionChooseSize() {
        return guiConfig != null ? guiConfig.getInt("kingdom-select.size", 45) : 45;
    }

    public boolean isChatEnabled() {
        return chatConfig != null ? chatConfig.getBoolean("enabled", true) : true;
    }

    public FileConfiguration getRanksConfig() { return ranksConfig; }

    public String getChatFormat() {
        return chatConfig != null ? chatConfig.getString("format", "<gray>[<yellow>Lv. {level} <gold>{title}</gold></yellow>]</gray> <gray>[<white>{rank}</white>]</gray> {kingdom} <white>{player}</white> <dark_gray>»</dark_gray> <white>{message}</white>") : "<gray>[<yellow>Lv. {level} <gold>{title}</gold></yellow>]</gray> <gray>[<white>{rank}</white>]</gray> {kingdom} <white>{player}</white> <dark_gray>»</dark_gray> <white>{message}</white>";
    }

    public String getKingdomChatTag(String kingdomKey) {
        if (chatConfig == null) {
            return "<gray>[None]</gray>";
        }
        if (kingdomKey == null || kingdomKey.isEmpty() || kingdomKey.equalsIgnoreCase("NONE")) {
            return chatConfig.getString("kingdom-tags.none", "<gray>[Unpledged]</gray>");
        }
        return chatConfig.getString("kingdom-tags." + kingdomKey.toUpperCase(), "<gold>[" + kingdomKey + "]</gold>");
    }

    public String getDefaultRank() {
        return chatConfig != null ? chatConfig.getString("default-rank", "Wanderer") : "Wanderer";
    }

    public ConfigurationSection getSection(String path) {
        if (kingdomsConfig != null && kingdomsConfig.contains(path)) return kingdomsConfig.getConfigurationSection(path);
        if (mainConfig != null && mainConfig.contains(path)) return mainConfig.getConfigurationSection(path);
        if (xpConfig != null && xpConfig.contains(path)) return xpConfig.getConfigurationSection(path);
        if (rewardsConfig != null && rewardsConfig.contains(path)) return rewardsConfig.getConfigurationSection(path);
        if (chatConfig != null && chatConfig.contains(path)) return chatConfig.getConfigurationSection(path);
        return null;
    }

    public String getDefaultRegion() { return "None"; }

    private String getEnvOrDefault(String key, String def) {
        String val = System.getenv(key);
        return (val != null && !val.trim().isEmpty()) ? val.trim() : def;
    }

    private int getEnvIntOrDefault(String key, int def) {
        String val = System.getenv(key);
        if (val != null && !val.trim().isEmpty()) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    private void parseDatabaseUrl(String url) {
        try {
            String cleanUrl = url.startsWith("jdbc:") ? url.substring(5) : url;
            java.net.URI uri = new java.net.URI(cleanUrl);
            if (uri.getHost() != null) this.dbHost = uri.getHost();
            if (uri.getPort() > 0) this.dbPort = uri.getPort();
            if (uri.getPath() != null && uri.getPath().length() > 1) this.dbName = uri.getPath().substring(1);
            if (uri.getUserInfo() != null) {
                String[] userPass = uri.getUserInfo().split(":", 2);
                this.dbUser = userPass[0];
                if (userPass.length > 1) this.dbPassword = userPass[1];
            }
            this.dbType = "postgresql";
        } catch (Exception e) {
            plugin.getLogger().warning("Could not parse database URL environment variable: " + e.getMessage());
        }
    }
}
