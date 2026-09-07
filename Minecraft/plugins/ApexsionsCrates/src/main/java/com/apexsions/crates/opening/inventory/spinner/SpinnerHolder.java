package com.apexsions.crates.opening.inventory.spinner;

import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.CratesPlugin;
import com.apexsions.crates.api.opening.Spinner;
import com.apexsions.crates.opening.inventory.InventoryOpening;

public class SpinnerHolder {

    private final String id;
    private final SpinnerType type;
    private final SpinnerData config;
    private final SpinnerProvider provider;

    public SpinnerHolder(@NotNull String id, @NotNull SpinnerType type, @NotNull SpinnerData config, @NotNull SpinnerProvider provider) {
        this.id = id.toLowerCase();
        this.type = type;
        this.config = config;
        this.provider = provider;
    }

    @NotNull
    public Spinner createSpinner(@NotNull CratesPlugin plugin, @NotNull InventoryOpening opening) {
        return this.provider.createSpinner(plugin, this.config, opening);
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public SpinnerType getType() {
        return this.type;
    }

    @NotNull
    public SpinnerData getConfig() {
        return this.config;
    }

    @NotNull
    public SpinnerProvider getProvider() {
        return this.provider;
    }
}
