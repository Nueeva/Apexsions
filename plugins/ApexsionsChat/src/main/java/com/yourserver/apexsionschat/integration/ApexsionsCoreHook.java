package com.yourserver.apexsionschat.integration;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import com.yourserver.apexsionscore.api.ApexsionsCoreAPI;
import com.yourserver.apexsionscore.api.ApexsionsCoreProvider;
import com.yourserver.apexsionscore.level.xp.XpSource;
import com.yourserver.apexsionscore.region.Region;
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
            ApexsionsCoreProvider.get().addXp(uuid, amount, XpSource.CUSTOM);
        } catch (Throwable ignored) {}
    }
}
