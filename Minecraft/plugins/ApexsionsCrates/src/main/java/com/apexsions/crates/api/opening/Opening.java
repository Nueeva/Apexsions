package com.apexsions.crates.api.opening;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.apexsions.crates.api.crate.Reward;
import com.apexsions.crates.crate.cost.Cost;
import com.apexsions.crates.crate.impl.Crate;
import com.apexsions.crates.crate.impl.CrateSource;

import java.util.Collection;
import java.util.List;

public interface Opening {

    @Deprecated
    default void run() {
        this.start();
    }

    void start();

    void stop();

    void tick();

    boolean isCompleted();

    long getInterval();

    long getTickCount();

    boolean isTickTime();

    boolean isRunning();

    @NotNull Player getPlayer();

    @NotNull CrateSource getSource();

    @NotNull Crate getCrate();

    @Nullable Cost getCost();

    @NotNull List<Reward> getRewards();

    void addReward(@NotNull Reward reward);

    void addRewards(@NotNull Collection<Reward> rewards);

    void instaRoll();

    boolean isRefundable();

    void setRefundable(boolean refundable);
}
