package com.apexsions.core.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Static accessor / service locator for the ApexsionsCore API.
 * Provides graceful No-Op fallback when the plugin is unavailable to ensure soft-dependency stability.
 */
public final class ApexsionsCoreProvider {

    private static ApexsionsCoreAPI instance;

    private ApexsionsCoreProvider() {}

    /**
     * Retrieves the active ApexsionsCoreAPI instance, or the safe No-Op fallback if unavailable.
     * This method will NEVER throw an exception.
     */
    public static @NotNull ApexsionsCoreAPI get() {
        return instance != null ? instance : NoOpApexsionsCoreAPI.INSTANCE;
    }

    /**
     * Retrieves the optional active ApexsionsCoreAPI instance.
     */
    public static Optional<ApexsionsCoreAPI> getOptional() {
        return Optional.ofNullable(instance);
    }

    /**
     * Checks if ApexsionsCore is currently loaded and available.
     */
    public static boolean isAvailable() {
        return instance != null;
    }

    public static void register(@NotNull ApexsionsCoreAPI api) {
        instance = Objects.requireNonNull(api, "ApexsionsCoreAPI instance cannot be null");
    }

    public static void unregister() {
        instance = null;
    }
}
