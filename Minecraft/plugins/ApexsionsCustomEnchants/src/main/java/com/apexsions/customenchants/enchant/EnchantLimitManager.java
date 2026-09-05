package com.apexsions.customenchants.enchant;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Map;

/**
 * Manages player custom enchantment limits per item based on rank, permissions, and admin bypass.
 */
public class EnchantLimitManager {

    private final ApexsionsCustomEnchantsPlugin plugin;

    public EnchantLimitManager(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Retrieves the maximum custom enchantment count allowed for a player on an item.
     * Returns Integer.MAX_VALUE if unlimited.
     */
    public int getPlayerEnchantLimit(Player player) {
        if (player == null) return 4;

        // Admin and unlimited permissions bypass all limits
        if (player.isOp() ||
                player.hasPermission("apexsions.admin") ||
                player.hasPermission("apexsions.ace.admin") ||
                player.hasPermission("apexsions.customenchants.limit.unlimited")) {
            return Integer.MAX_VALUE;
        }

        // Check explicit numerical permission: apexsions.customenchants.limit.<N>
        int maxPermLimit = -1;
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            if (pai.getValue()) {
                String perm = pai.getPermission().toLowerCase();
                if (perm.startsWith("apexsions.customenchants.limit.")) {
                    String numStr = perm.substring("apexsions.customenchants.limit.".length());
                    try {
                        int val = Integer.parseInt(numStr);
                        if (val > maxPermLimit) {
                            maxPermLimit = val;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        if (maxPermLimit >= 0) {
            return maxPermLimit;
        }

        // Check LuckPerms rank or group permission
        String[] rankOrder = {"ancestor", "architect", "overseer", "warden", "herald", "sions", "emperor", "sovereign", "archon", "ascendant", "wanderer"};
        for (String r : rankOrder) {
            if (player.hasPermission("group." + r) || player.hasPermission("apexsions.rank." + r)) {
                int rLimit = plugin.getConfig().getInt("enchant-limits.ranks." + r, getDefaultRankLimit(r));
                if (rLimit == -1) return Integer.MAX_VALUE;
                return rLimit;
            }
        }

        // Check via LuckPerms API if available
        try {
            if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
                Object lp = providerClass.getMethod("get").invoke(null);
                Object userManager = lp.getClass().getMethod("getUserManager").invoke(lp);
                Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class).invoke(userManager, player.getUniqueId());
                if (user != null) {
                    String primaryGroup = (String) user.getClass().getMethod("getPrimaryGroup").invoke(user);
                    if (primaryGroup != null) {
                        String gLower = primaryGroup.toLowerCase();
                        int rLimit = plugin.getConfig().getInt("enchant-limits.ranks." + gLower, getDefaultRankLimit(gLower));
                        if (rLimit == -1) return Integer.MAX_VALUE;
                        if (rLimit > 0) return rLimit;
                    }
                }
            }
        } catch (Throwable ignored) {}

        // Fallback to default
        return plugin.getConfig().getInt("enchant-limits.default", 4);
    }

    private int getDefaultRankLimit(String rank) {
        return switch (rank.toLowerCase()) {
            case "ancestor", "architect", "overseer", "warden", "herald" -> -1;
            case "sions" -> 9;
            case "emperor" -> 8;
            case "sovereign" -> 7;
            case "archon" -> 6;
            case "ascendant" -> 5;
            default -> 4;
        };
    }

    /**
     * Checks if applying the given custom enchant to the item is permitted under player's limit.
     * Upgrades to existing enchants do NOT consume an extra slot.
     */
    public boolean canApplyEnchant(Player player, ItemStack item, CustomEnchant enchant) {
        if (player == null || item == null || enchant == null) return false;
        int limit = getPlayerEnchantLimit(player);
        if (limit == Integer.MAX_VALUE) return true;

        Map<CustomEnchant, Integer> currentEnchants = plugin.getEnchantmentRegistry().getEnchantsOnItem(item);
        // If already present, it is an upgrade, not a new slot!
        if (currentEnchants.containsKey(enchant)) {
            return true;
        }

        return currentEnchants.size() < limit;
    }
}
