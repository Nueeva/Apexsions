package com.apex.battlepass.expshop.provider;

import com.apex.economy.api.ApexsionsEconomyAPI;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ApexsionsEconomyCurrencyProvider implements ExpShopCurrencyProvider {

    private final String currencyId;
    private final String displayName;

    public ApexsionsEconomyCurrencyProvider(String currencyId, String displayName) {
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
        try {
            return ApexsionsEconomyAPI.getCurrency(currencyId) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public CompletableFuture<Boolean> hasBalance(UUID playerId, double amount) {
        try {
            boolean has = ApexsionsEconomyAPI.has(playerId, currencyId, amount);
            return CompletableFuture.completedFuture(has);
        } catch (Throwable t) {
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    public CompletableFuture<Boolean> withdraw(UUID playerId, double amount) {
        try {
            boolean success = ApexsionsEconomyAPI.withdraw(playerId, currencyId, amount);
            return CompletableFuture.completedFuture(success);
        } catch (Throwable t) {
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    public String format(double amount) {
        try {
            return ApexsionsEconomyAPI.format(amount, currencyId);
        } catch (Throwable t) {
            if ("rupiah".equalsIgnoreCase(currencyId)) {
                return "Rp." + String.format("%,.0f", amount);
            }
            return String.format("%,.0f %s", amount, displayName);
        }
    }
}
