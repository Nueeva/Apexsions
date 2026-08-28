package com.apexsions.battlepass.expshop.provider;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ExpShopCurrencyProvider {

    String getCurrencyId();

    String getDisplayName();

    boolean isAvailable();

    CompletableFuture<Boolean> hasBalance(UUID playerId, double amount);

    CompletableFuture<Boolean> withdraw(UUID playerId, double amount);

    String format(double amount);
}
