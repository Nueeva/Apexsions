package com.apexsions.crates.api;

import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.reward.Reward;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a player wins a reward from a crate.
 */
public class CrateRewardWinEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Crate crate;
    private final Reward reward;

    public CrateRewardWinEvent(@NotNull Player player, @NotNull Crate crate, @NotNull Reward reward) {
        this.player = player;
        this.crate = crate;
        this.reward = reward;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Crate getCrate() {
        return crate;
    }

    public @NotNull Reward getReward() {
        return reward;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
