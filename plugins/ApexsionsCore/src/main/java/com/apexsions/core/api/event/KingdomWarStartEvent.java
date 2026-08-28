package com.apexsions.core.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class KingdomWarStartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String kingdom1;
    private final String kingdom2;
    private final int durationMinutes;

    public KingdomWarStartEvent(String kingdom1, String kingdom2, int durationMinutes) {
        this.kingdom1 = kingdom1;
        this.kingdom2 = kingdom2;
        this.durationMinutes = durationMinutes;
    }

    public String getKingdom1() { return kingdom1; }
    public String getKingdom2() { return kingdom2; }
    public int getDurationMinutes() { return durationMinutes; }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
