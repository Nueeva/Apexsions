package com.apexsions.crates.api;

import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.milestone.Milestone;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a player reaches a crate milestone.
 */
public class CrateMilestoneEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Crate crate;
    private final Milestone milestone;
    private final int totalOpens;

    public CrateMilestoneEvent(@NotNull Player player, @NotNull Crate crate, @NotNull Milestone milestone, int totalOpens) {
        this.player = player;
        this.crate = crate;
        this.milestone = milestone;
        this.totalOpens = totalOpens;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Crate getCrate() {
        return crate;
    }

    public @NotNull Milestone getMilestone() {
        return milestone;
    }

    public int getTotalOpens() {
        return totalOpens;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
