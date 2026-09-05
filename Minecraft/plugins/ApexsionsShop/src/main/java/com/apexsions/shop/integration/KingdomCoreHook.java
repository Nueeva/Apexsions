package com.apexsions.shop.integration;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.core.api.ApexsionsCoreAPI;
import com.apexsions.core.api.ApexsionsCoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class KingdomCoreHook {

    private final ApexsionsShop plugin;
    private boolean coreAvailable = false;

    public KingdomCoreHook(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsCore")) {
            coreAvailable = true;
            plugin.getLogger().info("Successfully connected to ApexsionsCore Provider.");
        } else {
            plugin.getLogger().info("ApexsionsCore not found. Shop will run in standalone mode.");
        }
    }

    public String getPlayerKingdom(UUID uuid) {
        if (coreAvailable) {
            try {
                ApexsionsCoreAPI api = ApexsionsCoreProvider.get();
                if (api != null) {
                    return api.getPlayerRegionKey(uuid).toUpperCase();
                }
            } catch (Throwable ignored) {}
        }
        return "NONE";
    }

    public String getPlayerKingdom(Player player) {
        if (player == null) return "NONE";
        return getPlayerKingdom(player.getUniqueId());
    }

    public String getKingdomAtLocation(org.bukkit.Location location) {
        if (coreAvailable && location != null) {
            try {
                ApexsionsCoreAPI api = ApexsionsCoreProvider.get();
                if (api != null) {
                    return api.getKingdomAt(location)
                            .map(r -> r.getKey().toUpperCase())
                            .orElse("NONE");
                }
            } catch (Throwable ignored) {}
        }
        return "NONE";
    }

    public boolean isCoreAvailable() {
        return coreAvailable;
    }

    public void addXp(UUID uuid, long amount) {
        if (coreAvailable && amount > 0) {
            try {
                ApexsionsCoreAPI api = ApexsionsCoreProvider.get();
                if (api != null) {
                    api.addXp(uuid, amount, com.apexsions.core.level.xp.XpSource.SHOP_TRANSACTION);
                }
            } catch (Throwable ignored) {}
        }
    }
}
