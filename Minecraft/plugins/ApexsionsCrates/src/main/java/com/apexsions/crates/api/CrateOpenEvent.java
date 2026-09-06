package com.apexsions.crates.api;

import com.apexsions.crates.crate.Crate;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a player begins opening a crate.
 */
public class CrateOpenEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Crate crate;
    private final boolean virtualKey;
    private boolean cancelled;

    public CrateOpenEvent(@NotNull Player player, @NotNull Crate crate, boolean virtualKey) {
        this.player = player;
        this.crate = crate;
        this.virtualKey = virtualKey;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Crate getCrate() {
        return crate;
    }

    public boolean isVirtualKey() {
        return virtualKey;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
