package com.apexsions.core.api.event;

import com.apexsions.core.level.xp.XpSource;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerGainXpEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private long amount;
    private final XpSource source;
    private boolean cancelled;

    public PlayerGainXpEvent(Player player, long amount, XpSource source) {
        this.player = player;
        this.amount = amount;
        this.source = source;
    }

    public Player getPlayer() { return player; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public XpSource getSource() { return source; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
