package com.yourserver.apexsionscore.integration;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import org.bukkit.Bukkit;

/**
 * Soft dependency integration checking EssentialsX presence and ensuring command coexistence.
 */
public class EssentialsHook {

    private final ApexsionsCorePlugin plugin;
    private boolean available = false;

    public EssentialsHook(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            this.available = true;
            plugin.getLogger().info("EssentialsX integration detected and confirmed active.");
        } else {
            this.available = false;
        }
    }

    public boolean isAvailable() {
        return available;
    }
}
