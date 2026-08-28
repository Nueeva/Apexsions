package com.apexsions.core.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class KingdomWarEndEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String kingdom1;
    private final String kingdom2;

    public KingdomWarEndEvent(String kingdom1, String kingdom2) {
        this.kingdom1 = kingdom1;
        this.kingdom2 = kingdom2;
    }

    public String getKingdom1() { return kingdom1; }
    public String getKingdom2() { return kingdom2; }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
