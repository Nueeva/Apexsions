package com.yourserver.apexsionscore;

import com.yourserver.apexsionscore.api.ApexsionsCoreAPI;
import com.yourserver.apexsionscore.api.ApexsionsCoreAPIImpl;
import com.yourserver.apexsionscore.api.ApexsionsCoreProvider;
import com.yourserver.apexsionscore.cache.PlayerCache;
import com.yourserver.apexsionscore.chat.ChatFormatter;
import com.yourserver.apexsionscore.chat.ChatListener;
import com.yourserver.apexsionscore.command.AdminCommand;
import com.yourserver.apexsionscore.command.KingdomCommand;
import com.yourserver.apexsionscore.command.LobbyCommand;
import com.yourserver.apexsionscore.config.ConfigManager;
import com.yourserver.apexsionscore.database.DatabaseManager;
import com.yourserver.apexsionscore.database.PlayerRepository;
import com.yourserver.apexsionscore.database.RegionRepository;
import com.yourserver.apexsionscore.integration.*;
import com.yourserver.apexsionscore.level.LevelFormula;
import com.yourserver.apexsionscore.level.LevelManager;
import com.yourserver.apexsionscore.level.LevelTitleResolver;
import com.yourserver.apexsionscore.level.reward.RewardManager;
import com.yourserver.apexsionscore.level.xp.XpService;
import com.yourserver.apexsionscore.level.xp.XpSourceRegistry;
import com.yourserver.apexsionscore.player.PlayerDataService;
import com.yourserver.apexsionscore.player.PlayerListener;
import com.yourserver.apexsionscore.player.TerritoryListener;
import com.yourserver.apexsionscore.region.RegionManager;
import com.yourserver.apexsionscore.region.RegionTeleportService;
import com.yourserver.apexsionscore.region.gui.KingdomProfileGUI;
import com.yourserver.apexsionscore.region.gui.LevelRewardsGUI;
import com.yourserver.apexsionscore.region.gui.RegionSelectionGUI;
import com.yourserver.apexsionscore.region.gui.XpGuideGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin entrypoint for KingdomCore on server Apexsions.
 * Designed for Paper 26.2 and Java 25+ with BlueMap and Citizens support.
 */
public class ApexsionsCorePlugin extends JavaPlugin {

    private static ApexsionsCorePlugin instance;

    // Config & Database
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PlayerRepository playerRepository;
    private RegionRepository regionRepository;

    // Cache & Core Services
    private PlayerCache playerCache;
    private PlayerDataService playerDataService;
    private RegionManager regionManager;
    private RegionTeleportService regionTeleportService;
    private RegionSelectionGUI regionSelectionGUI;
    private KingdomProfileGUI kingdomProfileGUI;
    private LevelRewardsGUI levelRewardsGUI;
    private XpGuideGUI xpGuideGUI;

    // Progression & Rewards
    private LevelFormula levelFormula;
    private LevelTitleResolver levelTitleResolver;
    private LevelManager levelManager;
    private RewardManager rewardManager;
    private XpSourceRegistry xpSourceRegistry;
    private XpService xpService;

    // Chat System
    private ChatFormatter chatFormatter;

    // Integrations
    private LuckPermsHook luckPermsHook;
    private VaultHook vaultHook;
    private PlaceholderApiHook placeholderApiHook;
    private EssentialsHook essentialsHook;
    private BlueMapHook blueMapHook;
    private CitizensHook citizensHook;
    private TerritoryListener territoryListener;

    // Public API
    private ApexsionsCoreAPI api;

    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();

        getLogger().info("=========================================");
        getLogger().info("Starting ApexsionsCore v" + getPluginMeta().getVersion() + " (Apexsions / Paper 26.2)...");
        getLogger().info("=========================================");

