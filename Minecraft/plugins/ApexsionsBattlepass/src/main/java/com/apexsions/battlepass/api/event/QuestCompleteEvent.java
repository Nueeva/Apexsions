package com.apexsions.battlepass.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class QuestCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String questId;
    private final String questCategory;
    private final int xpReward;

    public QuestCompleteEvent(Player player, String questId, String questCategory, int xpReward) {
        this.player = player;
        this.questId = questId;
        this.questCategory = questCategory;
        this.xpReward = xpReward;
    }

    public Player getPlayer() { return player; }
    public String getQuestId() { return questId; }
    public String getQuestCategory() { return questCategory; }
    public int getXpReward() { return xpReward; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
