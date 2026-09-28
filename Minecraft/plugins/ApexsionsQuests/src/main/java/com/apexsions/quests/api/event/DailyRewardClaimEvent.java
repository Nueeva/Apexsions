package com.apexsions.quests.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DailyRewardClaimEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final int day;
    private final double moneyReward;
    private final int coreXpReward;
    private final int passXpReward;

    public DailyRewardClaimEvent(@NotNull Player player, int day, double moneyReward, int coreXpReward, int passXpReward) {
        this.player = player;
        this.day = day;
        this.moneyReward = moneyReward;
        this.coreXpReward = coreXpReward;
        this.passXpReward = passXpReward;
    }

    public @NotNull Player getPlayer() { return player; }
    public int getDay() { return day; }
    public double getMoneyReward() { return moneyReward; }
    public int getCoreXpReward() { return coreXpReward; }
    public int getPassXpReward() { return passXpReward; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
