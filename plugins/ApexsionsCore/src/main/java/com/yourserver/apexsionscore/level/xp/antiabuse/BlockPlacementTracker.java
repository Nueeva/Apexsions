package com.yourserver.apexsionscore.level.xp.antiabuse;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yourserver.apexsionscore.config.ConfigManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.concurrent.TimeUnit;

/**
 * Tracks player-placed blocks to prevent infinite place -> break XP exploit loops.
 */
public class BlockPlacementTracker implements Listener {

    private final Cache<Long, Boolean> placedBlocks;

    public BlockPlacementTracker(ConfigManager configManager) {
        int cacheSize = configManager.getBlockTrackerCacheSize();
        int expireHours = configManager.getBlockTrackerExpireHours();

        this.placedBlocks = Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(expireHours, TimeUnit.HOURS)
                .build();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        long key = getBlockKey(block.getLocation());
        placedBlocks.put(key, Boolean.TRUE);
    }

    /**
     * Checks if a block was placed by a player and removes it from tracking if true.
     */
    public boolean isPlayerPlaced(Block block) {
        long key = getBlockKey(block.getLocation());
        Boolean present = placedBlocks.getIfPresent(key);
        if (present != null && present) {
            placedBlocks.invalidate(key);
            return true;
        }
        return false;
    }

    private long getBlockKey(Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        int worldHash = loc.getWorld() != null ? loc.getWorld().getUID().hashCode() : 0;

        long key = ((long) x & 0x3FFFFFF) << 38;
        key |= ((long) y & 0xFFF) << 26;
        key |= ((long) z & 0x3FFFFFF);
        return key ^ worldHash;
    }
}
