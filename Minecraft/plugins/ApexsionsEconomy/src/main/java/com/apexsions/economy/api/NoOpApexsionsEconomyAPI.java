package com.apexsions.economy.api;

import com.apexsions.economy.currency.Currency;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Graceful No-Op Fallback implementation of ApexsionsEconomyAPI.
 */
public class NoOpApexsionsEconomyAPI implements ApexsionsEconomyAPI {

    public static final NoOpApexsionsEconomyAPI INSTANCE = new NoOpApexsionsEconomyAPI();

    private NoOpApexsionsEconomyAPI() {}

    @Override
    public double getBalance(@NotNull UUID uuid, @NotNull String currencyId) {
        return 0.0;
    }

    @Override
    public boolean has(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        return false;
    }

    @Override
    public void deposit(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        // No-Op
    }

    @Override
    public boolean withdraw(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        return false;
    }

    @Override
    public boolean transfer(@NotNull UUID senderUuid, @NotNull UUID receiverUuid, @NotNull String currencyId, double amount) {
        return false;
    }

    @Override
    public @NotNull Collection<Currency> getCurrencies() {
        return Collections.emptyList();
    }

    @Override
    public @Nullable Currency getCurrency(@NotNull String id) {
        return null;
    }

    @Override
    public @NotNull String format(double amount, @NotNull String currencyId) {
        return "Rp " + String.format("%,.0f", amount);
    }

    @Override
    public @NotNull String formatCompact(double amount) {
        return String.valueOf((long) amount);
    }
}
