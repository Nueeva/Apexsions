package com.apexsions.shop.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class KingdomTaxCollectEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String kingdomId;
    private final double taxAmount;

    public KingdomTaxCollectEvent(Player player, String kingdomId, double taxAmount) {
        this.player = player;
        this.kingdomId = kingdomId;
        this.taxAmount = taxAmount;
    }

    public Player getPlayer() { return player; }
    public String getKingdomId() { return kingdomId; }
    public double getTaxAmount() { return taxAmount; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
