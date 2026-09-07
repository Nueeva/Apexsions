package com.apexsions.chat.integration;

import com.apexsions.chat.ApexsionsChatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Optional soft-dependency hook for AuthMeReloaded.
 * Safely inspects whether a player is authenticated and dynamically hooks into
 * authentication events (LoginEvent, RestoreSessionEvent) without requiring
 * AuthMe at compile time.
 */
public class AuthMeHook {

    private final ApexsionsChatPlugin plugin;
    private Boolean available = null;

    public AuthMeHook(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if AuthMe plugin is installed and enabled.
     */
    public boolean isAvailable() {
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
     * Check if a player is authenticated in AuthMe.
     * Returns true if AuthMe is NOT installed, or if player is logged in.
     */
    public boolean isAuthenticated(Player player) {
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

    /**
     * Register dynamic event listeners for AuthMe login and session restore.
     */
    @SuppressWarnings("unchecked")
    public void registerAuthEvents(Consumer<Player> onAuthenticated) {
        if (!isAvailable()) {
            return;
        }

        Listener dummyListener = new Listener() {};

        // 1. Hook LoginEvent
        try {
            Class<? extends Event> loginClass = (Class<? extends Event>) Class.forName("fr.xephi.authme.events.LoginEvent");
            Bukkit.getPluginManager().registerEvent(
                    loginClass,
                    dummyListener,
                    EventPriority.MONITOR,
                    (listener, event) -> {
                        try {
                            Method getPlayer = event.getClass().getMethod("getPlayer");
                            Player p = (Player) getPlayer.invoke(event);
                            if (p != null) {
                                onAuthenticated.accept(p);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error handling AuthMe LoginEvent: " + e.getMessage());
                        }
                    },
                    plugin
            );
            plugin.getLogger().info("Successfully hooked AuthMe LoginEvent for delayed chat messages.");
        } catch (Throwable t) {
            plugin.getLogger().warning("Could not register AuthMe LoginEvent listener: " + t.getMessage());
        }

        // 2. Hook RestoreSessionEvent (if available in this AuthMe build)
        try {
            Class<? extends Event> sessionClass = (Class<? extends Event>) Class.forName("fr.xephi.authme.events.RestoreSessionEvent");
            Bukkit.getPluginManager().registerEvent(
                    sessionClass,
                    dummyListener,
                    EventPriority.MONITOR,
                    (listener, event) -> {
                        try {
                            Method getPlayer = event.getClass().getMethod("getPlayer");
                            Player p = (Player) getPlayer.invoke(event);
                            if (p != null) {
                                onAuthenticated.accept(p);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error handling AuthMe RestoreSessionEvent: " + e.getMessage());
                        }
                    },
                    plugin
            );
            plugin.getLogger().info("Successfully hooked AuthMe RestoreSessionEvent for delayed chat messages.");
        } catch (ClassNotFoundException ignored) {
            // RestoreSessionEvent is not present in all AuthMe versions
        } catch (Throwable t) {
            plugin.getLogger().warning("Could not register AuthMe RestoreSessionEvent listener: " + t.getMessage());
        }
    }
}
