package com.apexsions.economy.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AuctionPurchaseEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player buyer;
    private final UUID sellerUuid;
    private final ItemStack item;
    private final double price;

    public AuctionPurchaseEvent(Player buyer, UUID sellerUuid, ItemStack item, double price) {
        this.buyer = buyer;
        this.sellerUuid = sellerUuid;
        this.item = item;
        this.price = price;
    }

    public Player getBuyer() { return buyer; }
    public UUID getSellerUuid() { return sellerUuid; }
    public ItemStack getItem() { return item; }
    public double getPrice() { return price; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
