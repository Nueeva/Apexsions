package com.apexsions.economy.api;

import com.apexsions.economy.currency.Currency;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * Public service interface for ApexsionsEconomy.
 */
public interface ApexsionsEconomyAPI {

    double getBalance(@NotNull UUID uuid, @NotNull String currencyId);

    boolean has(@NotNull UUID uuid, @NotNull String currencyId, double amount);

    void deposit(@NotNull UUID uuid, @NotNull String currencyId, double amount);

    boolean withdraw(@NotNull UUID uuid, @NotNull String currencyId, double amount);

    boolean transfer(@NotNull UUID senderUuid, @NotNull UUID receiverUuid, @NotNull String currencyId, double amount);

    @NotNull
    Collection<Currency> getCurrencies();

    @Nullable
    Currency getCurrency(@NotNull String id);

    @NotNull
    String format(double amount, @NotNull String currencyId);

    @NotNull
    String formatCompact(double amount);
}
