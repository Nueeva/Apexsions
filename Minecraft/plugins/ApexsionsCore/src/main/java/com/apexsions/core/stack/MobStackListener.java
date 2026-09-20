package com.apexsions.core.stack;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Bridges mob spawn/death events into the stacking engine.
 *
 * Stacked mobs never lose loot: on death one unit is removed from the counter
 * and the remainder is re-spawned, so vanilla drop and XP rates are preserved.
 */
public class MobStackListener implements Listener {

    private final MobStackManager stackManager;

    public MobStackListener(@NotNull MobStackManager stackManager) {
        this.stackManager = stackManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(@NotNull CreatureSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        stackManager.tryMergeOnSpawn(event.getEntity(), event.getSpawnReason());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        stackManager.handleDeath(entity);
    }
}