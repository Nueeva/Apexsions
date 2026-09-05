package com.apexsions.shop.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Service Locator for ApexsionsShop API.
 * Provides graceful No-Op fallback when the plugin is unavailable.
 */
public final class ApexsionsShopProvider {

    private static ApexsionsShopAPI instance;

    private ApexsionsShopProvider() {}

    /**
     * Retrieves the active ApexsionsShopAPI instance, or the safe No-Op fallback if unavailable.
     * This method will NEVER throw an exception.
     */
    public static @NotNull ApexsionsShopAPI get() {
        return instance != null ? instance : NoOpApexsionsShopAPI.INSTANCE;
    }

    /**
     * Retrieves the optional active ApexsionsShopAPI instance.
     */
    public static Optional<ApexsionsShopAPI> getOptional() {
        return Optional.ofNullable(instance);
    }

    /**
     * Checks if ApexsionsShop is currently loaded and available.
     */
    public static boolean isAvailable() {
        return instance != null;
    }

    public static void register(@NotNull ApexsionsShopAPI api) {
        instance = Objects.requireNonNull(api, "ApexsionsShopAPI instance cannot be null");
    }

    public static void unregister() {
        instance = null;
    }
}
