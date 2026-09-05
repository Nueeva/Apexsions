package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import de.bluecolored.bluemap.api.BlueMapAPI;
import org.bukkit.Bukkit;

/**
 * Soft-dependency hook for BlueMap API and configuration synchronization.
 */
public class BlueMapHook {

    private final ApexsionsCorePlugin plugin;
    private final BlueMapConfigParser configParser;
    private boolean hooked = false;

    public BlueMapHook(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.configParser = new BlueMapConfigParser(plugin);
    }

    public void initialize() {
        // Always try to parse world.conf if available
        configParser.parseAndApply();

        if (Bukkit.getPluginManager().isPluginEnabled("BlueMap")) {
            try {
                BlueMapAPI.onEnable(api -> {
                    this.hooked = true;
                    plugin.getLogger().info("Successfully hooked into BlueMap API.");
                    // Re-parse to ensure map consistency when BlueMap is ready
                    configParser.parseAndApply();
                });
            } catch (Throwable t) {
                plugin.getLogger().warning("Could not register BlueMap API listener: " + t.getMessage());
            }
        }
    }

    public boolean isHooked() {
        return hooked;
    }

    public BlueMapConfigParser getConfigParser() {
        return configParser;
    }
}
