package com.apexsions.economy.api;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.currency.Currency;
import com.apexsions.economy.util.NumberFormatUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class ApexsionsEconomyAPIImpl implements ApexsionsEconomyAPI {

    private final ApexsionsEconomy plugin;

    public ApexsionsEconomyAPIImpl(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public double getBalance(@NotNull UUID uuid, @NotNull String currencyId) {
        return plugin.getCurrencyService().getBalance(uuid, currencyId);
    }

    @Override
    public boolean has(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        return plugin.getCurrencyService().has(uuid, currencyId, amount);
    }

    @Override
    public void deposit(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        plugin.getCurrencyService().addBalance(uuid, currencyId, amount);
    }

    @Override
    public boolean withdraw(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        return plugin.getCurrencyService().removeBalance(uuid, currencyId, amount);
    }

    @Override
    public boolean transfer(@NotNull UUID senderUuid, @NotNull UUID receiverUuid, @NotNull String currencyId, double amount) {
        Currency curr = plugin.getCurrencyRegistry().get(currencyId);
        if (curr == null || !curr.isTransferable()) return false;
        return plugin.getCurrencyService().transferAtomic(senderUuid, receiverUuid, currencyId, amount);
    }

    @Override
    public @NotNull Collection<Currency> getCurrencies() {
        return plugin.getCurrencyRegistry().getAll();
    }

    @Override
    public @Nullable Currency getCurrency(@NotNull String id) {
        return plugin.getCurrencyRegistry().get(id);
    }

    @Override
    public @NotNull String format(double amount, @NotNull String currencyId) {
        Currency c = getCurrency(currencyId);
        return NumberFormatUtil.format(amount, c);
    }

    @Override
    public @NotNull String formatCompact(double amount) {
        return NumberFormatUtil.formatCompact(amount);
    }
}
