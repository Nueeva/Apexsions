package com.apexsions.economy.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerBalanceChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID playerUuid;
    private final String currency;
    private final double oldBalance;
    private final double newBalance;
    private final String reason;

    public PlayerBalanceChangeEvent(UUID playerUuid, String currency, double oldBalance, double newBalance, String reason) {
        this.playerUuid = playerUuid;
        this.currency = currency;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
        this.reason = reason;
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public String getCurrency() { return currency; }
    public double getOldBalance() { return oldBalance; }
    public double getNewBalance() { return newBalance; }
    public String getReason() { return reason; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
