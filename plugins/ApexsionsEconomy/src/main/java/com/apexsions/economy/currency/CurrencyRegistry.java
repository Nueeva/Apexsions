package com.apexsions.economy.currency;

import java.util.*;

public class CurrencyRegistry {

    private final Map<String, Currency> currencies = new LinkedHashMap<>();

    public CurrencyRegistry() {
        register(new RupiahCurrency());
        register(new DiamondCurrency());
    }

    public void register(Currency currency) {
        if (currency != null) {
            currencies.put(currency.getId().toLowerCase(), currency);
        }
    }

    public Currency get(String id) {
        if (id == null) return null;
        return currencies.get(id.toLowerCase());
    }

    public Collection<Currency> getAll() {
        return Collections.unmodifiableCollection(currencies.values());
    }

    public Currency getDefault() {
        return currencies.get("rupiah");
    }
}
