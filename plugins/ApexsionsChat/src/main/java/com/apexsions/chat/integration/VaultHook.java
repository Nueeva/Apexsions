package com.apexsions.chat.integration;

import com.apexsions.chat.ApexsionsChatPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private final ApexsionsChatPlugin plugin;
    private Economy economy;
    private boolean available = false;

    public VaultHook(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
                this.available = this.economy != null;
                if (this.available) {
                    plugin.getLogger().info("Successfully hooked into Vault Economy.");
                }
            }
        }
    }

    public boolean isAvailable() {
        return available && economy != null;
    }

    public double getBalance(Player player) {
        if (isAvailable() && player != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }

    public void deposit(Player player, double amount) {
        if (isAvailable() && player != null) {
            economy.depositPlayer(player, amount);
        }
    }
}
