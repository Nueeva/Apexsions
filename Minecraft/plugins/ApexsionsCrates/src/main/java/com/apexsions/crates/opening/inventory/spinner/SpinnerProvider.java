package com.apexsions.crates.opening.inventory.spinner;

import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.CratesPlugin;
import com.apexsions.crates.api.opening.Spinner;
import com.apexsions.crates.opening.inventory.InventoryOpening;

public interface SpinnerProvider {

    @NotNull Spinner createSpinner(@NotNull CratesPlugin plugin, @NotNull SpinnerData data, @NotNull InventoryOpening opening);
}
