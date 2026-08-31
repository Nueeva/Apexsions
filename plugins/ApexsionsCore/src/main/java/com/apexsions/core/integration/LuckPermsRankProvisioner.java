package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.DisplayNameNode;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.WeightNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Idempotent LuckPerms Rank Provisioner.
 * Automatically ensures all required server ranks exist with appropriate display metadata
 * without duplicating groups, wiping custom permissions, or assigning protected ranks arbitrarily.
 */
public class LuckPermsRankProvisioner {

    private final ApexsionsCorePlugin plugin;
    private final LuckPerms luckPerms;

    public LuckPermsRankProvisioner(ApexsionsCorePlugin plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    /**
     * Asynchronously and idempotently provisions the 9 required rank groups.
     */
    public CompletableFuture<Void> provisionRanksAsync() {
        return CompletableFuture.runAsync(() -> {
            if (luckPerms == null) return;

            FileConfiguration config = plugin.getConfigManager().getRanksConfig();
            if (config == null) return;

            ConfigurationSection ranksSec = config.getConfigurationSection("ranks");
            if (ranksSec == null) return;

            GroupManager groupManager = luckPerms.getGroupManager();

            plugin.getLogger().info("Synchronizing and provisioning LuckPerms rank hierarchy...");

            for (String rankKey : ranksSec.getKeys(false)) {
                String normalizedName = rankKey.toLowerCase().trim();
                String displayName = ranksSec.getString(rankKey + ".display-name", rankKey);
                String prefix = ranksSec.getString(rankKey + ".prefix", "[" + displayName + "] ");
                int weight = ranksSec.getInt(rankKey + ".weight", 10);

                try {
                    // 1. Get or create group idempotently
                    Group group = groupManager.getGroup(normalizedName);
                    if (group == null) {
                        group = groupManager.createAndLoadGroup(normalizedName).join();
                        plugin.getLogger().info("Created missing LuckPerms group: " + normalizedName);
                    }

                    // 2. Update display metadata idempotently without touching unrelated permissions
                    final Group finalGroup = group;
                    finalGroup.data().clear(node -> node instanceof PrefixNode || node instanceof WeightNode || node instanceof DisplayNameNode);

                    // Add managed metadata nodes
                    finalGroup.data().add(PrefixNode.builder(prefix, weight).build());
                    finalGroup.data().add(WeightNode.builder(weight).build());
                    finalGroup.data().add(DisplayNameNode.builder(displayName).build());

                    // Save group changes
                    groupManager.saveGroup(finalGroup).join();
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to provision LuckPerms rank '" + normalizedName + "': " + e.getMessage());
                }
            }

            plugin.getLogger().info("LuckPerms rank provisioning completed successfully.");
        });
    }

    /**
     * Checks and sets default rank on first join or verifies owner rank.
     */
    public void handlePlayerJoin(Player player) {
        if (luckPerms == null || player == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                UserManager userManager = luckPerms.getUserManager();
                User user = userManager.loadUser(player.getUniqueId()).join();
                if (user == null) return;

                FileConfiguration config = plugin.getConfigManager().getRanksConfig();
                String ownerUuidStr = config != null ? config.getString("owner.uuid", "") : "";

                // 1. Check Owner UUID for The Ancestor rank
                if (ownerUuidStr != null && !ownerUuidStr.isEmpty() && !ownerUuidStr.contains("00000000")) {
                    try {
                        UUID ownerUuid = UUID.fromString(ownerUuidStr);
                        if (player.getUniqueId().equals(ownerUuid)) {
                            boolean hasAncestor = user.getNodes().stream()
                                    .filter(n -> n instanceof InheritanceNode)
                                    .map(n -> ((InheritanceNode) n).getGroupName())
                                    .anyMatch(g -> g.equalsIgnoreCase("ancestor"));

                            if (!hasAncestor) {
                                user.data().add(InheritanceNode.builder("ancestor").build());
                                userManager.saveUser(user).join();
                                plugin.getLogger().info("Assigned protected rank 'The Ancestor' to owner: " + player.getName());
                            }
                            return;
                        }
                    } catch (IllegalArgumentException ignored) {}
                }

                // 2. Clear any stray direct personal PrefixNodes on user to ensure clean rank inheritance
                user.data().clear(node -> node instanceof PrefixNode);

                // 3. Assign Wanderer as default rank if player only has standard default group
                boolean hasCustomRank = user.getNodes().stream()
                        .filter(n -> n instanceof InheritanceNode)
                        .map(n -> ((InheritanceNode) n).getGroupName())
                        .anyMatch(g -> !g.equalsIgnoreCase("default"));

                if (!hasCustomRank) {
                    user.data().add(InheritanceNode.builder("wanderer").build());
                }
                userManager.saveUser(user).join();
            } catch (Exception e) {
                plugin.getLogger().warning("Error processing rank join for " + player.getName() + ": " + e.getMessage());
            }
        });
    }
}
