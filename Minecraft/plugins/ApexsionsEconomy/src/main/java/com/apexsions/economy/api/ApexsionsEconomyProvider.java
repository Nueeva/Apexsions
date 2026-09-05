package com.apexsions.economy.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Service Locator for ApexsionsEconomy API.
 * Provides graceful No-Op fallback when the plugin is unavailable.
 */
public final class ApexsionsEconomyProvider {

    private static ApexsionsEconomyAPI instance;

    private ApexsionsEconomyProvider() {}

    /**
     * Retrieves the active ApexsionsEconomyAPI instance, or the safe No-Op fallback if unavailable.
     * This method will NEVER throw an exception.
     */
    public static @NotNull ApexsionsEconomyAPI get() {
        return instance != null ? instance : NoOpApexsionsEconomyAPI.INSTANCE;
    }

    /**
     * Retrieves the optional active ApexsionsEconomyAPI instance.
     */
    public static Optional<ApexsionsEconomyAPI> getOptional() {
        return Optional.ofNullable(instance);
    }

    /**
     * Checks if ApexsionsEconomy is currently loaded and available.
     */
    public static boolean isAvailable() {
        return instance != null;
    }

    public static void register(@NotNull ApexsionsEconomyAPI api) {
        instance = Objects.requireNonNull(api, "ApexsionsEconomyAPI instance cannot be null");
    }

    public static void unregister() {
        instance = null;
    }
}
