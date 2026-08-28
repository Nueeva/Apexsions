package com.apex.battlepass.currency;

import java.util.UUID;

public interface CurrencyService {

    int getBalance(UUID uuid);

    void addCurrency(UUID uuid, int amount);

    boolean removeCurrency(UUID uuid, int amount);

    void setCurrency(UUID uuid, int amount);

    boolean hasBalance(UUID uuid, int amount);

    String getCurrencyName();

    String getCurrencySymbol();

    String format(int amount);
}
