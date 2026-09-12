package com.apexsions.chat.integration;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.core.api.ApexsionsCoreAPI;
import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.region.Region;
import org.bukkit.Bukkit;

import java.util.UUID;

public class ApexsionsCoreHook {

    private final ApexsionsChatPlugin plugin;
    private boolean available = false;

    public ApexsionsCoreHook(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
        this.available = checkAvailability();
        if (this.available) {
            plugin.getLogger().info("Successfully hooked into ApexsionsCore API.");
        } else {
            plugin.getLogger().info("ApexsionsCore not available. Falling back to default progression data.");
        }
    }

    private boolean checkAvailability() {
        if (!Bukkit.getPluginManager().isPluginEnabled("ApexsionsCore")) {
            return false;
        }
        try {
            return CoreBridge.isAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isAvailable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("ApexsionsCore")) {
            return false;
        }
        try {
            return CoreBridge.isAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    public int getPlayerLevel(UUID uuid) {
        if (!isAvailable() || uuid == null) return 1;
        try {
            ApexsionsCoreAPI api = CoreBridge.getApi();
            return api != null ? api.getLevel(uuid) : 1;
        } catch (Throwable t) {
            return 1;
        }
    }

    public long getPlayerXp(UUID uuid) {
        if (!isAvailable() || uuid == null) return 0L;
        try {
            ApexsionsCoreAPI api = CoreBridge.getApi();
            return api != null ? api.getXp(uuid) : 0L;
        } catch (Throwable t) {
            return 0L;
        }
    }

    public String getPlayerTitle(UUID uuid) {
        if (!isAvailable() || uuid == null) return "Citizen";
        try {
            ApexsionsCoreAPI api = CoreBridge.getApi();
            return api != null ? api.getLevelTitle(uuid) : "Citizen";
        } catch (Throwable t) {
            return "Citizen";
        }
    }

    public String getPlayerRegionKey(UUID uuid) {
        if (!isAvailable() || uuid == null) return "NONE";
        try {
            ApexsionsCoreAPI api = CoreBridge.getApi();
            if (api != null) {
                Region region = api.getRegion(uuid);
                return region != null ? region.getKey() : "NONE";
            }
            return "NONE";
        } catch (Throwable t) {
            return "NONE";
        }
    }

    public String getPlayerRegionName(UUID uuid) {
        if (!isAvailable() || uuid == null) return "Wilderness";
        try {
            ApexsionsCoreAPI api = CoreBridge.getApi();
            if (api != null) {
                Region region = api.getRegion(uuid);
                return region != null ? region.getDisplayName() : "Wilderness";
            }
            return "Wilderness";
        } catch (Throwable t) {
            return "Wilderness";
        }
    }

    public void addXp(UUID uuid, long amount) {
        if (!isAvailable() || uuid == null) return;
        try {
            ApexsionsCoreAPI api = CoreBridge.getApi();
            if (api != null) {
                api.addXp(uuid, amount, XpSource.CHAT_GAME_WIN);
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to add XP to " + uuid + ": " + t.getMessage());
        }
    }

    public com.apexsions.core.api.PlayerChatProfile getPlayerChatProfile(UUID uuid) {
        if (!isAvailable() || uuid == null) return null;
        try {
            ApexsionsCoreAPI api = CoreBridge.getApi();
            return api != null ? api.getPlayerChatProfile(uuid) : null;
        } catch (Throwable t) {
            return null;
        }
    }

    public boolean isPlayerVanished(UUID uuid) {
        if (!isAvailable() || uuid == null) return false;
        try {
            ApexsionsCoreAPI api = CoreBridge.getApi();
            return api != null && api.isVanished(uuid);
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Isolated helper class to ensure classloader does not attempt to resolve
     * ApexsionsCoreProvider unless ApexsionsCore plugin is confirmed enabled in Bukkit.
     */
    private static class CoreBridge {
        static boolean isAvailable() {
            try {
                return ApexsionsCoreProvider.isAvailable();
            } catch (Throwable t) {
                return false;
            }
        }

        static ApexsionsCoreAPI getApi() {
            try {
                return ApexsionsCoreProvider.get();
            } catch (Throwable t) {
                return null;
            }
        }
    }
}

