package com.apexsions.crates.opening.world.provider;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.apexsions.crates.CratesPlugin;
import com.apexsions.crates.api.opening.OpeningProvider;
import com.apexsions.crates.crate.cost.Cost;
import com.apexsions.crates.crate.impl.CrateSource;
import com.apexsions.crates.opening.world.impl.DummyOpening;
import su.nightexpress.nightcore.config.FileConfig;

public class DummyProvider implements OpeningProvider {

    private final CratesPlugin plugin;

    public DummyProvider(@NotNull CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getId() {
        return "dummy";
    }

    @Override
    public void load(@NotNull FileConfig config) {

    }

    @Override
    @NotNull
    public DummyOpening createOpening(@NotNull Player player, @NotNull CrateSource source, @Nullable Cost cost) {
        return new DummyOpening(this.plugin, player, source, cost);
    }
}