        try {
            // 1. Configuration (Modular YAMLs)
            this.configManager = new ConfigManager(this);
            this.configManager.load();

            // 2. Database & Repositories
            this.databaseManager = new DatabaseManager(this, configManager);
            this.databaseManager.initialize();
            this.playerRepository = new PlayerRepository(this, databaseManager);
            this.regionRepository = new RegionRepository(this, databaseManager);

            // 3. Caches & Player Services
            this.playerCache = new PlayerCache();
            this.playerDataService = new PlayerDataService(this, playerRepository, playerCache);

            // 4. Regions & GUIs
            this.regionManager = new RegionManager(this, regionRepository);
            this.regionManager.loadRegions();
            this.regionTeleportService = new RegionTeleportService(this);

            this.regionSelectionGUI = new RegionSelectionGUI(this);
            Bukkit.getPluginManager().registerEvents(regionSelectionGUI, this);

            this.kingdomProfileGUI = new KingdomProfileGUI(this);
            Bukkit.getPluginManager().registerEvents(kingdomProfileGUI, this);

            this.levelRewardsGUI = new LevelRewardsGUI(this);
            Bukkit.getPluginManager().registerEvents(levelRewardsGUI, this);

            this.xpGuideGUI = new XpGuideGUI(this);
            Bukkit.getPluginManager().registerEvents(xpGuideGUI, this);

            // 5. Progression (Level, XP, Rewards)
            this.levelFormula = new LevelFormula(configManager);
            this.levelTitleResolver = new LevelTitleResolver(this);
            this.levelManager = new LevelManager(this, levelFormula, levelTitleResolver);
            this.rewardManager = new RewardManager(this);
            this.rewardManager.loadRewards();

            this.xpService = new XpService(this);
            this.xpSourceRegistry = new XpSourceRegistry(this);
            this.xpSourceRegistry.registerAll();

            // 6. Listeners
            Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
            this.territoryListener = new TerritoryListener(this);
            Bukkit.getPluginManager().registerEvents(territoryListener, this);

            // 7. Chat System (Only register fallback if ApexsionsChat is not installed)
            if (this.configManager.isChatEnabled() && !Bukkit.getPluginManager().isPluginEnabled("ApexsionsChat")) {
                this.chatFormatter = new ChatFormatter(this);
                Bukkit.getPluginManager().registerEvents(new ChatListener(this, chatFormatter), this);
            }

            // 8. Integrations
            this.luckPermsHook = new LuckPermsHook(this);
            this.luckPermsHook.initialize();

            this.vaultHook = new VaultHook(this);
            this.vaultHook.initialize();

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                this.placeholderApiHook = new PlaceholderApiHook(this);
                this.placeholderApiHook.register();
                getLogger().info("PlaceholderAPI expansion registered successfully.");
            }

            this.essentialsHook = new EssentialsHook(this);
            this.essentialsHook.initialize();
            Bukkit.getPluginManager().registerEvents(new TpaRestrictionListener(this), this);

            this.blueMapHook = new BlueMapHook(this);
            this.blueMapHook.initialize();

            this.citizensHook = new CitizensHook(this);
            this.citizensHook.initialize();

            // 9. Commands
            registerCommands();

            // 10. Public API
            this.api = new ApexsionsCoreAPIImpl(this);
            ApexsionsCoreProvider.register(this.api);

            long elapsed = System.currentTimeMillis() - startTime;
            getLogger().info("ApexsionsCore loaded and enabled successfully in " + elapsed + "ms!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Critical failure during ApexsionsCore startup!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ApexsionsCore...");

        // Unregister Public API
        ApexsionsCoreProvider.unregister();

        // Flush all cached player profiles safely to database
        if (playerDataService != null) {
            playerDataService.flushAll();
        }

        // Shutdown database connection pool
        if (databaseManager != null) {
            databaseManager.shutdown();
        }

