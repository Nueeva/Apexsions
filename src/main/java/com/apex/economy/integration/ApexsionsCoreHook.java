package com.apex.economy.integration;

import com.apex.economy.ApexsionsEconomy;
import com.yourserver.apexsionscore.api.ApexsionsCoreAPI;
import com.yourserver.apexsionscore.api.ApexsionsCoreProvider;
import org.bukkit.Bukkit;

import java.util.UUID;

public class ApexsionsCoreHook {

    private final ApexsionsEconomy plugin;

    public ApexsionsCoreHook(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("ApexsionsCore") && ApexsionsCoreProvider.get() != null;
    }

    public String getPlayerKingdom(UUID uuid) {
        if (!isAvailable() || uuid == null) {
            return "NONE";
        }
        try {
            ApexsionsCoreAPI api = ApexsionsCoreProvider.get();
            if (api != null) {
                return api.getPlayerRegionKey(uuid);
            }
        } catch (Exception ignored) {}
        return "NONE";
    }

    public boolean isSameKingdom(UUID uuid1, UUID uuid2) {
        if (uuid1 == null || uuid2 == null) return false;
        String k1 = getPlayerKingdom(uuid1);
        String k2 = getPlayerKingdom(uuid2);
        if (k1.equalsIgnoreCase("NONE") || k2.equalsIgnoreCase("NONE")) {
            return false;
        }
        return k1.equalsIgnoreCase(k2);
    }
}
