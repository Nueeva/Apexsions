package com.apexsions.battlepass.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BattlepassTierClaimEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final int level;
    private final String tier;

    public BattlepassTierClaimEvent(Player player, int level, String tier) {
        this.player = player;
        this.level = level;
        this.tier = tier;
    }

    public Player getPlayer() { return player; }
    public int getLevel() { return level; }
    public String getTier() { return tier; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
