package com.apex.battlepass.integration;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private Economy economy;

    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public void deposit(Player player, double amount) {
        if (hasEconomy()) {
            economy.depositPlayer(player, amount);
        }
    }

    public double getBalance(Player player) {
        if (hasEconomy() && player != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }

    public boolean has(Player player, double amount) {
        if (hasEconomy() && player != null) {
            return economy.has(player, amount);
        }
        return false;
    }

    public boolean withdraw(Player player, double amount) {
        if (hasEconomy() && player != null) {
            return economy.withdrawPlayer(player, amount).transactionSuccess();
        }
        return false;
    }
}
