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
 * Handles XP awarded from exploring the world (walking/running, swimming, elytra gliding, vehicle riding, jumping).
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
        if (to.getY() > from.getY() && !player.isFlying() && !player.isGliding() && !player.isInsideVehicle()) {
            if (movementTracker.canGainJumpXp(player.getUniqueId())) {
                long jumpXp = plugin.getXpConfig().getLong("sources.exploration.jumping.xp", 1L);
                plugin.getLevelManager().addXp(player.getUniqueId(), jumpXp, XpSource.EXPLORATION);
            }
        }

        // 2. Movement Mode Distance Check (Gliding, Riding, Swimming, Walking)
        MovementTracker.MovementMode mode;
        double threshold;
        long xpGain;

        if (player.isGliding()) {
            mode = MovementTracker.MovementMode.GLIDING;
            threshold = plugin.getXpConfig().getDouble("sources.exploration.gliding.distance-threshold", 32.0);
            xpGain = plugin.getXpConfig().getLong("sources.exploration.gliding.xp", 2L);
        } else if (player.isInsideVehicle()) {
            mode = MovementTracker.MovementMode.RIDING;
            threshold = plugin.getXpConfig().getDouble("sources.exploration.riding.distance-threshold", 16.0);
            xpGain = plugin.getXpConfig().getLong("sources.exploration.riding.xp", 1L);
        } else if (player.isSwimming()) {
            mode = MovementTracker.MovementMode.SWIMMING;
            threshold = plugin.getXpConfig().getDouble("sources.exploration.swimming.distance-threshold", 16.0);
            xpGain = plugin.getXpConfig().getLong("sources.exploration.swimming.xp", 2L);
        } else {
            mode = MovementTracker.MovementMode.WALKING;
            threshold = plugin.getXpConfig().getDouble("sources.exploration.movement.distance-threshold", 8.0);
            xpGain = plugin.getXpConfig().getLong("sources.exploration.movement.xp", 1L);
        }

        if (movementTracker.trackMovement(player.getUniqueId(), from, to, mode, threshold)) {
            plugin.getLevelManager().addXp(player.getUniqueId(), xpGain, XpSource.EXPLORATION);
        }
    }
}
