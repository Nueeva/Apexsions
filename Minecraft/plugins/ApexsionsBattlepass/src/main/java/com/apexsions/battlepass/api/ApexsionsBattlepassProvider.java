package com.apexsions.battlepass.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Service Locator for ApexsionsBattlepass API.
 * Provides graceful No-Op fallback when the plugin is unavailable.
 */
public final class ApexsionsBattlepassProvider {

    private static ApexsionsBattlepassAPI instance;

    private ApexsionsBattlepassProvider() {}

    /**
     * Retrieves the active ApexsionsBattlepassAPI instance, or the safe No-Op fallback if unavailable.
     * This method will NEVER throw an exception.
     */
    public static @NotNull ApexsionsBattlepassAPI get() {
        return instance != null ? instance : NoOpApexsionsBattlepassAPI.INSTANCE;
    }

    /**
     * Retrieves the optional active ApexsionsBattlepassAPI instance.
     */
    public static Optional<ApexsionsBattlepassAPI> getOptional() {
        return Optional.ofNullable(instance);
    }

    /**
     * Checks if ApexsionsBattlepass is currently loaded and available.
     */
    public static boolean isAvailable() {
        return instance != null;
    }

    public static void register(@NotNull ApexsionsBattlepassAPI api) {
        instance = Objects.requireNonNull(api, "ApexsionsBattlepassAPI instance cannot be null");
    }

    public static void unregister() {
        instance = null;
    }
}
