package com.yourserver.apexsionscore.integration;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Integration layer for reading LuckPerms rank, prefix, and metadata.
 */
public class LuckPermsHook {

    private final ApexsionsCorePlugin plugin;
    private LuckPerms luckPerms;
    private LuckPermsRankProvisioner rankProvisioner;
    private boolean available = false;

    public LuckPermsHook(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                this.luckPerms = LuckPermsProvider.get();
                this.rankProvisioner = new LuckPermsRankProvisioner(plugin, luckPerms);
                this.available = true;
                plugin.getLogger().info("LuckPerms hook initialized successfully.");
                
                // Automatically and idempotently provision managed rank hierarchy
                rankProvisioner.provisionRanksAsync();
            } catch (Exception e) {
                plugin.getLogger().warning("Could not hook into LuckPerms: " + e.getMessage());
                this.available = false;
            }
        } else {
            this.available = false;
            plugin.getLogger().info("LuckPerms not found. Defaulting rank resolution to config values.");
        }
    }

    public LuckPermsRankProvisioner getRankProvisioner() {
        return rankProvisioner;
    }

    public boolean isAvailable() {
        return available && luckPerms != null;
    }

    public String getPlayerRankKey(Player player) {
        if (!isAvailable() || player == null) {
            return "wanderer";
        }
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String primaryGroup = user.getPrimaryGroup();
                if (primaryGroup != null && !primaryGroup.isEmpty()) {
                    return primaryGroup.toLowerCase().trim();
                }
            }
        } catch (Exception ignored) {}
        return "wanderer";
    }

    public String getPlayerRank(Player player) {
        if (!isAvailable() || player == null) {
            return plugin.getConfigManager().getDefaultRank();
        }

        String groupKey = getPlayerRankKey(player);
        var ranksConfig = plugin.getConfigManager().getRanksConfig();
        if (ranksConfig != null && ranksConfig.contains("ranks." + groupKey)) {
            return ranksConfig.getString("ranks." + groupKey + ".formatted-name",
                    ranksConfig.getString("ranks." + groupKey + ".display-name", capitalize(groupKey)));
        }

        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                if (prefix != null && !prefix.trim().isEmpty()) {
                    return convertLegacyToMiniMessage(prefix.trim());
                }
                String primaryGroup = user.getPrimaryGroup();
                if (primaryGroup != null && !primaryGroup.isEmpty()) {
                    return capitalize(primaryGroup);
                }
            }
        } catch (Exception ignored) {}

        return plugin.getConfigManager().getDefaultRank();
    }

    public String getPlayerRankDisplayName(Player player) {
        String groupKey = getPlayerRankKey(player);
        var ranksConfig = plugin.getConfigManager().getRanksConfig();
        if (ranksConfig != null && ranksConfig.contains("ranks." + groupKey + ".display-name")) {
            return ranksConfig.getString("ranks." + groupKey + ".display-name", capitalize(groupKey));
        }
        return capitalize(groupKey);
    }

    public String getPlayerRankColor(Player player) {
        String groupKey = getPlayerRankKey(player);
        var ranksConfig = plugin.getConfigManager().getRanksConfig();
        if (ranksConfig != null && ranksConfig.contains("ranks." + groupKey + ".color")) {
            return ranksConfig.getString("ranks." + groupKey + ".color", "#808080");
        }
        return "#808080";
    }

    public String getPlayerPrefix(Player player) {
        if (!isAvailable() || player == null) {
            return "";
        }

        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                if (prefix != null && !prefix.trim().isEmpty()) {
                    return convertLegacyToMiniMessage(prefix.trim());
                }
            }
        } catch (Exception ignored) {}

        return "";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String convertLegacyToMiniMessage(String text) {
        if (text == null) return "";
        return text
                .replace("&0", "<black>").replace("§0", "<black>")
                .replace("&1", "<dark_blue>").replace("§1", "<dark_blue>")
                .replace("&2", "<dark_green>").replace("§2", "<dark_green>")
                .replace("&3", "<dark_aqua>").replace("§3", "<dark_aqua>")
                .replace("&4", "<dark_red>").replace("§4", "<dark_red>")
                .replace("&5", "<dark_purple>").replace("§5", "<dark_purple>")
                .replace("&6", "<gold>").replace("§6", "<gold>")
                .replace("&7", "<gray>").replace("§7", "<gray>")
                .replace("&8", "<dark_gray>").replace("§8", "<dark_gray>")
                .replace("&9", "<blue>").replace("§9", "<blue>")
                .replace("&a", "<green>").replace("§a", "<green>")
                .replace("&b", "<aqua>").replace("§b", "<aqua>")
                .replace("&c", "<red>").replace("§c", "<red>")
                .replace("&d", "<light_purple>").replace("§d", "<light_purple>")
                .replace("&e", "<yellow>").replace("§e", "<yellow>")
                .replace("&f", "<white>").replace("§f", "<white>")
                .replace("&l", "<bold>").replace("§l", "<bold>")
                .replace("&o", "<italic>").replace("§o", "<italic>")
                .replace("&n", "<underlined>").replace("§n", "<underlined>")
                .replace("&m", "<strikethrough>").replace("§m", "<strikethrough>")
                .replace("&r", "<reset>").replace("§r", "<reset>");
    }
}
