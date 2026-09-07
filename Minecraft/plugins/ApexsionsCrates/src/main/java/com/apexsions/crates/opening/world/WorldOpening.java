package com.apexsions.crates.opening.world;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.apexsions.crates.CratesPlugin;
import com.apexsions.crates.crate.cost.Cost;
import com.apexsions.crates.crate.impl.CrateSource;
import com.apexsions.crates.opening.AbstractOpening;
import com.apexsions.crates.util.pos.WorldPos;

public abstract class WorldOpening extends AbstractOpening {

    public WorldOpening(@NotNull CratesPlugin plugin, @NotNull Player player, @NotNull CrateSource source, @Nullable Cost cost) {
        super(plugin, player, source, cost);
    }

    protected void hideHologram(@NotNull WorldPos blockPos) {
        this.plugin.getHologramManager().ifPresent(hologramManager -> hologramManager.disableBlockHologram(this.crate, blockPos));
    }

    protected void showHologram(@NotNull WorldPos blockPos) {
        this.plugin.getHologramManager().ifPresent(hologramManager -> hologramManager.enableBlockHologram(this.crate, blockPos));
    }
}
