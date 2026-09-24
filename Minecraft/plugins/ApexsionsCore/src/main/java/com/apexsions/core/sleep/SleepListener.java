package com.apexsions.core.sleep;

import com.apexsions.core.ApexsionsCorePlugin;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for bed enter, time skip, and world load events to support single-player sleep.
 */
public class SleepListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final SleepManager sleepManager;

    public SleepListener(@NotNull ApexsionsCorePlugin plugin, @NotNull SleepManager sleepManager) {
        this.plugin = plugin;
        this.sleepManager = sleepManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }

        Player player = event.getPlayer();

        // If player is vanished, do not broadcast sleep activity
        if (plugin.getVanishManager() != null && plugin.getVanishManager().isVanished(player)) {
            return;
        }

        sleepManager.onPlayerSleep(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTimeSkip(TimeSkipEvent event) {
        if (event.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            sleepManager.onNightSkip(event.getWorld());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        sleepManager.applyGamerule(event.getWorld());
    }
}
