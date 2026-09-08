package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import org.bukkit.Bukkit;

/**
 * Soft-dependency hook for Citizens2 NPC plugin.
 * Hardcoded NPC click listeners and custom traits have been removed so administrators
 * can freely bind commands natively using Citizens (e.g. /npc cmd add -p k).
 */
public class CitizensHook {

    private final ApexsionsCorePlugin plugin;
    private boolean hooked = false;

    public CitizensHook(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            return;
        }

        this.hooked = true;
        plugin.getLogger().info("Citizens2 hook active (NPC interactions handled natively via Citizens commands).");
    }

    public boolean isHooked() {
        return hooked;
    }
}

