package com.apex.economy;

import com.apex.battlepass.gui.core.GuiClickListener;
import com.apex.economy.command.AdminEconomyCommand;
import com.apex.economy.command.AuctionCommand;
import com.apex.economy.command.EconomyCommand;
import com.apex.economy.command.PayCommand;
import com.apex.economy.currency.CurrencyRegistry;
import com.apex.economy.database.EconomyRepository;
import com.apex.economy.service.AuctionService;
import com.apex.economy.service.CurrencyService;
import com.apex.economy.service.EconomyLeaderboardService;
import com.apex.economy.service.PayService;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ApexsionsEconomy extends JavaPlugin {

    private static ApexsionsEconomy instance;

    private CurrencyRegistry currencyRegistry;
    private EconomyRepository repository;
    private CurrencyService currencyService;
    private PayService payService;
    private EconomyLeaderboardService leaderboardService;
    private AuctionService auctionService;
    private com.apex.economy.trade.TradeManager tradeManager;
    private com.apex.battlepass.util.ChatInputManager chatInputManager;

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

        // 3. Initialize Services
        this.chatInputManager = new com.apex.battlepass.util.ChatInputManager(this);
        this.currencyService = new CurrencyService(this);
        this.payService = new PayService(this);
        this.leaderboardService = new EconomyLeaderboardService(this);
        this.auctionService = new AuctionService(this);
        this.tradeManager = new com.apex.economy.trade.TradeManager(this);

        // 4. Register Event Listeners
        getServer().getPluginManager().registerEvents(new GuiClickListener(), this);
        getServer().getPluginManager().registerEvents(new com.apex.economy.listener.EconomyPlayerListener(this), this);

        // 5. Register Commands
        registerCommands();

        getLogger().info("=======================================");
        getLogger().info(" Apexsions Economy v" + getDescription().getVersion() + " initialized!");
        getLogger().info(" Registered Currencies: " + currencyRegistry.getAll().size());
        getLogger().info(" Active Auctions Loaded: " + auctionService.getActiveAuctions().size());
        getLogger().info(" Trading System: ENABLED");
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
            registerDynamicCommand("economy", ecoCmd, java.util.List.of("eco", "uang", "bal"));
        }

        PayCommand payCmd = new PayCommand(this);
        PluginCommand payPlugin = getCommand("pay");
        if (payPlugin != null) {
            payPlugin.setExecutor(payCmd);
            payPlugin.setTabCompleter(payCmd);
        } else {
            registerDynamicCommand("pay", payCmd, java.util.List.of("transfer", "kirimuang"));
        }

        AuctionCommand ahCmd = new AuctionCommand(this);
        PluginCommand ahPlugin = getCommand("ah");
        if (ahPlugin != null) {
            ahPlugin.setExecutor(ahCmd);
            ahPlugin.setTabCompleter(ahCmd);
        } else {
            registerDynamicCommand("ah", ahCmd, java.util.List.of("lelang", "auction"));
        }

        com.apex.economy.command.TradeCommand tradeCmd = new com.apex.economy.command.TradeCommand(this);
        PluginCommand tradePlugin = getCommand("trade");
        if (tradePlugin != null) {
            tradePlugin.setExecutor(tradeCmd);
            tradePlugin.setTabCompleter(tradeCmd);
        } else {
            registerDynamicCommand("trade", tradeCmd, java.util.List.of("barter", "tukar"));
        }

        AdminEconomyCommand adminCmd = new AdminEconomyCommand(this);
        PluginCommand adminPlugin = getCommand("ecoadmin");
        if (adminPlugin != null) {
            adminPlugin.setExecutor(adminCmd);
            adminPlugin.setTabCompleter(adminCmd);
        } else {
            registerDynamicCommand("ecoadmin", adminCmd, java.util.List.of("adminpay", "apexeconomy"));
        }
    }

    private void registerDynamicCommand(String name, org.bukkit.command.CommandExecutor executor, java.util.List<String> aliases) {
        try {
            java.lang.reflect.Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            org.bukkit.command.CommandMap commandMap = (org.bukkit.command.CommandMap) commandMapField.get(Bukkit.getServer());

            org.bukkit.command.defaults.BukkitCommand cmd = new org.bukkit.command.defaults.BukkitCommand(name) {
                @Override
                public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
                    return executor.onCommand(sender, this, commandLabel, args);
                }

                @Override
                public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                    if (executor instanceof org.bukkit.command.TabCompleter tc) {
                        return tc.onTabComplete(sender, this, alias, args);
                    }
                    return super.tabComplete(sender, alias, args);
                }
            };
            if (aliases != null) cmd.setAliases(aliases);
            commandMap.register(name, cmd);
        } catch (Throwable t) {
            getLogger().warning("Could not register dynamic command /" + name + ": " + t.getMessage());
        }
    }

    public static ApexsionsEconomy getInstance() { return instance; }
    public CurrencyRegistry getCurrencyRegistry() { return currencyRegistry; }
    public EconomyRepository getRepository() { return repository; }
    public CurrencyService getCurrencyService() { return currencyService; }
    public PayService getPayService() { return payService; }
    public EconomyLeaderboardService getLeaderboardService() { return leaderboardService; }
    public AuctionService getAuctionService() { return auctionService; }
    public com.apex.economy.trade.TradeManager getTradeManager() { return tradeManager; }
    public com.apex.battlepass.util.ChatInputManager getChatInputManager() { return chatInputManager; }
}

