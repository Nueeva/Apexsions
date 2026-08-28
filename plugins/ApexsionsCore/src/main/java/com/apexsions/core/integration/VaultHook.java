package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Integration layer for Vault permissions and chat services.
 */
public class VaultHook {

    private final ApexsionsCorePlugin plugin;
    private Permission permissions;
    private Chat chat;
    private boolean available = false;

    public VaultHook(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            setupPermissions();
            setupChat();
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

    public boolean isAvailable() {
        return available;
    }

    public Permission getPermissions() {
        return permissions;
    }

    public Chat getChat() {
        return chat;
    }
}
