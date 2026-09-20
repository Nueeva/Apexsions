package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Integration layer for Vault permissions, chat, and economy services.
 */
public class VaultHook {

    private final ApexsionsCorePlugin plugin;
    private Permission permissions;
    private Chat chat;
    private Economy economy;
    private boolean available = false;

    public VaultHook(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            setupPermissions();
            setupChat();
            setupEconomy();
            this.available = true;
            plugin.getLogger().info("Vault hook initialized successfully.");
        } else {
            this.available = false;
        }
    }

    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            this.permissions = rsp.getProvider();
        }
    }

    private void setupChat() {
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (rsp != null) {
            this.chat = rsp.getProvider();
        }
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            this.economy = rsp.getProvider();
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean hasEconomy() {
        if (economy == null && Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            setupEconomy();
        }
        return economy != null;
    }

    public double getBalance(OfflinePlayer player) {
        if (economy == null && Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            setupEconomy();
        }
        if (economy != null && player != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }

    public double getBalance(Player player) {
        return getBalance((OfflinePlayer) player);
    }

    public boolean has(OfflinePlayer player, double amount) {
        if (economy == null && Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            setupEconomy();
        }
        if (economy != null && player != null && amount > 0) {
            return economy.has(player, amount);
        }
        return false;
    }

    public void deposit(OfflinePlayer player, double amount) {
        if (economy == null && Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            setupEconomy();
        }
        if (economy != null && player != null && amount > 0) {
            economy.depositPlayer(player, amount);
        }
    }

    public void deposit(Player player, double amount) {
        deposit((OfflinePlayer) player, amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (economy == null && Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            setupEconomy();
        }
        if (economy != null && player != null && amount > 0) {
            var resp = economy.withdrawPlayer(player, amount);
            return resp != null && resp.transactionSuccess();
        }
        return false;
    }

    public void withdraw(Player player, double amount) {
        withdraw((OfflinePlayer) player, amount);
    }

    public Permission getPermissions() {
        return permissions;
    }

    public Chat getChat() {
        return chat;
    }

    public Economy getEconomy() {
        return economy;
    }
}
