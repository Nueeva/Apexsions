package com.apexsions.crates.api.opening;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.apexsions.crates.crate.cost.Cost;
import com.apexsions.crates.crate.impl.CrateSource;
import su.nightexpress.nightcore.config.FileConfig;

public interface OpeningProvider {

    void load(@NotNull FileConfig config);

    @NotNull String getId();

    @NotNull Opening createOpening(@NotNull Player player, @NotNull CrateSource source, @Nullable Cost cost);
}
