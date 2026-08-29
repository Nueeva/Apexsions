package com.apexsions.chat.integration;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.core.api.ApexsionsCoreAPI;
import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.region.Region;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class ApexsionsCoreHook {

    private final ApexsionsChatPlugin plugin;
    private boolean available = false;

    public ApexsionsCoreHook(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
        try {
            ApexsionsCoreAPI api = ApexsionsCoreProvider.get();
            if (api != null) {
                this.available = true;
                plugin.getLogger().info("Successfully hooked into ApexsionsCore API.");
            }
        } catch (Throwable t) {
            this.available = false;
            plugin.getLogger().info("ApexsionsCore not available. Falling back to default progression data.");
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public int getPlayerLevel(UUID uuid) {
        if (!isAvailable()) return 1;
        try {
            return ApexsionsCoreProvider.get().getLevel(uuid);
        } catch (Throwable t) {
            return 1;
        }
    }

    public String getPlayerTitle(UUID uuid) {
        if (!isAvailable()) return "Citizen";
        try {
            return ApexsionsCoreProvider.get().getLevelTitle(uuid);
        } catch (Throwable t) {
            return "Citizen";
        }
    }

    public String getPlayerRegionKey(UUID uuid) {
        if (!isAvailable()) return "NONE";
        try {
            Region region = ApexsionsCoreProvider.get().getRegion(uuid);
            return region != null ? region.getKey() : "NONE";
        } catch (Throwable t) {
            return "NONE";
        }
    }

    public String getPlayerRegionName(UUID uuid) {
        if (!isAvailable()) return "Wilderness";
        try {
            Region region = ApexsionsCoreProvider.get().getRegion(uuid);
            return region != null ? region.getDisplayName() : "Wilderness";
        } catch (Throwable t) {
            return "Wilderness";
        }
    }

    public void addXp(UUID uuid, long amount) {
        if (!isAvailable()) return;
        try {
            ApexsionsCoreProvider.get().addXp(uuid, amount, XpSource.CHAT_GAME_WIN);
        } catch (Throwable ignored) {}
    }
}
