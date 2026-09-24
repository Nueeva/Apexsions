package com.apexsions.core.sleep;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages the single-player sleep subsystem.
 * Enforces PLAYERS_SLEEPING_PERCENTAGE = 0 on Overworld worlds so a single player
 * can sleep through the night, while providing thematic broadcasts and weather resets.
 */
public class SleepManager {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Per-world cooldowns to prevent chat spam when multiple players sleep or wake
    private final Map<UUID, Long> lastSleepBroadcast = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastMorningBroadcast = new ConcurrentHashMap<>();

    private static final long SLEEP_COOLDOWN_MS = 10_000L;
    private static final long MORNING_COOLDOWN_MS = 10_000L;

    public SleepManager(@NotNull ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the sleep subsystem and enforces the single-player sleep rule across loaded worlds.
     */
    public void initialize() {
        if (!plugin.getConfigManager().isSleepEnabled()) {
            plugin.getLogger().info("Single-player sleep system is disabled in config.yml.");
            return;
        }

        if (plugin.getConfigManager().isSinglePlayerSleep()) {
            for (World world : Bukkit.getWorlds()) {
                applyGamerule(world);
            }
            plugin.getLogger().info("Single-player sleep system initialized (PLAYERS_SLEEPING_PERCENTAGE = 0).");
        }
    }

    /**
     * Applies the single-player sleep gamerule to the given world if eligible.
     */
    public void applyGamerule(@NotNull World world) {
        if (!plugin.getConfigManager().isSleepEnabled() || !plugin.getConfigManager().isSinglePlayerSleep()) {
            return;
        }

        if (world.getEnvironment() == World.Environment.NORMAL) {
            try {
                world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 0);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to set playersSleepingPercentage on world " + world.getName(), e);
            }
        }
    }

    /**
     * Handles when a player successfully enters a bed.
     */
    public void onPlayerSleep(@NotNull Player player) {
        if (!plugin.getConfigManager().isSleepEnabled()) {
            return;
        }

        World world = player.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        // Re-enforce gamerule just in case
        applyGamerule(world);

        if (!plugin.getConfigManager().isSleepBroadcast()) {
            return;
        }

        long now = System.currentTimeMillis();
        Long lastTime = lastSleepBroadcast.get(world.getUID());
        if (lastTime != null && (now - lastTime) < SLEEP_COOLDOWN_MS) {
            return; // Debounced
        }
        lastSleepBroadcast.put(world.getUID(), now);

        String rawMsg = plugin.getConfigManager().getSleepBroadcastMessage();
        String formatted = rawMsg.replace("%player%", player.getName());

        world.sendMessage(miniMessage.deserialize(formatted));
    }

    /**
     * Handles when time is skipped due to bed sleep.
     */
    public void onNightSkip(@NotNull World world) {
        if (!plugin.getConfigManager().isSleepEnabled()) {
            return;
        }

        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        // Reset storm/thunder if configured
        if (plugin.getConfigManager().isSleepClearWeather()) {
            if (world.hasStorm() || world.isThundering()) {
                world.setStorm(false);
                world.setThundering(false);
            }
        }

        if (!plugin.getConfigManager().isSleepMorningBroadcast()) {
            return;
        }

        long now = System.currentTimeMillis();
        Long lastTime = lastMorningBroadcast.get(world.getUID());
        if (lastTime != null && (now - lastTime) < MORNING_COOLDOWN_MS) {
            return; // Debounced
        }
        lastMorningBroadcast.put(world.getUID(), now);

        String rawMsg = plugin.getConfigManager().getSleepMorningMessage();
        world.sendMessage(miniMessage.deserialize(rawMsg));
    }
}
