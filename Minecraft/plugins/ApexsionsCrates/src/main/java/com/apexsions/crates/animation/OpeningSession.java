package com.apexsions.crates.animation;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.reward.Reward;
import org.bukkit.entity.Player;

public abstract class OpeningSession {

    protected final ApexsionsCratesPlugin plugin;
    protected final Player player;
    protected final Crate crate;
    protected final Reward winningReward;
    protected final boolean virtualKey;
    protected boolean completed = false;

    public OpeningSession(ApexsionsCratesPlugin plugin, Player player, Crate crate, boolean virtualKey) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;
        this.winningReward = crate.rollReward();
        this.virtualKey = virtualKey;
    }

    public Player getPlayer() {
        return player;
    }

    public Crate getCrate() {
        return crate;
    }

    public Reward getWinningReward() {
        return winningReward;
    }

    public abstract void start();

    public abstract void skip();

    public abstract void cancel();

    public boolean isCompleted() {
        return completed;
    }
}
