package com.apexsions.chat.integration;

import com.apexsions.chat.ApexsionsChatPlugin;
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

    public String getPlayerRankKey(Player player) {
        if (!isAvailable() || player == null) {
            return player != null && player.isOp() ? "ancestor" : "wanderer";
        }
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String highestGroup = "wanderer";
                int highestWeight = -1;

                for (net.luckperms.api.node.Node node : user.getNodes()) {
                    if (node instanceof net.luckperms.api.node.types.InheritanceNode inh) {
                        String grp = inh.getGroupName().toLowerCase().trim();
                        int weight = getRankWeight(grp);
                        if (weight > highestWeight) {
                            highestWeight = weight;
                            highestGroup = grp;
                        }
                    }
                }

                String primaryGroup = user.getPrimaryGroup();
                if (primaryGroup != null && !primaryGroup.isEmpty()) {
                    String pGrp = primaryGroup.toLowerCase().trim();
                    int weight = getRankWeight(pGrp);
                    if (weight > highestWeight) {
                        highestWeight = weight;
                        highestGroup = pGrp;
                    }
                }

                if (highestGroup.equalsIgnoreCase("default")) {
                    highestGroup = "wanderer";
                }
                return highestGroup;
            }
        } catch (Exception ignored) {}
        return player.isOp() ? "ancestor" : "wanderer";
    }

    public int getRankWeight(String rankKey) {
        if (rankKey == null) return 0;
        return switch (rankKey.toLowerCase().trim()) {
            case "ancestor", "owner" -> 100;
            case "architect", "overseer" -> 95;
            case "warden", "admin", "headadmin" -> 90;
            case "herald", "mod", "moderator" -> 80;
            case "sions" -> 70;
            case "emperor" -> 60;
            case "sovereign" -> 50;
            case "archon" -> 40;
            case "ascendant" -> 30;
            case "wanderer" -> 10;
            default -> 5;
        };
    }

    public String getPlayerRankMentionFormat(Player player) {
        String rankKey = getPlayerRankKey(player);
        return switch (rankKey.toLowerCase().trim()) {
            case "ancestor", "owner" -> "<gradient:#8B0000:#FF0000><bold>@{player}</bold></gradient>";
            case "architect" -> "<gradient:#8E2DE2:#4A00E0><bold>@{player}</bold></gradient>";
            case "overseer" -> "<gradient:#FFD700:#FFA500><bold>@{player}</bold></gradient>";
            case "warden", "admin", "headadmin" -> "<gradient:#1e3c72:#2a5298><bold>@{player}</bold></gradient>";
            case "herald", "mod", "moderator" -> "<gradient:#f857a6:#ff5858><bold>@{player}</bold></gradient>";
            case "sions" -> "<gradient:#00FFFF:#FFD700><bold>@{player}</bold></gradient>";
            case "emperor" -> "<gradient:#e52d27:#b31217><bold>@{player}</bold></gradient>";
            case "sovereign" -> "<gradient:#f39c12:#f1c40f><bold>@{player}</bold></gradient>";
            case "archon" -> "<gradient:#00c6ff:#0072ff><bold>@{player}</bold></gradient>";
            case "ascendant" -> "<gradient:#11998e:#38ef7d><bold>@{player}</bold></gradient>";
            default -> "<color:#38bdf8><bold>@{player}</bold></color>";
        };
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
                String primaryGroup = getPlayerRankKey(player);
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
