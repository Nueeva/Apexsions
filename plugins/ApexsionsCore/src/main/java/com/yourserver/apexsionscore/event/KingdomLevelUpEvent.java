package com.yourserver.apexsionscore.event;

import com.yourserver.apexsionscore.region.Region;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired when a player levels up in KingdomCore.
 */
public class KingdomLevelUpEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID playerUuid;
    private final Player player;
    private final int oldLevel;
    private final int newLevel;
    private final Region region;
    private final String oldTitle;
    private final String newTitle;

    public KingdomLevelUpEvent(@NotNull UUID playerUuid, Player player, int oldLevel, int newLevel,
                               Region region, String oldTitle, String newTitle) {
        this.playerUuid = playerUuid;
        this.player = player;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.region = region;
        this.oldTitle = oldTitle;
        this.newTitle = newTitle;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Player getPlayer() {
        return player;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public Region getRegion() {
        return region;
    }

    public String getOldTitle() {
        return oldTitle;
    }

    public String getNewTitle() {
        return newTitle;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
