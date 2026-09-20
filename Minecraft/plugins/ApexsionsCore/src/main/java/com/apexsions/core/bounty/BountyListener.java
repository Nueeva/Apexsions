package com.apexsions.core.bounty;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Routes PvP kills to the bounty payout engine and tracks per-session online time.
 */
public class BountyListener implements Listener {

    private final BountyManager bountyManager;

    public BountyListener(@NotNull BountyManager bountyManager) {
        this.bountyManager = bountyManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || killer.equals(victim)) {
            return;
        }
        bountyManager.handleKill(victim, killer);
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        bountyManager.onJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        bountyManager.onQuit(event.getPlayer());
    }
}