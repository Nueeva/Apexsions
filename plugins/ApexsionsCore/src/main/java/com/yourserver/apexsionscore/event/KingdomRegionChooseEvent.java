package com.yourserver.apexsionscore.event;

import com.yourserver.apexsionscore.region.Region;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player chooses a kingdom region for the first time.
 */
public class KingdomRegionChooseEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Region selectedRegion;
    private boolean cancelled;

    public KingdomRegionChooseEvent(@NotNull Player player, @NotNull Region selectedRegion) {
        this.player = player;
        this.selectedRegion = selectedRegion;
    }

    public Player getPlayer() {
        return player;
    }

    public Region getSelectedRegion() {
        return selectedRegion;
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
