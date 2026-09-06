package com.apexsions.core.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * Optional soft-dependency hook for AuthMeReloaded.
 * Safely inspects whether a player is authenticated before allowing sensitive commands.
 */
public final class AuthMeHook {

    private static Boolean available = null;

    private AuthMeHook() {}

    /**
     * Check if AuthMe plugin is installed and enabled.
     */
    public static boolean isAvailable() {
        if (available == null) {
            try {
                Class.forName("fr.xephi.authme.api.v8.AuthMeApi");
                available = Bukkit.getPluginManager().isPluginEnabled("AuthMe");
            } catch (ClassNotFoundException e) {
                available = false;
            }
        }
        return available != null && available;
    }

    /**
     * Check if player is authenticated in AuthMe.
     * Returns true if AuthMe is NOT installed, or if player is logged in.
     */
    public static boolean isAuthenticated(Player player) {
        if (!isAvailable() || player == null) {
            return true;
        }

        try {
            Class<?> apiClass = Class.forName("fr.xephi.authme.api.v8.AuthMeApi");
            Method getInstance = apiClass.getMethod("getInstance");
            Object apiInstance = getInstance.invoke(null);
            Method isAuth = apiClass.getMethod("isAuthenticated", Player.class);
            return (Boolean) isAuth.invoke(apiInstance, player);
        } catch (Throwable ignored) {
            return true;
        }
    }
}
