package com.apexsions.battlepass;

import com.apexsions.battlepass.command.ABPCommand;
import com.apexsions.battlepass.command.BPCommand;
import com.apexsions.battlepass.currency.CurrencyManager;
import com.apexsions.battlepass.currency.CurrencyService;
import com.apexsions.battlepass.database.PlayerDataRepository;
import com.apexsions.battlepass.database.SQLiteRepository;
import com.apexsions.battlepass.expshop.service.ExpShopService;
import com.apexsions.battlepass.gui.core.GuiClickListener;
import com.apexsions.battlepass.integration.PlaceholderAPIHook;
import com.apexsions.battlepass.integration.VaultHook;
import com.apexsions.battlepass.pass.PassManager;
import com.apexsions.battlepass.player.PlayerManager;
import com.apexsions.battlepass.player.listener.PlayerConnectionListener;
import com.apexsions.battlepass.progression.BattlePassXpService;
import com.apexsions.battlepass.quest.listener.QuestListener;
import com.apexsions.battlepass.quest.manager.QuestManager;
import com.apexsions.battlepass.reward.RewardManager;
import com.apexsions.battlepass.season.SeasonManager;
import com.apexsions.battlepass.shop.ShopManager;
import com.apexsions.battlepass.shop.refresh.ShopRefreshService;
import com.apexsions.battlepass.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ApexsionsBattlepass extends JavaPlugin {

    private static ApexsionsBattlepass instance;

    private PlayerDataRepository repository;
    private PlayerManager playerManager;
    private SeasonManager seasonManager;
    private PassManager passManager;
    private BattlePassXpService xpService;
    private RewardManager rewardManager;
    private CurrencyManager currencyManager;
    private ShopManager shopManager;
    private ShopRefreshService shopRefreshService;
    private ExpShopService expShopService;
    private QuestManager questManager;

    private VaultHook vaultHook;
    private FileConfiguration messagesConfig;
    private FileConfiguration guiConfig;

    private com.apexsions.battlepass.util.ChatInputManager chatInputManager;
    private com.apexsions.battlepass.leaderboard.BattlePassLeaderboardService leaderboardService;
    private com.apexsions.battlepass.shop.refresh.RarityChanceService rarityChanceService;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadMessages();
        loadGuiConfig();

        // 1. Initialize Database
        String storageType = getConfig().getString("storage.type", "SQLITE");
        String sqliteFile = getConfig().getString("storage.sqlite-file", "battlepass.db");
        this.repository = new SQLiteRepository(this, sqliteFile);
        this.repository.init();

        // 2. Initialize Core Services & Managers
        this.seasonManager = new SeasonManager(this);
        this.playerManager = new PlayerManager(this, repository);
        this.passManager = new PassManager(this);
        this.rewardManager = new RewardManager(this);
        this.xpService = new BattlePassXpService(this);
        this.currencyManager = new CurrencyManager(this);
        this.shopManager = new ShopManager(this);
        this.shopRefreshService = new ShopRefreshService(this);
        this.expShopService = new ExpShopService(this);
        this.questManager = new QuestManager(this);
        this.rarityChanceService = new com.apexsions.battlepass.shop.refresh.RarityChanceService(this);
        this.leaderboardService = new com.apexsions.battlepass.leaderboard.BattlePassLeaderboardService(this);
        this.chatInputManager = new com.apexsions.battlepass.util.ChatInputManager(this);

        // 3. Register Event Listeners
        getServer().getPluginManager().registerEvents(this.chatInputManager, this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new QuestListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiClickListener(), this);

        // 4. Register Player (/bp) & Admin (/abp) Commands
        registerCommands();

        // 5. Setup External Hooks
        setupHooks();

        // 6. Load data for online players
        int seasonId = seasonManager.getCurrentSeason().getId();
        for (Player p : Bukkit.getOnlinePlayers()) {
            playerManager.loadPlayerData(p, seasonId);
        }

        // 7. Auto-save async task (every 5 minutes)
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            playerManager.saveAllPlayerData();
        }, 6000L, 6000L);

        getLogger().info("=======================================");
        getLogger().info(" Apexsions Battlepass v" + getDescription().getVersion() + " initialized!");
        getLogger().info(" Active Season: " + seasonManager.getCurrentSeason().getName());
        getLogger().info(" Season State: " + seasonManager.getSeasonState());
        getLogger().info(" Total Pass Tiers Loaded: " + passManager.getPasses().size());
        getLogger().info(" Base Shop Refresh Cost: " + shopRefreshService.getBaseCost() + " Coins");
        getLogger().info("=======================================");
    }

    @Override
    public void onDisable() {
        if (playerManager != null) {
            playerManager.saveAllPlayerData();
        }
        if (repository != null) {
            repository.close();
        }
        getLogger().info("Apexsions Battlepass safely disabled.");
        instance = null;
    }

    private void registerCommands() {
        // Player Command (/bp)
        BPCommand bpCmd = new BPCommand(this);
        PluginCommand bpPluginCmd = getCommand("bp");
        if (bpPluginCmd != null) {
            bpPluginCmd.setExecutor(bpCmd);
            bpPluginCmd.setTabCompleter(bpCmd);
        } else {
            registerFallbackPlayerCommand(bpCmd);
        }

        // Admin Command (/abp)
        ABPCommand abpCmd = new ABPCommand(this);
        PluginCommand abpPluginCmd = getCommand("abp");
        if (abpPluginCmd != null) {
            abpPluginCmd.setExecutor(abpCmd);
            abpPluginCmd.setTabCompleter(abpCmd);
        } else {
            registerFallbackAdminCommand(abpCmd);
        }
    }

    private void setupHooks() {
        // Vault
        this.vaultHook = new VaultHook();
        if (vaultHook.setupEconomy()) {
            getLogger().info("Vault Economy hooked successfully!");
        } else {
            getLogger().info("Vault not found or no Economy provider available.");
        }

        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
            getLogger().info("PlaceholderAPI Expansion registered successfully!");
        }
    }

    public void reloadAllConfigurations() {
        reloadConfig();
        loadMessages();
        loadGuiConfig();
        seasonManager.loadSeasonConfig();
        passManager.loadPasses();
        rewardManager.loadRewards();
        shopManager.loadShop();
        shopRefreshService.loadConfiguration();
        expShopService.loadPackages();
        questManager.loadQuests();
    }

    public void loadMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);
    }

    public void loadGuiConfig() {
        File file = new File(getDataFolder(), "gui.yml");
        if (!file.exists()) {
            saveResource("gui.yml", false);
        }
        guiConfig = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage(String key) {
        if (messagesConfig == null) return key;
        String prefix = messagesConfig.getString("prefix", "&8[&6&lApexsions BP&8] &r");
        String msg = messagesConfig.getString(key, key);
        return ColorUtil.colorize(msg.replace("%prefix%", prefix));
    }

    private void registerFallbackPlayerCommand(BPCommand cmd) {
        try {
            java.lang.reflect.Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            org.bukkit.command.CommandMap commandMap = (org.bukkit.command.CommandMap) commandMapField.get(Bukkit.getServer());

            org.bukkit.command.defaults.BukkitCommand bukkitCmd = new org.bukkit.command.defaults.BukkitCommand("bp") {
                @Override
                public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
                    return cmd.onCommand(sender, this, commandLabel, args);
                }

                @Override
                public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                    java.util.List<String> list = cmd.onTabComplete(sender, this, alias, args);
                    return list != null ? list : super.tabComplete(sender, alias, args);
                }
            };
            bukkitCmd.setAliases(java.util.List.of("battlepass"));
            bukkitCmd.setDescription("Main player command for Apexsions Battlepass");
            bukkitCmd.setPermission("apexsionsbattlepass.use");

            commandMap.register("bp", bukkitCmd);
        } catch (Throwable t) {
            getLogger().warning("Could not register dynamic fallback /bp command: " + t.getMessage());
        }
    }

    private void registerFallbackAdminCommand(ABPCommand cmd) {
        try {
            java.lang.reflect.Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            org.bukkit.command.CommandMap commandMap = (org.bukkit.command.CommandMap) commandMapField.get(Bukkit.getServer());

            org.bukkit.command.defaults.BukkitCommand bukkitCmd = new org.bukkit.command.defaults.BukkitCommand("abp") {
                @Override
                public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
                    return cmd.onCommand(sender, this, commandLabel, args);
                }

                @Override
                public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                    java.util.List<String> list = cmd.onTabComplete(sender, this, alias, args);
                    return list != null ? list : super.tabComplete(sender, alias, args);
                }
            };
            bukkitCmd.setAliases(java.util.List.of("apexsionsbattlepass", "adminbp", "bpadmin"));
            bukkitCmd.setDescription("Admin BattlePass Control Panel");
            bukkitCmd.setPermission("apexsionsbattlepass.admin");

            commandMap.register("abp", bukkitCmd);
        } catch (Throwable t) {
            getLogger().warning("Could not register dynamic fallback /abp command: " + t.getMessage());
        }
    }

    public static ApexsionsBattlepass getInstance() { return instance; }
    public PlayerDataRepository getRepository() { return repository; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public SeasonManager getSeasonManager() { return seasonManager; }
    public PassManager getPassManager() { return passManager; }
    public BattlePassXpService getXpService() { return xpService; }
    public RewardManager getRewardManager() { return rewardManager; }
    public CurrencyService getCurrencyService() { return currencyManager; }
    public ShopManager getShopManager() { return shopManager; }
    public ShopRefreshService getShopRefreshService() { return shopRefreshService; }
    public ExpShopService getExpShopService() { return expShopService; }
    public QuestManager getQuestManager() { return questManager; }
    public VaultHook getVaultHook() { return vaultHook; }
    public FileConfiguration getGuiConfig() { return guiConfig; }
    public com.apexsions.battlepass.util.ChatInputManager getChatInputManager() { return chatInputManager; }
    public com.apexsions.battlepass.leaderboard.BattlePassLeaderboardService getLeaderboardService() { return leaderboardService; }
    public com.apexsions.battlepass.shop.refresh.RarityChanceService getRarityChanceService() { return rarityChanceService; }
}

