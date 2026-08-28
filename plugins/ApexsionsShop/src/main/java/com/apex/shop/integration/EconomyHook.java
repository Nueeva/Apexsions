package com.apex.shop.integration;

import com.apex.economy.api.ApexsionsEconomyAPI;
import com.apex.shop.ApexsionsShop;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHook {

    private final ApexsionsShop plugin;
    private Economy vaultEconomy;
    private boolean useApexsionsEconomy = false;

    public EconomyHook(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsEconomy")) {
            useApexsionsEconomy = true;
            plugin.getLogger().info("Successfully hooked into ApexsionsEconomy API.");
        } else if (setupVault()) {
            plugin.getLogger().info("Hooked into Vault Economy provider.");
        } else {
            plugin.getLogger().warning("No external Economy found! Shop will use internal transaction fallback.");
        }
    }

    private boolean setupVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        vaultEconomy = rsp.getProvider();
        return vaultEconomy != null;
    }

    public double getBalance(Player player) {
        if (useApexsionsEconomy) {
            return ApexsionsEconomyAPI.getBalance(player.getUniqueId(), "rupiah");
        }
        if (vaultEconomy != null) {
            return vaultEconomy.getBalance(player);
        }
        return 0.0;
    }

    public boolean has(Player player, double amount) {
        return getBalance(player) >= amount;
    }

    public boolean withdraw(Player player, double amount) {
        if (amount <= 0) return true;
        if (useApexsionsEconomy) {
            return ApexsionsEconomyAPI.withdraw(player.getUniqueId(), "rupiah", amount);
        }
        if (vaultEconomy != null) {
            return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
        }
        return false;
    }

    public boolean deposit(Player player, double amount) {
        if (amount <= 0) return true;
        if (useApexsionsEconomy) {
            ApexsionsEconomyAPI.deposit(player.getUniqueId(), "rupiah", amount);
            return true;
        }
        if (vaultEconomy != null) {
            return vaultEconomy.depositPlayer(player, amount).transactionSuccess();
        }
        return false;
    }

    public String format(double amount) {
        String symbol = plugin.getConfig().getString("economy.currency-symbol", "Rp ");
        return symbol + String.format("%,.0f", amount).replace(',', '.');
    }
}
