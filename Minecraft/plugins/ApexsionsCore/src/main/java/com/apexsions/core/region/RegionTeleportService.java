package com.apexsions.core.region;

import com.apexsions.core.ApexsionsCorePlugin;
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

        player.sendMessage(miniMessage.deserialize("<gradient:#f39c12:#f1c40f><bold>APEXSIONS REALM</bold></gradient> <dark_gray>»</dark_gray> <gray>Menteleportasi Anda ke ibukota <yellow>" + region.getDisplayName() + "</yellow>...</gray>"));
        player.teleportAsync(spawnLoc.get()).thenAccept(success -> {
            if (success) {
                player.sendMessage(miniMessage.deserialize("<gradient:#f39c12:#f1c40f><bold>APEXSIONS REALM</bold></gradient> <dark_gray>»</dark_gray> <green>Selamat datang di wilayah kedaulatan <yellow>" + region.getDisplayName() + "</yellow>!</green>"));
                player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.8f, 1.0f);
            } else {
                player.sendMessage(miniMessage.deserialize("<red>Teleportasi gagal. Silakan coba sesaat lagi.</red>"));
            }
        });

        return true;
    }

    public boolean teleportToRegion(Player player) {
        Optional<com.apexsions.core.player.PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        if (dataOpt.isEmpty() || !dataOpt.get().hasRegion()) {
            return false;
        }
        Optional<Region> regionOpt = plugin.getRegionManager().getRegion(dataOpt.get().getRegionId());
        return regionOpt.filter(region -> teleport(player, region)).isPresent();
    }
}
