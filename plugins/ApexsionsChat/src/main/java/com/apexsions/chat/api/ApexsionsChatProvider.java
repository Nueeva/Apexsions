package com.apexsions.chat.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Service Locator for ApexsionsChat API.
 * Provides graceful No-Op fallback when the plugin is unavailable.
 */
public final class ApexsionsChatProvider {

    private static ApexsionsChatAPI instance;

    private ApexsionsChatProvider() {}

    /**
     * Retrieves the active ApexsionsChatAPI instance, or the safe No-Op fallback if unavailable.
     * This method will NEVER throw an exception.
     */
    public static @NotNull ApexsionsChatAPI get() {
        return instance != null ? instance : NoOpApexsionsChatAPI.INSTANCE;
    }

    /**
     * Retrieves the optional active ApexsionsChatAPI instance.
     */
    public static Optional<ApexsionsChatAPI> getOptional() {
        return Optional.ofNullable(instance);
    }

    /**
     * Checks if ApexsionsChat is currently loaded and available.
     */
    public static boolean isAvailable() {
        return instance != null;
    }

    public static void register(@NotNull ApexsionsChatAPI api) {
        instance = Objects.requireNonNull(api, "ApexsionsChatAPI instance cannot be null");
    }

    public static void unregister() {
        instance = null;
    }
}
