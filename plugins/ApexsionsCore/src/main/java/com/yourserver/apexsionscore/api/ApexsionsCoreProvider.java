package com.yourserver.apexsionscore.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Static accessor / service locator for the KingdomCore API.
 */
public final class ApexsionsCoreProvider {

    private static ApexsionsCoreAPI instance;

    private ApexsionsCoreProvider() {}

    public static @NotNull ApexsionsCoreAPI get() {
        if (instance == null) {
            throw new IllegalStateException("ApexsionsCoreAPI has not been initialized yet or the plugin is disabled.");
        }
        return instance;
    }

    public static void register(@NotNull ApexsionsCoreAPI api) {
        instance = Objects.requireNonNull(api, "ApexsionsCoreAPI instance cannot be null");
    }

    public static void unregister() {
        instance = null;
    }
}
