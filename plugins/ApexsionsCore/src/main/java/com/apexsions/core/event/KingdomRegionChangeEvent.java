package com.apexsions.core.event;

import com.apexsions.core.region.Region;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when an existing player's kingdom region is changed.
 */
public class KingdomRegionChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Region oldRegion;
    private final Region newRegion;
    private boolean cancelled;

    public KingdomRegionChangeEvent(@NotNull Player player, Region oldRegion, @NotNull Region newRegion) {
        this.player = player;
        this.oldRegion = oldRegion;
        this.newRegion = newRegion;
    }

    public Player getPlayer() {
        return player;
    }

    public Region getOldRegion() {
        return oldRegion;
    }

    public Region getNewRegion() {
        return newRegion;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
