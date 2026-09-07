package com.apexsions.core.sions;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the Lore-Friendly Temporal Reconstruction Engine for Kerajaan Sions.
 * Tracks all block modifications (breaks, places, explosions, fire) within the Sions polygon
 * and automatically restores them back to their original state on an hourly schedule.
 */
public class SionsTemporalService {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Map of modified block locations -> snapshot of their original pristine BlockData
    private final Map<Location, BlockData> originalBlocks = new ConcurrentHashMap<>();

    // Set of admin UUIDs bypassing temporal tracking (permanent builders)
    private final Set<UUID> bypassPlayers = ConcurrentHashMap.newKeySet();

    private BukkitTask hourlyTask;
    private long nextResetTimeMs = 0L;
    private int intervalMinutes = 60;

    public SionsTemporalService(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!isEnabled()) {
            plugin.getLogger().info("Sions Temporal Reconstruction is disabled in config.");
            return;
        }

        this.intervalMinutes = Math.max(1, plugin.getConfig().getInt("sions-temporal.interval-minutes", 60));
        long intervalTicks = intervalMinutes * 60L * 20L;
        this.nextResetTimeMs = System.currentTimeMillis() + (intervalMinutes * 60L * 1000L);

        if (hourlyTask != null) {
            hourlyTask.cancel();
        }

        this.hourlyTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            restoreAll(true);
        }, intervalTicks, intervalTicks);

        plugin.getLogger().info("Sions Temporal Reconstruction Engine active. Scheduled hourly reset every " + intervalMinutes + " minutes.");
    }

    public void stop() {
        if (hourlyTask != null) {
            hourlyTask.cancel();
            hourlyTask = null;
        }
    }

    public void onDisable() {
        stop();
        if (plugin.getConfig().getBoolean("sions-temporal.restore-on-shutdown", true) && !originalBlocks.isEmpty()) {
            int count = restoreAll(false);
            plugin.getLogger().info("Sions Temporal Engine: Cleaned up and restored " + count + " blocks on shutdown.");
        }
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("sions-temporal.enabled", true);
    }

    public boolean isPreventItemDrops() {
        return plugin.getConfig().getBoolean("sions-temporal.prevent-item-drops", true);
    }

    public boolean isInSions(Location location) {
        if (location == null || location.getWorld() == null) return false;
        Optional<Region> sionsOpt = plugin.getRegionManager().getRegion("SIONS");
        return sionsOpt.map(region -> region.containsLocation(location)).orElse(false);
    }

    public boolean isBypassing(Player player) {
        if (player == null) return false;
        return bypassPlayers.contains(player.getUniqueId());
    }

    public boolean toggleBypass(Player player) {
        if (player == null) return false;
        UUID uuid = player.getUniqueId();
        if (bypassPlayers.contains(uuid)) {
            bypassPlayers.remove(uuid);
            return false;
        } else {
            bypassPlayers.add(uuid);
            return true;
        }
    }

    /**
     * Records a block's original state before modification if inside Kerajaan Sions.
     * Guaranteed invariant: Only records the very first original pristine state of each location.
     */
    public void recordBlockState(Location loc, BlockData originalState) {
        if (!isEnabled() || loc == null || originalState == null) return;
        if (!isInSions(loc)) return;

        // Clone location with block integers to prevent floating-point key mismatch
        Location blockLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        originalBlocks.putIfAbsent(blockLoc, originalState.clone());
    }

    /**
     * Records that an empty AIR spot was filled by placing a block.
     */
    public void recordAirPlacement(Location loc) {
        if (!isEnabled() || loc == null) return;
        if (!isInSions(loc)) return;

        Location blockLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        originalBlocks.putIfAbsent(blockLoc, Bukkit.createBlockData(Material.AIR));
    }

    /**
     * Restores all modified blocks in Kerajaan Sions back to their original states.
     */
    public int restoreAll(boolean announce) {
        if (originalBlocks.isEmpty()) {
            this.nextResetTimeMs = System.currentTimeMillis() + (intervalMinutes * 60L * 1000L);
            return 0;
        }

        int count = originalBlocks.size();

        // 1. Broadcast Lore Reconstruction Announcement
        if (announce) {
            String message = "<dark_purple>✦ [SIONS] </dark_purple><light_purple><bold>REKONSTRUKSI TEMPORAL KERAJAAN SIONS!</bold></light_purple><newline>" +
                    "<gray>Waktu temporal telah berputar kembali. <gold>" + count + "</gold> struktur kuno telah menyusun kembali dirinya ke wujud semula!</gray>";

            boolean serverWide = plugin.getConfig().getBoolean("sions-temporal.broadcast-server", false);
            if (serverWide) {
                Bukkit.broadcast(miniMessage.deserialize(message));
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isInSions(player.getLocation())) {
                        player.sendMessage(miniMessage.deserialize(message));
                        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0f, 0.8f);
                        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0f, 1.2f);
                    }
                }
            }
        }

        // 2. Perform restoration
        for (Map.Entry<Location, BlockData> entry : originalBlocks.entrySet()) {
            Location loc = entry.getKey();
            BlockData originalData = entry.getValue();

            if (loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                Block block = loc.getBlock();
                block.setBlockData(originalData, false);
                loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0.5, 0.5, 0.5), 3, 0.2, 0.2, 0.2, 0.02);
            } else if (loc.getWorld() != null) {
                // If chunk is unloaded, setting block data without physics safely loads the block
                loc.getBlock().setBlockData(originalData, false);
            }
        }

        originalBlocks.clear();
        this.nextResetTimeMs = System.currentTimeMillis() + (intervalMinutes * 60L * 1000L);
        return count;
    }

    public int getModifiedBlockCount() {
        return originalBlocks.size();
    }

    public long getRemainingSeconds() {
        return Math.max(0L, (nextResetTimeMs - System.currentTimeMillis()) / 1000L);
    }

    public int getIntervalMinutes() {
        return intervalMinutes;
    }
}