        instance = null;
        getLogger().info("ApexsionsCore has been safely disabled.");
    }

    private void registerCommands() {
        // /lobby
        PluginCommand lobbyCmd = getCommand("lobby");
        if (lobbyCmd != null) {
            lobbyCmd.setExecutor(new LobbyCommand(this));
        }

        // /region (aliases: /kingdom, /k, /kingdoms)
        KingdomCommand regionHandler = new KingdomCommand(this);
        PluginCommand regionCmd = getCommand("region");
        if (regionCmd != null) {
            regionCmd.setExecutor(regionHandler);
            regionCmd.setTabCompleter(regionHandler);
        }
        PluginCommand kingdomCmd = getCommand("kingdom");
        if (kingdomCmd != null) {
            kingdomCmd.setExecutor(regionHandler);
            kingdomCmd.setTabCompleter(regionHandler);
        }

        // /apexsionscore (aliases: /ac, /apexionscore, /kingdomcore, /kc)
        AdminCommand adminHandler = new AdminCommand(this);
        PluginCommand adminCmd = getCommand("apexsionscore");
        if (adminCmd != null) {
            adminCmd.setExecutor(adminHandler);
            adminCmd.setTabCompleter(adminHandler);
        }
        PluginCommand aliasAdminCmd = getCommand("apexionscore");
        if (aliasAdminCmd != null) {
            aliasAdminCmd.setExecutor(adminHandler);
            aliasAdminCmd.setTabCompleter(adminHandler);
        }
        PluginCommand legacyAdminCmd = getCommand("kingdomcore");
        if (legacyAdminCmd != null) {
            legacyAdminCmd.setExecutor(adminHandler);
            legacyAdminCmd.setTabCompleter(adminHandler);
        }

        // /level (aliases: /lvl, /profile, /rewards, /exp, /xpguide)
        PluginCommand levelCmd = getCommand("level");
        if (levelCmd != null) {
            levelCmd.setExecutor(regionHandler);
            levelCmd.setTabCompleter(regionHandler);
        }
    }

    public static ApexsionsCorePlugin getInstance() { return instance; }

    public ConfigManager getConfigManager() { return configManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public PlayerRepository getPlayerRepository() { return playerRepository; }
    public RegionRepository getRegionRepository() { return regionRepository; }
    public PlayerCache getPlayerCache() { return playerCache; }
    public PlayerDataService getPlayerDataService() { return playerDataService; }
    public RegionManager getRegionManager() { return regionManager; }
    public RegionTeleportService getRegionTeleportService() { return regionTeleportService; }
    public RegionSelectionGUI getRegionSelectionGUI() { return regionSelectionGUI; }
    public KingdomProfileGUI getKingdomProfileGUI() { return kingdomProfileGUI; }
    public LevelRewardsGUI getLevelRewardsGUI() { return levelRewardsGUI; }
    public XpGuideGUI getXpGuideGUI() { return xpGuideGUI; }
    public LevelFormula getLevelFormula() { return levelFormula; }
    public LevelTitleResolver getLevelTitleResolver() { return levelTitleResolver; }
    public LevelManager getLevelManager() { return levelManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public XpSourceRegistry getXpSourceRegistry() { return xpSourceRegistry; }
    public XpService getXpService() { return xpService; }
    public ChatFormatter getChatFormatter() { return chatFormatter; }
    public LuckPermsHook getLuckPermsHook() { return luckPermsHook; }
    public VaultHook getVaultHook() { return vaultHook; }
    public PlaceholderApiHook getPlaceholderApiHook() { return placeholderApiHook; }
    public EssentialsHook getEssentialsHook() { return essentialsHook; }
    public BlueMapHook getBlueMapHook() { return blueMapHook; }
    public CitizensHook getCitizensHook() { return citizensHook; }
    public FileConfiguration getXpConfig() { return configManager.getXpConfig(); }
    public FileConfiguration getRewardsConfig() { return configManager.getRewardsConfig(); }
    public FileConfiguration getMessagesConfig() { return configManager.getMessagesConfig(); }
    public FileConfiguration getGuiConfig() { return configManager.getGuiConfig(); }
    public FileConfiguration getChatConfig() { return configManager.getChatConfig(); }
    public TerritoryListener getTerritoryListener() { return territoryListener; }
    public ApexsionsCoreAPI getApi() { return api; }
}
