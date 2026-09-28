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

import java.lang.reflect.Method;

/**
 * Listens for bed enter, time skip, and world load events to support single-player sleep.
 */
public class SleepListener implements Listener {

    private static final Method GET_SKIP_REASON_METHOD;

    static {
        Method m = null;
        try {
            m = TimeSkipEvent.class.getMethod("getSkipReason");
            m.setAccessible(true);
        } catch (Exception ignored) {
        }
        GET_SKIP_REASON_METHOD = m;
    }

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
        if (isNightSkip(event)) {
            sleepManager.onNightSkip(event.getWorld());
        }
    }

    private boolean isNightSkip(@NotNull TimeSkipEvent event) {
        if (GET_SKIP_REASON_METHOD != null) {
            try {
                Object reason = GET_SKIP_REASON_METHOD.invoke(event);
                if (reason != null) {
                    return "NIGHT_SKIP".equals(reason.toString());
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        sleepManager.applyGamerule(event.getWorld());
    }
}
