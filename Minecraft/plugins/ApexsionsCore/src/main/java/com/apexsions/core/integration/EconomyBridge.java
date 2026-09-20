package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Single reflective access point to ApexsionsEconomy, with a Vault fallback.
 *
 * Keeps ApexsionsEconomy an optional soft-dependency and avoids scattering the
 * same Class.forName block across every feature that moves money.
 */
public class EconomyBridge {

    public static final String DEFAULT_CURRENCY = "rupiah";

    private final ApexsionsCorePlugin plugin;
    private volatile Object api;

    public EconomyBridge(@NotNull ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Nullable
    private Object resolve() {
        if (api != null) {
            return api;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("ApexsionsEconomy")) {
            return null;
        }
        try {
            Class<?> providerClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyProvider");
            Object available = providerClass.getMethod("isAvailable").invoke(null);
            if (Boolean.TRUE.equals(available)) {
                api = providerClass.getMethod("get").invoke(null);
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.FINE, "ApexsionsEconomy API unavailable", t);
        }
        return api;
    }

    private boolean vaultAvailable() {
        return plugin.getVaultHook() != null && plugin.getVaultHook().hasEconomy();
    }

    public boolean isAvailable() {
        return resolve() != null || vaultAvailable();
    }

    public double getBalance(@NotNull UUID uuid, @NotNull String currencyId) {
        Object eco = resolve();
        if (eco != null) {
            try {
                Class<?> apiClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyAPI");
                Method m = apiClass.getMethod("getBalance", UUID.class, String.class);
                return ((Number) m.invoke(eco, uuid, currencyId)).doubleValue();
            } catch (Throwable ignored) {
                // fall through to Vault
            }
        }
        Player online = Bukkit.getPlayer(uuid);
        if (online != null && vaultAvailable()) {
            return plugin.getVaultHook().getBalance(online);
        }
        return 0.0;
    }

    public boolean has(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        Object eco = resolve();
        if (eco != null) {
            try {
                Class<?> apiClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyAPI");
                Method m = apiClass.getMethod("has", UUID.class, String.class, double.class);
                return Boolean.TRUE.equals(m.invoke(eco, uuid, currencyId, amount));
            } catch (Throwable ignored) {
                // fall through
            }
        }
        return getBalance(uuid, currencyId) >= amount;
    }

    public boolean withdraw(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        Object eco = resolve();
        if (eco != null) {
            try {
                Class<?> apiClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyAPI");
                Method m = apiClass.getMethod("withdraw", UUID.class, String.class, double.class);
                return Boolean.TRUE.equals(m.invoke(eco, uuid, currencyId, amount));
            } catch (Throwable ignored) {
                // fall through to Vault
            }
        }
        Player online = Bukkit.getPlayer(uuid);
        if (online != null && vaultAvailable() && getBalance(uuid, currencyId) >= amount) {
            plugin.getVaultHook().withdraw(online, amount);
            return true;
        }
        return false;
    }

    public void deposit(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        if (amount <= 0) {
            return;
        }
        Object eco = resolve();
        if (eco != null) {
            try {
                Class<?> apiClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyAPI");
                Method m = apiClass.getMethod("deposit", UUID.class, String.class, double.class);
                m.invoke(eco, uuid, currencyId, amount);
                return;
            } catch (Throwable ignored) {
                // fall through to Vault
            }
        }
        Player online = Bukkit.getPlayer(uuid);
        if (online != null && vaultAvailable()) {
            plugin.getVaultHook().deposit(online, amount);
        }
    }

    @NotNull
    public String format(double amount, @NotNull String currencyId) {
        Object eco = resolve();
        if (eco != null) {
            try {
                Class<?> apiClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyAPI");
                Method m = apiClass.getMethod("format", double.class, String.class);
                Object out = m.invoke(eco, amount, currencyId);
                if (out != null) {
                    return out.toString();
                }
            } catch (Throwable ignored) {
                // fall through
            }
        }
        return "Rp" + String.format(java.util.Locale.ROOT, "%,.0f", amount);
    }
}
