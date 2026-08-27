package com.yourserver.apexsionschat.integration;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LuckPermsHook {

    private final ApexsionsChatPlugin plugin;
    private LuckPerms luckPerms;
    private boolean available = false;

    public LuckPermsHook(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                this.luckPerms = LuckPermsProvider.get();
                this.available = true;
                plugin.getLogger().info("Successfully hooked into LuckPerms.");
            } catch (Exception e) {
                this.available = false;
            }
        }
    }

    public boolean isAvailable() {
        return available && luckPerms != null;
    }

    public String getPlayerRank(Player player) {
        if (!isAvailable() || player == null) {
            return "Wanderer";
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
                    return formatGroup(primaryGroup);
                }
            }
        } catch (Exception ignored) {}
        return "Wanderer";
    }

    private String formatGroup(String group) {
        if (group == null || group.isEmpty()) return "Wanderer";
        return Character.toUpperCase(group.charAt(0)) + group.substring(1).toLowerCase();
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
                .replace("&l", "<bold>").replace("§l", "<bold>");
    }
}
