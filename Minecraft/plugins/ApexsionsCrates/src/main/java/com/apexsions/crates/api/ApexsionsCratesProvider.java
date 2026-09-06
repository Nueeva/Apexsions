package com.apexsions.crates.api;

public final class ApexsionsCratesProvider {

    private static ApexsionsCratesAPI instance;

    private ApexsionsCratesProvider() {}

    public static ApexsionsCratesAPI get() {
        if (instance == null) {
            throw new IllegalStateException("ApexsionsCratesAPI has not been initialized yet!");
        }
        return instance;
    }

    public static void register(ApexsionsCratesAPI api) {
        instance = api;
    }

    public static void unregister() {
        instance = null;
    }

    public static boolean isAvailable() {
        return instance != null;
    }
}
