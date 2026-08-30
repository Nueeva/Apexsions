package com.apexsions.media.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Service Locator for ApexsionsMedia API.
 * Provides graceful No-Op fallback when the plugin is unavailable.
 */
public final class ApexsionsMediaProvider {

    private static ApexsionsMediaAPI instance;

    private ApexsionsMediaProvider() {}

    /**
     * Retrieves the active ApexsionsMediaAPI instance, or the safe No-Op fallback if unavailable.
     * This method will NEVER throw an exception.
     */
    public static @NotNull ApexsionsMediaAPI get() {
        return instance != null ? instance : NoOpApexsionsMediaAPI.INSTANCE;
    }

    /**
     * Retrieves the optional active ApexsionsMediaAPI instance.
     */
    public static Optional<ApexsionsMediaAPI> getOptional() {
        return Optional.ofNullable(instance);
    }

    /**
     * Checks if ApexsionsMedia is currently loaded and available.
     */
    public static boolean isAvailable() {
        return instance != null;
    }

    public static void register(@NotNull ApexsionsMediaAPI api) {
        instance = Objects.requireNonNull(api, "ApexsionsMediaAPI instance cannot be null");
    }

    public static void unregister() {
        instance = null;
    }
}
