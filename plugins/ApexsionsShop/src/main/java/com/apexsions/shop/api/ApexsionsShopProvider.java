package com.apexsions.shop.api;

public final class ApexsionsShopProvider {

    private static ApexsionsShopAPI instance;

    private ApexsionsShopProvider() {}

    public static ApexsionsShopAPI get() {
        return instance;
    }

    public static void register(ApexsionsShopAPI api) {
        instance = api;
    }

    public static void unregister() {
        instance = null;
    }
}
