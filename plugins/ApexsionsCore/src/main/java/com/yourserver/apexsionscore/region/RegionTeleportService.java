package com.yourserver.apexsionscore.region;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Handles safe player teleportation to region spawns.
 */
public class RegionTeleportService {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public RegionTeleportService(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean teleport(Player player, Region region) {
        if (region == null) {
            player.sendMessage(miniMessage.deserialize("<red>Region not found or invalid.</red>"));
            return false;
        }

        Optional<Location> spawnLoc = region.getBukkitSpawnLocation();
        if (spawnLoc.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<red>Spawn location for " + region.getDisplayName() + " is not configured or world is unloaded.</red>"));
            return false;
        }

        player.sendMessage(miniMessage.deserialize("<gold>Teleporting to <yellow>" + region.getDisplayName() + "<gold>...</gold>"));
        player.teleportAsync(spawnLoc.get()).thenAccept(success -> {
            if (success) {
                player.sendMessage(miniMessage.deserialize("<green>Welcome to <yellow>" + region.getDisplayName() + "<green>!</green>"));
            } else {
                player.sendMessage(miniMessage.deserialize("<red>Teleportation failed. Please try again.</red>"));
            }
        });

        return true;
    }

    public boolean teleportToRegion(Player player) {
        Optional<com.yourserver.apexsionscore.player.PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        if (dataOpt.isEmpty() || !dataOpt.get().hasRegion()) {
            return false;
        }
        Optional<Region> regionOpt = plugin.getRegionManager().getRegion(dataOpt.get().getRegionId());
        return regionOpt.filter(region -> teleport(player, region)).isPresent();
    }
}
