package com.yourserver.apexsionschat.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ApexsionsChatProvider {

    private static ApexsionsChatAPI instance;

    private ApexsionsChatProvider() {}

    public static @Nullable ApexsionsChatAPI get() {
        return instance;
    }

    public static void register(@NotNull ApexsionsChatAPI api) {
        instance = api;
    }

    public static void unregister() {
        instance = null;
    }
}
