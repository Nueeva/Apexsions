package com.apexsions.core;

import com.apexsions.core.api.ApexsionsCoreAPI;
import com.apexsions.core.api.ApexsionsCoreAPIImpl;
import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.core.cache.PlayerCache;
import com.apexsions.core.chat.ChatFormatter;
import com.apexsions.core.chat.ChatListener;
import com.apexsions.core.command.AdminCommand;
import com.apexsions.core.command.KingdomCommand;
import com.apexsions.core.command.LobbyCommand;
import com.apexsions.core.command.RtpCommand;
import com.apexsions.core.config.ConfigManager;
import com.apexsions.core.database.DatabaseManager;
import com.apexsions.core.database.PlayerRepository;
import com.apexsions.core.database.RegionRepository;
import com.apexsions.core.integration.*;
import com.apexsions.core.level.LevelFormula;
import com.apexsions.core.level.LevelManager;
import com.apexsions.core.level.LevelTitleResolver;
import com.apexsions.core.level.reward.RewardManager;
import com.apexsions.core.level.xp.XpService;
import com.apexsions.core.level.xp.XpSourceRegistry;
import com.apexsions.core.player.PlayerDataService;
import com.apexsions.core.player.PlayerListener;
import com.apexsions.core.player.TerritoryListener;
import com.apexsions.core.region.KingdomRtpService;
import com.apexsions.core.region.RegionManager;
import com.apexsions.core.region.RegionTeleportService;
import com.apexsions.core.region.gui.KingdomProfileGUI;
import com.apexsions.core.region.gui.KingdomTopGUI;
import com.apexsions.core.region.gui.LevelRewardsGUI;
import com.apexsions.core.region.gui.RegionSelectionGUI;
import com.apexsions.core.region.gui.XpGuideGUI;
import com.apexsions.core.war.CombatTagService;
import com.apexsions.core.war.WarManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin entrypoint for ApexsionsCore on server Apexsions.
 * Designed for Paper 26.2 and Java 21+ with BlueMap and Citizens support.
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
    private KingdomRtpService kingdomRtpService;
    private RegionSelectionGUI regionSelectionGUI;
    private KingdomProfileGUI kingdomProfileGUI;
    private KingdomTopGUI kingdomTopGUI;
    private LevelRewardsGUI levelRewardsGUI;
    private XpGuideGUI xpGuideGUI;

    // War & Combat
    private CombatTagService combatTagService;
    private WarManager warManager;

    // Warp System
    private com.apexsions.core.warp.WarpManager warpManager;

    // Admin Hub
    private com.apexsions.core.admin.AdminChatInputManager adminChatInputManager;
    private com.apexsions.core.admin.AdminHubManager adminHubManager;

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

    // Rank, Titles & Cosmetics
    private com.apexsions.core.rank.RankAnimationManager rankAnimationManager;
    private com.apexsions.core.title.TitleManager titleManager;
    private com.apexsions.core.cosmetics.CosmeticsManager cosmeticsManager;

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
            this.kingdomRtpService = new KingdomRtpService(this);

            this.regionSelectionGUI = new RegionSelectionGUI(this);
            Bukkit.getPluginManager().registerEvents(regionSelectionGUI, this);

            this.kingdomProfileGUI = new KingdomProfileGUI(this);
            Bukkit.getPluginManager().registerEvents(kingdomProfileGUI, this);

            this.kingdomTopGUI = new KingdomTopGUI(this);
            Bukkit.getPluginManager().registerEvents(kingdomTopGUI, this);

            Bukkit.getPluginManager().registerEvents(new com.apexsions.core.region.gui.KingdomInfoGUI(this), this);
            Bukkit.getPluginManager().registerEvents(new com.apexsions.core.region.gui.KingdomConfirmGUI(this), this);

            this.levelRewardsGUI = new LevelRewardsGUI(this);
            Bukkit.getPluginManager().registerEvents(levelRewardsGUI, this);

            this.xpGuideGUI = new XpGuideGUI(this);
            Bukkit.getPluginManager().registerEvents(xpGuideGUI, this);

            this.combatTagService = new CombatTagService(this);
            this.warManager = new WarManager(this);

            // 5. Warp System & Admin Hub
            this.warpManager = new com.apexsions.core.warp.WarpManager(this);
            Bukkit.getPluginManager().registerEvents(new com.apexsions.core.gui.warp.WarpGUIListener(), this);
            Bukkit.getPluginManager().registerEvents(new com.apexsions.core.war.KingdomProtectionListener(this), this);

            this.adminChatInputManager = new com.apexsions.core.admin.AdminChatInputManager(this);
            this.adminHubManager = new com.apexsions.core.admin.AdminHubManager(this);
            Bukkit.getPluginManager().registerEvents(new com.apexsions.core.gui.admin.AdminHubListener(), this);

            // 6. Progression (Level, XP, Rewards)
            this.levelFormula = new LevelFormula(configManager);
            this.levelTitleResolver = new LevelTitleResolver(this);
            this.levelManager = new LevelManager(this, levelFormula, levelTitleResolver);
            this.rewardManager = new RewardManager(this);
            this.rewardManager.loadRewards();

            this.xpService = new XpService(this);
            this.xpSourceRegistry = new XpSourceRegistry(this);
            this.xpSourceRegistry.registerAll();

            // 7. Rank Animation, Titles & Particle Cosmetics Engine
            this.rankAnimationManager = new com.apexsions.core.rank.RankAnimationManager(this);
            this.rankAnimationManager.start();

            this.titleManager = new com.apexsions.core.title.TitleManager(this);

            this.cosmeticsManager = new com.apexsions.core.cosmetics.CosmeticsManager(this);
            this.cosmeticsManager.start();
            Bukkit.getPluginManager().registerEvents(this.cosmeticsManager, this);
            Bukkit.getPluginManager().registerEvents(new com.apexsions.core.cosmetics.gui.CosmeticsGUIListener(), this);

            // 8. Listeners
            Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
            this.territoryListener = new TerritoryListener(this);
            Bukkit.getPluginManager().registerEvents(territoryListener, this);

            // 9. Chat System (Only register fallback if ApexsionsChat is not installed)
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
                this.placeholderApiHook = new PlaceholderApiHook(this, "apexsions");
                this.placeholderApiHook.register();
                new PlaceholderApiHook(this, "apexsionscore").register();
                getLogger().info("PlaceholderAPI expansions (%apexsions_*% and %apexsionscore_*%) registered successfully.");
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

        // Stop Cosmetics and Rank Animation
        if (rankAnimationManager != null) {
            rankAnimationManager.stop();
        }
        if (cosmeticsManager != null) {
            cosmeticsManager.stop();
        }

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

        // /rtp (aliases: /wild, /wilderness, /krtp, /randomteleport, /rtpkingdom)
        com.apexsions.core.command.RtpCommand rtpHandler = new com.apexsions.core.command.RtpCommand(this);
        PluginCommand rtpCmd = getCommand("rtp");
        if (rtpCmd != null) {
            rtpCmd.setExecutor(rtpHandler);
            rtpCmd.setTabCompleter(rtpHandler);
        }
        PluginCommand wildCmd = getCommand("wild");
        if (wildCmd != null) {
            wildCmd.setExecutor(rtpHandler);
            wildCmd.setTabCompleter(rtpHandler);
        }

        // /warp (aliases: /warps, /warpmgr, /warpadmin)
        com.apexsions.core.command.WarpCommand warpHandler = new com.apexsions.core.command.WarpCommand(this);
        PluginCommand warpCmd = getCommand("warp");
        if (warpCmd != null) {
            warpCmd.setExecutor(warpHandler);
            warpCmd.setTabCompleter(warpHandler);
        }
        PluginCommand warpsCmd = getCommand("warps");
        if (warpsCmd != null) {
            warpsCmd.setExecutor(warpHandler);
            warpsCmd.setTabCompleter(warpHandler);
        }
        PluginCommand warpMgrCmd = getCommand("warpmgr");
        if (warpMgrCmd != null) {
            warpMgrCmd.setExecutor(warpHandler);
            warpMgrCmd.setTabCompleter(warpHandler);
        }

        // /admingui (aliases: /apexadmin, /aadmin, /aa)
        com.apexsions.core.command.AdminHubCommand hubHandler = new com.apexsions.core.command.AdminHubCommand(this);
        PluginCommand adminGuiCmd = getCommand("admingui");
        if (adminGuiCmd != null) {
            adminGuiCmd.setExecutor(hubHandler);
            adminGuiCmd.setTabCompleter(hubHandler);
        }
        PluginCommand apexAdminCmd = getCommand("apexadmin");
        if (apexAdminCmd != null) {
            apexAdminCmd.setExecutor(hubHandler);
            apexAdminCmd.setTabCompleter(hubHandler);
        }
        PluginCommand aAdminCmd = getCommand("aadmin");
        if (aAdminCmd != null) {
            aAdminCmd.setExecutor(hubHandler);
            aAdminCmd.setTabCompleter(hubHandler);
        }

        // /titles (aliases: /tags, /title, /tag)
        com.apexsions.core.command.TitlesCommand titlesHandler = new com.apexsions.core.command.TitlesCommand(this);
        PluginCommand titlesCmd = getCommand("titles");
        if (titlesCmd != null) {
            titlesCmd.setExecutor(titlesHandler);
            titlesCmd.setTabCompleter(titlesHandler);
        }

        // /cosmetics (aliases: /aura, /auras, /trail, /trails)
        com.apexsions.core.command.CosmeticsCommand cosmeticsHandler = new com.apexsions.core.command.CosmeticsCommand(this);
        PluginCommand cosmeticsCmd = getCommand("cosmetics");
        if (cosmeticsCmd != null) {
            cosmeticsCmd.setExecutor(cosmeticsHandler);
            cosmeticsCmd.setTabCompleter(cosmeticsHandler);
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
    public KingdomRtpService getKingdomRtpService() { return kingdomRtpService; }
    public RegionSelectionGUI getRegionSelectionGUI() { return regionSelectionGUI; }
    public KingdomProfileGUI getKingdomProfileGUI() { return kingdomProfileGUI; }
    public KingdomTopGUI getKingdomTopGUI() { return kingdomTopGUI; }
    public CombatTagService getCombatTagService() { return combatTagService; }
    public CombatTagService getCombatTagManager() { return combatTagService; }
    public WarManager getWarManager() { return warManager; }
    public WarManager getKingdomWarManager() { return warManager; }
    public com.apexsions.core.warp.WarpManager getWarpManager() { return warpManager; }
    public com.apexsions.core.admin.AdminChatInputManager getAdminChatInputManager() { return adminChatInputManager; }
    public com.apexsions.core.admin.AdminHubManager getAdminHubManager() { return adminHubManager; }
    public com.apexsions.core.rank.RankAnimationManager getRankAnimationManager() { return rankAnimationManager; }
    public com.apexsions.core.title.TitleManager getTitleManager() { return titleManager; }
    public com.apexsions.core.cosmetics.CosmeticsManager getCosmeticsManager() { return cosmeticsManager; }
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
