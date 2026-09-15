package com.apexsions.core.security;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Watchdog monitoring high-frequency redstone oscillations to prevent lag machines and TPS drops.
 */
public class RedstoneWatchdogListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<String, PulseTracker> pulseTrackers = new ConcurrentHashMap<>();
    private final Map<String, Long> lastWarning = new ConcurrentHashMap<>();

    private static final int MAX_PULSES_PER_WINDOW = 25;
    private static final long WINDOW_MS = 2000L;

    public RedstoneWatchdogListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (event.getOldCurrent() == event.getNewCurrent()) return;

        Block block = event.getBlock();
        String locKey = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
        long now = System.currentTimeMillis();

        PulseTracker tracker = pulseTrackers.computeIfAbsent(locKey, k -> new PulseTracker(now));
        int pulses = tracker.recordPulse(now);

        if (pulses > MAX_PULSES_PER_WINDOW) {
            // Freeze signal
            event.setNewCurrent(0);

            Location loc = block.getLocation().add(0.5, 0.5, 0.5);
            loc.getWorld().spawnParticle(Particle.SMOKE, loc, 10, 0.2, 0.2, 0.2, 0.05);

            Long lastWarn = lastWarning.get(locKey);
            if (lastWarn == null || now - lastWarn > 5000L) {
                lastWarning.put(locKey, now);
                loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.0f);

                for (Player nearby : loc.getWorld().getPlayers()) {
                    if (nearby.getLocation().distanceSquared(loc) <= 225) { // 15 blocks
                        nearby.sendActionBar(mm.deserialize("<yellow>⚠ Sirkuit redstone cepat dibekukan sementara demi menjaga kestabilan 20 TPS server.</yellow>"));
                    }
                }
            }
        }

        // Clean trackers if map gets too big
        if (pulseTrackers.size() > 5000) {
            pulseTrackers.entrySet().removeIf(e -> now - e.getValue().lastPulse > 10_000L);
        }
    }

    private static class PulseTracker {
        long windowStart;
        long lastPulse;
        int count;

        PulseTracker(long now) {
            this.windowStart = now;
            this.lastPulse = now;
            this.count = 1;
        }

        synchronized int recordPulse(long now) {
            lastPulse = now;
            if (now - windowStart > WINDOW_MS) {
                windowStart = now;
                count = 1;
            } else {
                count++;
            }
            return count;
        }
    }
}
