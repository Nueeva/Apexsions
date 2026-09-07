package com.apexsions.core.sions;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class SionsTemporalListener implements Listener {

    private final SionsTemporalService service;

    public SionsTemporalListener(SionsTemporalService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (service.isBypassing(player)) {
            return; // Staff in bypass mode can build permanently
        }

        Block block = event.getBlock();
        if (service.isInSions(block.getLocation())) {
            // Record original block state before it becomes AIR
            service.recordBlockState(block.getLocation(), block.getBlockData());

            // Prevent infinite item farming exploitation in temporal realm
            if (service.isPreventItemDrops()) {
                event.setDropItems(false);
                event.setExpToDrop(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (service.isBypassing(player)) {
            return; // Staff in bypass mode can build permanently
        }

        Block placedBlock = event.getBlockPlaced();
        if (service.isInSions(placedBlock.getLocation())) {
            // Record state of what was replaced (usually AIR, water, or grass)
            service.recordBlockState(event.getBlockReplacedState().getLocation(), event.getBlockReplacedState().getBlockData());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (service.isInSions(block.getLocation())) {
                service.recordBlockState(block.getLocation(), block.getBlockData());
            }
        }
        if (service.isPreventItemDrops() && service.isInSions(event.getLocation())) {
            event.setYield(0.0f);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (service.isInSions(block.getLocation())) {
                service.recordBlockState(block.getLocation(), block.getBlockData());
            }
        }
        if (service.isPreventItemDrops() && service.isInSions(event.getBlock().getLocation())) {
            event.setYield(0.0f);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        if (service.isInSions(block.getLocation())) {
            service.recordBlockState(block.getLocation(), block.getBlockData());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        Block block = event.getBlock();
        if (service.isInSions(block.getLocation())) {
            service.recordBlockState(block.getLocation(), block.getBlockData());
        }
    }
}
