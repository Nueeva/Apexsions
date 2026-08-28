package com.apexsions.core.event;

import com.apexsions.core.level.xp.XpSource;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired when a player gains KingdomCore progression XP.
 */
public class KingdomXpGainEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID playerUuid;
    private final Player player;
    private final XpSource source;
    private long amount;
    private final long oldXp;
    private final long newXp;
    private boolean cancelled;

    public KingdomXpGainEvent(@NotNull UUID playerUuid, Player player, @NotNull XpSource source,
                             long amount, long oldXp, long newXp) {
        this.playerUuid = playerUuid;
        this.player = player;
        this.source = source;
        this.amount = amount;
        this.oldXp = oldXp;
        this.newXp = newXp;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Player getPlayer() {
        return player;
    }

    public XpSource getSource() {
        return source;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = Math.max(0, amount);
    }

    public long getOldXp() {
        return oldXp;
    }

    public long getNewXp() {
        return newXp;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
