package com.apexsions.crates.opening;

import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.api.opening.OpeningProvider;
import com.apexsions.crates.api.opening.ProviderLoader;
import com.apexsions.crates.api.opening.ProviderSupplier;
import com.apexsions.crates.config.Config;
import com.apexsions.crates.opening.inventory.InventoryProvider;
import com.apexsions.crates.opening.selectable.SelectableProvider;
import com.apexsions.crates.opening.world.provider.SimpleRollProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProviderRegistry {

    private static final Map<String, ProviderLoader> LOADERS   = new HashMap<>();
    private static final Set<OpeningProvider>        PROVIDERS = new HashSet<>();

    public static void load() {
        ProviderRegistry.registerLoader(Config.DIR_OPENINGS_INVENTORY, InventoryProvider::new);
        ProviderRegistry.registerLoader(Config.DIR_OPENINGS_SIMPLE_ROLL, SimpleRollProvider::new);
        ProviderRegistry.registerLoader(Config.DIR_OPENINGS_SELECTABLE, SelectableProvider::new);
    }

    public static void clear() {
        LOADERS.clear();
        PROVIDERS.clear();
    }

    public static void registerLoader(@NotNull String directory, @NotNull ProviderSupplier supplier) {
        LOADERS.put(directory.toLowerCase(), new ProviderLoader(directory, supplier));
    }

    public static void registerProvider(@NotNull OpeningProvider provider) {
        PROVIDERS.add(provider);
    }

    @NotNull
    public static Set<ProviderLoader> getLoaders() {
        return new HashSet<>(LOADERS.values());
    }

    @NotNull
    public static Set<OpeningProvider> getProviders() {
        return new HashSet<>(PROVIDERS);
    }
}
