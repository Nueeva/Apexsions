package com.apexsions.quests.api.event;

import com.apexsions.quests.model.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DailyQuestCompleteEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Quest quest;

    public DailyQuestCompleteEvent(@NotNull Player player, @NotNull Quest quest) {
        this.player = player;
        this.quest = quest;
    }

    public @NotNull Player getPlayer() { return player; }
    public @NotNull Quest getQuest() { return quest; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
