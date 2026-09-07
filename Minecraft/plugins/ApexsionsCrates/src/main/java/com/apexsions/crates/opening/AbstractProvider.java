package com.apexsions.crates.opening;

import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.CratesPlugin;
import com.apexsions.crates.api.opening.OpeningProvider;

public abstract class AbstractProvider implements OpeningProvider {

    protected final CratesPlugin plugin;
    protected final String id;

    public AbstractProvider(@NotNull CratesPlugin plugin, @NotNull String id) {
        this.plugin = plugin;
        this.id = id;
    }

    @Override
    @NotNull
    public String getId() {
        return this.id;
    }
}
