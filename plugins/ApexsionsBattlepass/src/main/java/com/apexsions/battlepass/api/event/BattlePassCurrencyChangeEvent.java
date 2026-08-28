package com.apexsions.battlepass.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import java.util.UUID;

public class BattlePassCurrencyChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID playerUuid;
    private final int oldBalance;
    private final int newBalance;

    public BattlePassCurrencyChangeEvent(UUID playerUuid, int oldBalance, int newBalance) {
        this.playerUuid = playerUuid;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public int getOldBalance() {
        return oldBalance;
    }

    public int getNewBalance() {
        return newBalance;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
