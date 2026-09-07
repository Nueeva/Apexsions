package com.apexsions.crates.api.opening;

import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.CratesPlugin;

public interface ProviderSupplier {

    @NotNull OpeningProvider supply(@NotNull CratesPlugin plugin, /*@NotNull FileConfig config,*/ @NotNull String id);
}
