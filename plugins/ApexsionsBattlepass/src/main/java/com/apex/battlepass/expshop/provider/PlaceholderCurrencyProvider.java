package com.apex.battlepass.expshop.provider;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlaceholderCurrencyProvider implements ExpShopCurrencyProvider {

    private final String currencyId;
    private final String displayName;

    public PlaceholderCurrencyProvider(String currencyId, String displayName) {
        this.currencyId = currencyId.toLowerCase();
        this.displayName = displayName;
    }

    @Override
    public String getCurrencyId() {
        return currencyId;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean isAvailable() {
        // Placeholder provider is not yet configured for live money transactions
        return false;
    }

    @Override
    public CompletableFuture<Boolean> hasBalance(UUID playerId, double amount) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Boolean> withdraw(UUID playerId, double amount) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public String format(double amount) {
        return String.format("%.0f %s", amount, displayName);
    }
}
