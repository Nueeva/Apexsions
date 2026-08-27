package com.apex.economy;

import com.apex.economy.gui.core.GuiClickListener;
import com.apex.economy.command.AdminEconomyCommand;
import com.apex.economy.command.AuctionCommand;
import com.apex.economy.command.EconomyCommand;
import com.apex.economy.command.PayCommand;
import com.apex.economy.currency.CurrencyRegistry;
import com.apex.economy.database.EconomyRepository;
import com.apex.economy.integration.ApexsionsCoreHook;
import com.apex.economy.service.AuctionService;
import com.apex.economy.service.CurrencyService;
import com.apex.economy.service.EconomyLeaderboardService;
import com.apex.economy.service.PayService;
import com.apex.economy.trade.TradeManager;
import com.apex.economy.util.ChatInputManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ApexsionsEconomy extends JavaPlugin {

    private static ApexsionsEconomy instance;

    private CurrencyRegistry currencyRegistry;
    private EconomyRepository repository;
    private CurrencyService currencyService;
    private PayService payService;
    private EconomyLeaderboardService leaderboardService;
    private AuctionService auctionService;
    private TradeManager tradeManager;
    private ChatInputManager chatInputManager;
    private ApexsionsCoreHook apexsionsCoreHook;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // 1. Initialize Currency Registry
        this.currencyRegistry = new CurrencyRegistry();

        // 2. Initialize Database
        String dbFile = getConfig().getString("database.file", "economy.db");
        this.repository = new EconomyRepository(this, dbFile);
        this.repository.init();

        // 3. Initialize Hooks & Services
        this.apexsionsCoreHook = new ApexsionsCoreHook(this);
        this.chatInputManager = new ChatInputManager(this);
        this.currencyService = new CurrencyService(this);
        this.payService = new PayService(this);
        this.leaderboardService = new EconomyLeaderboardService(this);
        this.auctionService = new AuctionService(this);
        this.tradeManager = new TradeManager(this);

        // 4. Register Event Listeners
        getServer().getPluginManager().registerEvents(new GuiClickListener(), this);
        getServer().getPluginManager().registerEvents(new com.apex.economy.listener.EconomyPlayerListener(this), this);

        // 5. Register Commands
        registerCommands();

        getLogger().info("=======================================");
        getLogger().info(" Apexsions Economy v" + getDescription().getVersion() + " initialized!");
        getLogger().info(" Registered Currencies: " + currencyRegistry.getAll().size());
        getLogger().info(" Active Auctions Loaded: " + auctionService.getActiveAuctions().size());
        getLogger().info(" Trading System: ENABLED (Kingdom Integrated)");
        getLogger().info("=======================================");
    }

    @Override
    public void onDisable() {
        if (tradeManager != null) {
            tradeManager.cancelAllTradesOnDisable();
        }
        if (repository != null) {
            repository.close();
        }
        getLogger().info("Apexsions Economy safely disabled.");
        instance = null;
    }

    private void registerCommands() {
        EconomyCommand ecoCmd = new EconomyCommand(this);
        PluginCommand ecoPlugin = getCommand("economy");
        if (ecoPlugin != null) {
            ecoPlugin.setExecutor(ecoCmd);
            ecoPlugin.setTabCompleter(ecoCmd);
        } else {
            registerDynamicCommand("economy", ecoCmd, List.of("eco", "uang", "bal"));
        }

        PayCommand payCmd = new PayCommand(this);
        PluginCommand payPlugin = getCommand("pay");
        if (payPlugin != null) {
            payPlugin.setExecutor(payCmd);
            payPlugin.setTabCompleter(payCmd);
        } else {
            registerDynamicCommand("pay", payCmd, List.of("transfer", "kirimuang"));
        }

        AuctionCommand ahCmd = new AuctionCommand(this);
        PluginCommand ahPlugin = getCommand("ah");
        if (ahPlugin != null) {
            ahPlugin.setExecutor(ahCmd);
            ahPlugin.setTabCompleter(ahCmd);
        } else {
            registerDynamicCommand("ah", ahCmd, List.of("auction", "lelang", "pasar"));
        }

        AdminEconomyCommand adminCmd = new AdminEconomyCommand(this);
        PluginCommand adminPlugin = getCommand("aeco");
        if (adminPlugin != null) {
            adminPlugin.setExecutor(adminCmd);
            adminPlugin.setTabCompleter(adminCmd);
        } else {
            registerDynamicCommand("aeco", adminCmd, List.of("admineconomy", "ecoae"));
        }

        com.apex.economy.command.TradeCommand tradeCmd = new com.apex.economy.command.TradeCommand(this);
        PluginCommand tradePlugin = getCommand("trade");
        if (tradePlugin != null) {
            tradePlugin.setExecutor(tradeCmd);
            tradePlugin.setTabCompleter(tradeCmd);
        } else {
            registerDynamicCommand("trade", tradeCmd, List.of("tukar", "barter"));
        }
    }

    private void registerDynamicCommand(String name, CommandExecutor executor, List<String> aliases) {
        try {
            org.bukkit.command.CommandMap commandMap = Bukkit.getCommandMap();
            org.bukkit.command.defaults.BukkitCommand cmd = new org.bukkit.command.defaults.BukkitCommand(name) {
                @Override
                public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
                    return executor.onCommand(sender, this, commandLabel, args);
                }

                @Override
                public List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                    if (executor instanceof TabCompleter tc) {
                        return tc.onTabComplete(sender, this, alias, args);
                    }
                    return super.tabComplete(sender, alias, args);
                }
            };
            cmd.setAliases(aliases);
            commandMap.register("apexeconomy", cmd);
        } catch (Exception e) {
            getLogger().warning("Could not dynamically register fallback command /" + name + ": " + e.getMessage());
        }
    }

    public static ApexsionsEconomy getInstance() { return instance; }
    public CurrencyRegistry getCurrencyRegistry() { return currencyRegistry; }
    public EconomyRepository getRepository() { return repository; }
    public CurrencyService getCurrencyService() { return currencyService; }
    public PayService getPayService() { return payService; }
    public EconomyLeaderboardService getLeaderboardService() { return leaderboardService; }
    public AuctionService getAuctionService() { return auctionService; }
    public TradeManager getTradeManager() { return tradeManager; }
    public ChatInputManager getChatInputManager() { return chatInputManager; }
    public ApexsionsCoreHook getApexsionsCoreHook() { return apexsionsCoreHook; }
}
