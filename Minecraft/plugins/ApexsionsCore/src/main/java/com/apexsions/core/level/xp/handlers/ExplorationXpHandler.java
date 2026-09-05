package com.apexsions.core.level.xp.handlers;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.level.xp.XpSourceHandler;
import com.apexsions.core.level.xp.antiabuse.MovementTracker;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handles XP awarded from exploring the world (walking, running, swimming, jumping).
 */
public class ExplorationXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;
    private final MovementTracker movementTracker;

    public ExplorationXpHandler(ApexsionsCorePlugin plugin, MovementTracker movementTracker) {
        this.plugin = plugin;
        this.movementTracker = movementTracker;
    }

    @Override
    public XpSource getSource() {
        return XpSource.EXPLORATION;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.exploration.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        // Must change block coordinates
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        // 1. Jumping Check
        if (to.getY() > from.getY() && !player.isFlying() && !player.isInsideVehicle()) {
            if (movementTracker.canGainJumpXp(player.getUniqueId())) {
                long jumpXp = plugin.getXpConfig().getLong("sources.exploration.jumping.xp", 1L);
                plugin.getLevelManager().addXp(player.getUniqueId(), jumpXp, XpSource.EXPLORATION);
            }
        }

        // 2. Horizontal Distance Check (Walking / Swimming)
        double threshold = player.isSwimming()
                ? plugin.getXpConfig().getDouble("sources.exploration.swimming.distance-threshold", 16.0)
                : plugin.getXpConfig().getDouble("sources.exploration.movement.distance-threshold", 16.0);

        long xpGain = player.isSwimming()
                ? plugin.getXpConfig().getLong("sources.exploration.swimming.xp", 1L)
                : plugin.getXpConfig().getLong("sources.exploration.movement.xp", 1L);

        if (movementTracker.trackMovement(player.getUniqueId(), from, to, player.isSwimming(), threshold)) {
            plugin.getLevelManager().addXp(player.getUniqueId(), xpGain, XpSource.EXPLORATION);
        }
    }
}
