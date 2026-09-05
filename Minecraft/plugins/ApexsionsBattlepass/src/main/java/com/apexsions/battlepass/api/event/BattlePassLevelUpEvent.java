package com.apexsions.battlepass.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattlePassLevelUpEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final int newLevel;

    public BattlePassLevelUpEvent(Player player, int newLevel) {
        this.player = player;
        this.newLevel = newLevel;
    }

    public Player getPlayer() {
        return player;
    }

    public int getNewLevel() {
        return newLevel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
