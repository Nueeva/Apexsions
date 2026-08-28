package com.apexsions.shop.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MarketPriceChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String itemId;
    private final double oldPrice;
    private final double newPrice;
    private final double supplyMultiplier;

    public MarketPriceChangeEvent(String itemId, double oldPrice, double newPrice, double supplyMultiplier) {
        this.itemId = itemId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.supplyMultiplier = supplyMultiplier;
    }

    public String getItemId() { return itemId; }
    public double getOldPrice() { return oldPrice; }
    public double getNewPrice() { return newPrice; }
    public double getSupplyMultiplier() { return supplyMultiplier; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
